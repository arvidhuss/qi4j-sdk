/*
 * Copyright 2008 Alin Dreghiciu.
 * Copyright 2009 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *count( INTERFACES_OF.map( A.class ) ), equalTo( 1L )
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.spi.query;

import org.qi4j.api.common.Optional;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.dataset.DataSet;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.query.grammar.OrderBy;
import org.qi4j.functional.Specification;

import java.util.Map;

/**
 * JAVADOC Add JavaDoc
 */
public interface EntityFinder
{
    Iterable<EntityReference> findEntities( Class<?> resultType,
                                            @Optional Specification<Composite> whereClause,
                                            @Optional OrderBy[] orderBySegments,
                                            @Optional Integer firstResult,
                                            @Optional Integer maxResults,
                                            Map<String,Object> variables
    )
        throws EntityFinderException;

    EntityReference findEntity( Class<?> resultType, @Optional Specification<Composite> whereClause, Map<String, Object> variables )
        throws EntityFinderException;

    long countEntities( Class<?> resultType, @Optional Specification<Composite> whereClause, Map<String, Object> variables )
        throws EntityFinderException;
}