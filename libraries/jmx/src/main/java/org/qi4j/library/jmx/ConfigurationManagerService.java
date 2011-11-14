/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.library.jmx;

import org.qi4j.api.Qi4j;
import org.qi4j.api.association.AssociationStateHolder;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.EnumType;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

import javax.management.*;
import javax.management.modelmbean.DescriptorSupport;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Expose ConfigurationComposites through JMX. Allow configurations to be edited, and the services to be restarted.
 */
@Mixins( ConfigurationManagerService.Mixin.class )
public interface ConfigurationManagerService
    extends ServiceComposite, Activatable
{
    class Mixin
        implements Activatable
    {
        @Structure
        UnitOfWorkFactory uowf;

        @Service
        MBeanServer server;

        @Structure
        Application application;

        @Structure
        Qi4j api;

        @Service
        Iterable<ServiceReference<Configuration>> configurableServices;

        private List<ObjectName> configurationNames = new ArrayList<ObjectName>();

        public void activate()
            throws Exception
        {
            // Expose configurable services
            exportConfigurableServices();
        }

        private void exportConfigurableServices()
            throws NotCompliantMBeanException, MBeanRegistrationException, InstanceAlreadyExistsException, MalformedObjectNameException
        {
            for( ServiceReference<Configuration> configurableService : configurableServices )
            {
                String serviceClass = configurableService.get().getClass().getInterfaces()[ 0 ].getName();
                String name = configurableService.identity();
                ServiceDescriptor serviceDescriptor = api.getServiceDescriptor( configurableService );
                Module module = api.getModule( configurableService );
                Class<Object> configurationClass = serviceDescriptor.configurationType();
                if (configurationClass != null)
                {
                    EntityDescriptor descriptor = module.entityDescriptor( configurationClass.getName() );
                    List<MBeanAttributeInfo> attributes = new ArrayList<MBeanAttributeInfo>();
                    Map<String, AccessibleObject> properties = new HashMap<String, AccessibleObject>();
                    for( PropertyDescriptor persistentProperty : descriptor.state().properties() )
                    {
                        if( !persistentProperty.isImmutable() )
                        {
                            String propertyName = persistentProperty.qualifiedName().name();
                            String type = persistentProperty.valueType().type().getName();

                            Descriptor attrDescriptor = new DescriptorSupport();
                            attrDescriptor.setField( "name",  propertyName);
                            attrDescriptor.setField( "descriptorType",  "attribute");

                            if( persistentProperty.valueType() instanceof EnumType )
                            {
                                type = String.class.getName();

                                // Try to add legal values
                                try
                                {
                                    Set<String> legalValues = new LinkedHashSet();
                                    Class<?> enumType = getClass().getClassLoader().loadClass( persistentProperty.valueType().type().getName() );
                                    for (Field field : enumType.getFields())
                                    {
                                        legalValues.add( field.getName() );
                                    }
                                    attrDescriptor.setField( "legalValues",  legalValues);
                                } catch (ClassNotFoundException e)
                                {
                                    // Ignore
                                    e.printStackTrace();
                                }
                            }
                            attributes.add( new MBeanAttributeInfo( propertyName, type, propertyName, true, true, type.equals( "java.lang.Boolean" ), attrDescriptor ) );
                            properties.put( propertyName, persistentProperty.accessor() );
                        }
                    }

                    List<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>();
                    if( configurableService instanceof Activatable )
                    {
                        operations.add( new MBeanOperationInfo( "restart", "Restart service", new MBeanParameterInfo[0], "java.lang.String", MBeanOperationInfo.ACTION_INFO ) );
                    }

                    MBeanInfo mbeanInfo = new MBeanInfo( serviceClass, name, attributes.toArray( new MBeanAttributeInfo[attributes
                        .size()] ), null, operations.toArray( new MBeanOperationInfo[operations.size()] ), null );
                    Object mbean = new ConfigurableService( configurableService, mbeanInfo, name, properties );
                    ObjectName configurableServiceName;
                    ObjectName serviceName = Qi4jMBeans.findServiceName( server, application.name(), name);
                    if (serviceName != null)
                    {
                       configurableServiceName = new ObjectName(serviceName.toString()+",name=Configuration");
                    } else
                       configurableServiceName = new ObjectName( "Configuration:name=" + name );


                    server.registerMBean( mbean, configurableServiceName );
                    configurationNames.add( configurableServiceName );
                }
            }
        }

        public void passivate()
            throws Exception
        {
            for( ObjectName configurableServiceName : configurationNames )
            {
                server.unregisterMBean( configurableServiceName );
            }
        }

        abstract class EditableConfiguration
            implements DynamicMBean
        {
            MBeanInfo info;
            String identity;
            Map<String, AccessibleObject> propertyNames;

            EditableConfiguration( MBeanInfo info, String identity, Map<String, AccessibleObject> propertyNames )
            {
                this.info = info;
                this.identity = identity;
                this.propertyNames = propertyNames;
            }

            public Object getAttribute( String name )
                throws AttributeNotFoundException, MBeanException, ReflectionException
            {
                UnitOfWork uow = uowf.newUnitOfWork();
                try
                {
                    EntityComposite configuration = uow.get( EntityComposite.class, identity );
                    AssociationStateHolder state = api.getState( configuration );
                    AccessibleObject accessor = propertyNames.get( name );
                    Property<Object> property = state.propertyFor( accessor );
                    Object object = property.get();
                    if( object instanceof Enum )
                    {
                        object = object.toString();
                    }
                    return object;
                }
                catch( Exception ex )
                {
                    throw new ReflectionException( ex, "Could not get attribute " + name );
                }
                finally
                {
                    uow.discard();
                }
            }

            public void setAttribute( Attribute attribute )
                throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
            {
                UnitOfWork uow = uowf.newUnitOfWork();
                try
                {
                    EntityComposite configuration = uow.get( EntityComposite.class, identity );
                    AssociationStateHolder state = api.getState( (EntityComposite) configuration );
                    AccessibleObject accessor = propertyNames.get( attribute.getName() );
                    Property<Object> property = state.propertyFor( accessor );
                    PropertyDescriptor propertyDescriptor = api.getPropertyDescriptor( property );
                    if( EnumType.isEnum( propertyDescriptor.type() ))
                    {
                        property.set( Enum.valueOf( (Class<Enum>) propertyDescriptor.type(), attribute.getValue().toString() ) );
                    }
                    else
                    {
                        property.set( attribute.getValue() );
                    }

                    uow.complete();
                }
                catch( Exception ex )
                {
                    uow.discard();
                }
            }

            public AttributeList getAttributes( String[] names )
            {
                AttributeList list = new AttributeList();
                for( String name : names )
                {
                    try
                    {
                        Object value = getAttribute( name );
                        list.add( new Attribute( name, value ) );
                    }
                    catch( AttributeNotFoundException e )
                    {
                        e.printStackTrace();
                    }
                    catch( MBeanException e )
                    {
                        e.printStackTrace();
                    }
                    catch( ReflectionException e )
                    {
                        e.printStackTrace();
                    }
                }

                return list;
            }

            public AttributeList setAttributes( AttributeList attributeList )
            {
                AttributeList list = new AttributeList();
                for( int i = 0; i < list.size(); i++ )
                {
                    Attribute attribute = (Attribute) list.get( i );

                    try
                    {
                        setAttribute( attribute );
                        list.add( attribute );
                    }
                    catch( AttributeNotFoundException e )
                    {
                        e.printStackTrace();
                    }
                    catch( InvalidAttributeValueException e )
                    {
                        e.printStackTrace();
                    }
                    catch( MBeanException e )
                    {
                        e.printStackTrace();
                    }
                    catch( ReflectionException e )
                    {
                        e.printStackTrace();
                    }
                }

                return list;
            }

            public MBeanInfo getMBeanInfo()
            {
                return info;
            }
        }

        class ConfigurableService
            extends EditableConfiguration
        {
            private ServiceReference<Configuration> service;

            ConfigurableService( ServiceReference<Configuration> service,
                                 MBeanInfo info,
                                 String identity,
                                 Map<String, AccessibleObject> propertyNames
            )
            {
                super( info, identity, propertyNames );
                this.service = service;
            }

            public Object invoke( String s, Object[] objects, String[] strings )
                throws MBeanException, ReflectionException
            {
                if( s.equals( "restart" ) )
                {
                    try
                    {
                        // Refresh and restart
                        if( service.isActive() )
                        {
                            // Refresh configuration
                            service.get().refresh();

                            ( (Activatable) service ).passivate();
                            ( (Activatable) service ).activate();
                        }

                        return "Service restarted";
                    }
                    catch( Exception e )
                    {
                        return "Could not restart service:" + e.getMessage();
                    }
                }

                return "Unknown operation";
            }
        }
    }
}