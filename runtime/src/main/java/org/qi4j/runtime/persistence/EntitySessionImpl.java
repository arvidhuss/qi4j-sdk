/*  Copyright 2007 Niclas Hedhman.
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
package org.qi4j.runtime.persistence;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.CompositeBuilderFactory;
import static org.qi4j.composite.PropertyValue.property;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntitySession;
import org.qi4j.entity.Identity;
import org.qi4j.entity.IdentityGenerator;
import org.qi4j.query.Query;
import org.qi4j.query.QueryBuilderFactory;
import org.qi4j.query.QueryBuilderFactoryImpl;
import org.qi4j.query.QueryableIterable;
import org.qi4j.runtime.composite.EntityCompositeInstance;
import org.qi4j.spi.composite.CompositeModel;
import org.qi4j.spi.persistence.EntityStateHolder;
import org.qi4j.spi.persistence.PersistenceException;
import org.qi4j.spi.persistence.PersistentStore;

public class EntitySessionImpl
    implements EntitySession
{
    private HashMap<String, ? extends EntityComposite> cache;

    private boolean open;
    private PersistentStore store;
    private CompositeBuilderFactory builderFactory;
    private IdentityGenerator identityGenerator;

    public EntitySessionImpl( PersistentStore store, CompositeBuilderFactory builderFactory, IdentityGenerator identityGenerator )
    {
        this.identityGenerator = identityGenerator;
        this.builderFactory = builderFactory;
        this.open = true;
        this.store = store;
        cache = new HashMap<String, EntityComposite>();
    }

    public <T extends EntityComposite> CompositeBuilder<T> newEntityBuilder( String identity, Class<T> compositeType )
    {
        CompositeBuilder<T> builder = builderFactory.newCompositeBuilder( compositeType );

        if( identity == null )
        {
            identity = identityGenerator.generate( compositeType );
        }

        builder.properties( Identity.class, property( "identity", identity ) );
        return builder;
    }

    public <T> T attach( T entity )
    {
        return null;
    }

    public void remove( EntityComposite entity )
    {
        try
        {
            store.delete( entity.getIdentity() );
        }
        catch( PersistenceException e )
        {
            throw new EntityStorageException( "Storage unable to remove entity " + entity.getIdentity(), e );
        }
    }

    public <T extends EntityComposite> T find( String identity, Class<T> compositeType )
    {
        // TODO: Argument check.

        try
        {
            T entity = compositeType.cast( cache.get( identity ) );
            if( entity == null )
            {
                CompositeBuilder<T> builder = builderFactory.newCompositeBuilder( compositeType );
                CompositeModel model = null; // TODO builder.getCompositeModel();
                builder.properties( Identity.class, property( "identity", identity ) );
                entity = builder.newInstance();
                EntityStateHolder holder = store.getEntityInstance( identity, model );
                EntityCompositeInstance handler = EntityCompositeInstance.getEntityCompositeInstance( entity );
                handler.setEntityStateHolder( holder );
            }
            else
            {
                if( entity.isReference() )
                {
                    CompositeBuilder<T> builder = builderFactory.newCompositeBuilder( compositeType );
                    CompositeModel model = null; // TODO builder.getCompositeModel();
                    EntityStateHolder holder = store.getEntityInstance( identity, model );
                    EntityCompositeInstance handler = EntityCompositeInstance.getEntityCompositeInstance( entity );
                    handler.setEntityStateHolder( holder );
                }
            }
            return entity;
        }
        catch( PersistenceException e )
        {
            throw new EntityStorageException( "Storage unable to access entity " + identity, e );
        }
    }

    public <T extends EntityComposite> T getReference( String identity, Class<T> compositeType )
    {
        return null;
    }

    public void refresh( EntityComposite entity )
    {
    }

    public void clear()
    {
    }

    public boolean contains( EntityComposite entity )
    {
        return false;
    }

    public QueryBuilderFactory getQueryFactory()
    {
        return new QueryBuilderFactoryImpl( new QueryableIterable( Collections.emptyList() ) );
    }

    public Query getNamedQuery( String name )
    {
        return null;
    }

    public Query newQuery( String expression, Class compositeType )
    {
        return null;
    }

    public void close()
    {
        open = false;
    }

    public boolean isOpen()
    {
        return open;
    }

    public URL toURL( Identity identity )
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
