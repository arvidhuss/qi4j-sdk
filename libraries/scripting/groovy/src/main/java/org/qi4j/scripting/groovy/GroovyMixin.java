/*
 * Copyright 2008 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.scripting.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyShell;
import org.qi4j.api.common.AppliesTo;
import org.qi4j.api.common.AppliesToFilter;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.injection.scope.This;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Generic mixin that implements interfaces by delegating to Groovy functions
 * using Groovy. Each method in an interface is declared by a Groovy method
 * in a file located in classpath with the name "<interface>.groovy",
 * where the interface name includes the package, and has "." replaced with "/".
 * <p/>
 * Example:
 * org/qi4j/samples/hello/domain/HelloWorldSpeaker.groovy
 * org/qi4j/samples/hello/domain/HelloWorldSpeaker.sayAgain.groovy
 *
 */
@AppliesTo( GroovyMixin.AppliesTo.class )
public class GroovyMixin
    implements InvocationHandler
{
    private @This Composite me;
    private final Map<Class, GroovyObject> groovyObjects;

    public static class AppliesTo
        implements AppliesToFilter
    {
        public boolean appliesTo( Method method, Class compositeType, Class mixin, Class modelClass )
        {
            return getFunctionResource( method ) != null;
        }
    }

    public GroovyMixin()
    {

        groovyObjects = new HashMap<Class, GroovyObject>();
    }

    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
    {
        final FunctionResource groovySource = getFunctionResource( method );
        if( groovySource != null )
        {
            if( groovySource.script )
            {
                return invokeAsObject( method, args, groovySource.url );
            }
            return invokeAsScript( method, args, groovySource.url );
        }
        throw new RuntimeException( "Internal error: Mixin invoked even if it does not apply" );
    }

    private Object invokeAsObject( Method method, Object[] args, URL groovySource ) throws Throwable
    {
        try
        {
            Class declaringClass = method.getDeclaringClass();
            GroovyObject groovyObject = groovyObjects.get( declaringClass );
            if( groovyObject == null )
            {
                InputStream is = null;
                final Class groovyClass;
                try
                {
                    is = groovySource.openStream();
                    GroovyClassLoader groovyClassLoader = new GroovyClassLoader( declaringClass.getClassLoader() );
                    groovyClass = groovyClassLoader.parseClass( is );
                }
                finally
                {
                    if( is != null )
                    {
                        is.close();
                    }
                }
                groovyObject = (GroovyObject) groovyClass.newInstance();
                // TODO check if there is such a property
                groovyObject.setProperty( "This", me );
                groovyObjects.put( declaringClass, groovyObject );
            }
            return groovyObject.invokeMethod( method.getName(), args );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            throw e;
        }
    }

    private Object invokeAsScript( Method method, Object[] args, URL groovySource ) throws Throwable
    {
        try
        {
            Binding binding = new Binding();
            binding.setVariable( "This", me );
            // TODO bind arguments to an "args" variable?
            GroovyShell shell = new GroovyShell( binding );
            InputStream is = null;
            try
            {
                is = groovySource.openStream();
                return shell.evaluate( is );
            }
            finally
            {
                if( is != null )
                {
                    is.close();
                }
            }
        }
        catch( Exception e )
        {
            e.printStackTrace();
            throw e;
        }
    }

    private static FunctionResource getFunctionResource( final Method method )
    {
        boolean script = false;
        final String scriptPath = method.getDeclaringClass().getName().replace( '.', File.separatorChar );
        String scriptFile = scriptPath + "." + method.getName() + ".groovy";
        URL scriptUrl = method.getDeclaringClass().getClassLoader().getResource( scriptFile );
        if( scriptUrl == null )
        {
            script = true;
            scriptFile = scriptPath + ".groovy";
            scriptUrl = method.getDeclaringClass().getClassLoader().getResource( scriptFile );
        }
        if( scriptUrl != null )
        {
            return new FunctionResource( script, scriptUrl );
        }
        return null;
    }

    private static class FunctionResource
    {
        URL url;
        boolean script;

        private FunctionResource( final boolean script, final URL url )
        {
            this.script = script;
            this.url = url;
        }
    }

}