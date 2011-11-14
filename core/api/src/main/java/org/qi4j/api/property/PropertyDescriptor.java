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

package org.qi4j.api.property;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.structure.Module;
import org.qi4j.api.type.ValueType;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;

/**
 * JAVADOC
 */
public interface PropertyDescriptor
{
    boolean isImmutable();

    /**
     * Access metadata about the property with a given type.
     * The info is registered for the property during
     * assembly of the application.
     *
     * @param infoType the type of the metadata to return
     *
     * @return a metadata object that implements the requested type or null if none is registered
     */
    <T> T metaInfo( Class<T> infoType );

    /**
     * Get the qualified name of the property which is equal to:<br/>
     * <interface name>:<method name>
     *
     * @return the qualified name of the property
     */
    QualifiedName qualifiedName();

    /**
     * Get the type of the property. If the property is declared
     * as Property<X> then X is returned.
     *
     * @return the property type
     */
    Type type();

    AccessibleObject accessor();

    Object initialValue( Module module );

    ValueType valueType();

    boolean queryable();
}
