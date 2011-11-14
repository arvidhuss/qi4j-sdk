/*
 * Copyright (c) 2010, Stanislav Muhametsin. All Rights Reserved.
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

package org.qi4j.library.sql.ds;

import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;

/**
 * 
 * @author Stanislav Muhametsin
 * @author Paul Merlin
 */
public interface PGDataSourceConfiguration
    extends ConfigurationComposite
{

    @Optional
    public Property<String> server();

    @Optional
    public Property<Integer> port();

    @Optional
    public Property<String> database();

    @Optional
    public Property<String> user();

    @Optional
    public Property<String> password();

}
