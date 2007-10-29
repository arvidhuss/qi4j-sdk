/*
 * Copyright (c) 2007, Sianny Halim. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.general.model.associations;

import java.io.Serializable;
import org.qi4j.annotation.Mixins;
import org.qi4j.library.framework.properties.PropertiesMixin;
import org.qi4j.library.general.model.State;

/**
 * Represents one-to-one relationship with {@link org.qi4j.library.general.model.State}
 */
@Mixins( { PropertiesMixin.class } )
public interface HasState extends Serializable
{
    void setState( State aState );

    State getState();
}
