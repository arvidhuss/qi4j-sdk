/*
 * Copyright (c) 2007, Rickard �berg. All Rights Reserved.
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

package org.qi4j.spi.serialization;

import java.io.Serializable;
import java.util.Map;
import org.qi4j.composite.Composite;

/**
 * TODO
 */
class SerializedComposite
    implements Serializable
{
    private Map<Class, Object> mixins;
    private Class<Composite> compositeInterface;

    public SerializedComposite( Map<Class, Object> mixins, Class<Composite> compositeInterface )
    {
        this.mixins = mixins;
        this.compositeInterface = compositeInterface;
    }

    public Map<Class, Object> getMixins()
    {
        return mixins;
    }

    public Class<Composite> getCompositeInterface()
    {
        return compositeInterface;
    }
}
