/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.structure;

import org.qi4j.api.composite.TransientBuilderFactory;
import org.qi4j.api.composite.TransientDescriptor;
import org.qi4j.api.entity.EntityDescriptor;
import org.qi4j.api.event.ActivationEventListenerRegistration;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.object.ObjectFactory;
import org.qi4j.api.object.ObjectDescriptor;
import org.qi4j.api.query.QueryBuilderFactory;
import org.qi4j.api.service.ServiceFinder;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueDescriptor;

/**
 * API for interacting with a Module. Instances
 * of this can be accessed by using the {@link Structure}
 * injection scope.
 */
public interface Module
    extends ActivationEventListenerRegistration,
        TransientBuilderFactory,
        ObjectFactory,
        ValueBuilderFactory,
        UnitOfWorkFactory,
        QueryBuilderFactory,
        ServiceFinder
{
    String name();

    <T> T metaInfo( Class<T> infoType );

    @Deprecated
    TransientBuilderFactory transientBuilderFactory();

    @Deprecated
    ObjectFactory objectFactory();

    @Deprecated
    ValueBuilderFactory valueBuilderFactory();

    @Deprecated
    UnitOfWorkFactory unitOfWorkFactory();

    @Deprecated
    QueryBuilderFactory queryBuilderFactory();

    @Deprecated
    ServiceFinder serviceFinder();

    ClassLoader classLoader();

    TransientDescriptor transientDescriptor( String typeName );

    EntityDescriptor entityDescriptor( String typeName );

    ObjectDescriptor objectDescriptor( String typeName );

    ValueDescriptor valueDescriptor( String typeName );
}
