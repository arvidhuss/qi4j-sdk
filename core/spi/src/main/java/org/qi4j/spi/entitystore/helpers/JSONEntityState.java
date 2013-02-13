/*
 * Copyright 2007, Niclas Hedhman. All Rights Reserved.
 * Copyright 2009, Rickard Öberg. All Rights Reserved.
 * Copyright 2013, Paul Merlin. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi.entitystore.helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.property.PropertyDescriptor;
import org.qi4j.api.type.ValueType;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.api.value.ValueSerializationException;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStatus;
import org.qi4j.spi.entity.ManyAssociationState;
import org.qi4j.spi.entitystore.DefaultEntityStoreUnitOfWork;
import org.qi4j.spi.entitystore.EntityStoreException;

/**
 * Standard implementation of EntityState.
 */
public final class JSONEntityState
    implements EntityState
{
    public static final String JSON_KEY_PROPERTIES = "properties";
    public static final String JSON_KEY_ASSOCIATIONS = "associations";
    public static final String JSON_KEY_MANYASSOCIATIONS = "manyassociations";
    public static final String JSON_KEY_IDENTITY = "identity";
    public static final String JSON_KEY_APPLICATION_VERSION = "application_version";
    public static final String JSON_KEY_TYPE = "type";
    public static final String JSON_KEY_VERSION = "version";
    public static final String JSON_KEY_MODIFIED = "modified";
    private static final String[] EMPTY_NAMES = new String[ 0 ];
    private static final String[] CLONE_NAMES = {
        JSON_KEY_IDENTITY,
        JSON_KEY_APPLICATION_VERSION,
        JSON_KEY_TYPE,
        JSON_KEY_VERSION,
        JSON_KEY_MODIFIED
    };

    private final DefaultEntityStoreUnitOfWork unitOfWork;
    private final ValueSerialization valueSerialization;
    private final String version;
    private final EntityReference identity;
    private final EntityDescriptor entityDescriptor;

    private EntityStatus status;
    private long lastModified;
    private JSONObject state;

    /* package */ JSONEntityState( DefaultEntityStoreUnitOfWork unitOfWork,
                                   ValueSerialization valueSerialization,
                                   EntityReference identity,
                                   EntityDescriptor entityDescriptor,
                                   JSONObject initialState )
    {
        this( unitOfWork,
              valueSerialization,
              "",
              unitOfWork.currentTime(),
              identity,
              EntityStatus.NEW,
              entityDescriptor,
              initialState );
    }

    /* package */ JSONEntityState( DefaultEntityStoreUnitOfWork unitOfWork,
                                   ValueSerialization valueSerialization,
                                   String version,
                                   long lastModified,
                                   EntityReference identity,
                                   EntityStatus status,
                                   EntityDescriptor entityDescriptor,
                                   JSONObject state
    )
    {
        this.unitOfWork = unitOfWork;
        this.valueSerialization = valueSerialization;
        this.version = version;
        this.lastModified = lastModified;
        this.identity = identity;
        this.status = status;
        this.entityDescriptor = entityDescriptor;
        this.state = state;
    }

    // EntityState implementation

    @Override
    public final String version()
    {
        return version;
    }

    @Override
    public long lastModified()
    {
        return lastModified;
    }

    @Override
    public EntityReference identity()
    {
        return identity;
    }

    @Override
    public Object propertyValueOf( QualifiedName stateName )
    {
        try
        {
            Object json = state.getJSONObject( JSON_KEY_PROPERTIES ).opt( stateName.name() );
            if( JSONObject.NULL.equals( json ) )
            {
                return null;
            }
            else
            {
                PropertyDescriptor descriptor = entityDescriptor.state().findPropertyModelByQualifiedName( stateName );
                if( descriptor == null )
                {
                    return null;
                }
                return valueSerialization.deserialize( descriptor.valueType(), json.toString() );
            }
        }
        catch( ValueSerializationException e )
        {
            throw new EntityStoreException( e );
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public void setPropertyValue( QualifiedName stateName, Object newValue )
    {
        try
        {
            Object jsonValue;
            if( newValue == null || ValueType.isPrimitiveValue( newValue ) )
            {
                jsonValue = newValue;
            }
            else
            {
                String serialized = valueSerialization.serialize( newValue );
                if( serialized.startsWith( "{" ) )
                {
                    jsonValue = new JSONObject( serialized );
                }
                else if( serialized.startsWith( "[" ) )
                {
                    jsonValue = new JSONArray( serialized );
                }
                else
                {
                    jsonValue = serialized;
                }
            }
            cloneStateIfGlobalStateLoaded();
            state.getJSONObject( JSON_KEY_PROPERTIES ).put( stateName.name(), jsonValue );
            markUpdated();
        }
        catch( ValueSerializationException e )
        {
            throw new EntityStoreException( e );
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    private JSONObject cloneJSON( JSONObject jsonObject )
        throws JSONException
    {
        String[] names = JSONObject.getNames( jsonObject );
        if( names == null )
        {
            names = EMPTY_NAMES;
        }
        return new JSONObject( jsonObject, names );
    }

    @Override
    public EntityReference associationValueOf( QualifiedName stateName )
    {
        try
        {
            Object jsonValue = state.getJSONObject( JSON_KEY_ASSOCIATIONS ).opt( stateName.name() );
            if( jsonValue == null )
            {
                return null;
            }

            EntityReference value = jsonValue == JSONObject.NULL ? null : EntityReference.parseEntityReference(
                (String) jsonValue );
            return value;
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public void setAssociationValue( QualifiedName stateName, EntityReference newEntity )
    {
        try
        {
            cloneStateIfGlobalStateLoaded();
            state.getJSONObject( JSON_KEY_ASSOCIATIONS )
                .put( stateName.name(), newEntity == null ? null : newEntity.identity() );
            markUpdated();
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public ManyAssociationState manyAssociationValueOf( QualifiedName stateName )
    {
        try
        {
            JSONObject manyAssociations = state.getJSONObject( JSON_KEY_MANYASSOCIATIONS );
            JSONArray jsonValues = manyAssociations.optJSONArray( stateName.name() );
            if( jsonValues == null )
            {
                jsonValues = new JSONArray();
                manyAssociations.put( stateName.name(), jsonValues );
            }
            return new JSONManyAssociationState( this, jsonValues );
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }

    @Override
    public void remove()
    {
        status = EntityStatus.REMOVED;
    }

    @Override
    public EntityStatus status()
    {
        return status;
    }

    @Override
    public boolean isAssignableTo( Class<?> type )
    {
        return entityDescriptor.isAssignableTo( type );
    }

    @Override
    public EntityDescriptor entityDescriptor()
    {
        return entityDescriptor;
    }

    public JSONObject state()
    {
        return state;
    }

    @Override
    public String toString()
    {
        return identity + "(" + state + ")";
    }

    public void markUpdated()
    {
        if( status == EntityStatus.LOADED )
        {
            status = EntityStatus.UPDATED;
        }
    }

    boolean isStateNotCloned()
    {
        return status == EntityStatus.LOADED;
    }

    void cloneStateIfGlobalStateLoaded()
    {
        if( isStateNotCloned() )
        {
            return;
        }

        try
        {
            JSONObject newProperties = cloneJSON( state.getJSONObject( JSON_KEY_PROPERTIES ) );
            JSONObject newAssoc = cloneJSON( state.getJSONObject( JSON_KEY_ASSOCIATIONS ) );
            JSONObject newManyAssoc = cloneJSON( state.getJSONObject( JSON_KEY_MANYASSOCIATIONS ) );
            JSONObject stateClone = new JSONObject( state, CLONE_NAMES );
            stateClone.put( JSON_KEY_PROPERTIES, newProperties );
            stateClone.put( JSON_KEY_ASSOCIATIONS, newAssoc );
            stateClone.put( JSON_KEY_MANYASSOCIATIONS, newManyAssoc );
            state = stateClone;
        }
        catch( JSONException e )
        {
            throw new EntityStoreException( e );
        }
    }
}
