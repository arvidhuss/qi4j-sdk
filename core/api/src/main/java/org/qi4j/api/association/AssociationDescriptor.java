/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.api.association;

import org.qi4j.api.common.QualifiedName;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;

/**
 * JAVADOC
 */
public interface AssociationDescriptor
{
    /**
     * Get metadata that implements the given type
     *
     * @param infoType the type of metadata to be returned
     *
     * @return the metadata for the given type, or <code>null</code> if
     *         no such metadata has been registered
     */
    <T> T metaInfo( Class<T> infoType );

    /**
     * Get the qualified name of the association. This is constructed by
     * concatenating the name of the declaring interface with the name
     * of the method, using ":" as separator. Example:<br/>
     * com.somecompany.MyInterface with association method<br/>
     * Association<String> someAssociation();<br/>
     * will have the qualified name:<br/>
     * com.somecompany.MyInterface:someAssociation
     *
     * @return the qualified name of the association
     */
    QualifiedName qualifiedName();

    /**
     * Get the type of the associated Entities
     *
     * @return the type of the associated Entities
     */
    Type type();

    boolean isImmutable();

    boolean isAggregated();

    AccessibleObject accessor();

    boolean queryable();
}
