/**
 * Copyright (c) 2012, Paul Merlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.eventsourcing.domain.source;

import org.qi4j.api.activation.ActivatorAdapter;
import org.qi4j.api.service.ServiceReference;

public interface EventStoreActivation
{

    void activateEventStore()
            throws Exception;

    void passivateEventStore()
            throws Exception;

    public static class Activator
            extends ActivatorAdapter<ServiceReference<EventStoreActivation>>
    {

        @Override
        public void afterActivation( ServiceReference<EventStoreActivation> activated )
                throws Exception
        {
            activated.get().activateEventStore();
        }

        @Override
        public void beforePassivation( ServiceReference<EventStoreActivation> passivating )
                throws Exception
        {
            passivating.get().passivateEventStore();
        }

    }

}
