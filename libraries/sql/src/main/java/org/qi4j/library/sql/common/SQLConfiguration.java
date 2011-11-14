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

package org.qi4j.library.sql.common;

import org.qi4j.api.common.Optional;
import org.qi4j.api.configuration.ConfigurationComposite;
import org.qi4j.api.property.Property;
import org.qi4j.library.sql.ds.DataSourceService;

/**
 * Typical configuration for service, which uses data source (through {@link DataSourceService} ) as connection to SQL
 * database, and given schema name as schema to create tables in.
 * 
 * @author Stanislav Muhametsin
 */
public interface SQLConfiguration
    extends ConfigurationComposite // DataSourceConfiguration
{

    /**
     * The schema name to use to create/find tables.
     */
    @Optional
    Property<String> schemaName();
}
