/*  Copyright 2008 Edward Yakop.
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
package org.qi4j.library.swing.visualizer.school.domain.model.user.assembler;

import org.qi4j.composite.Mixins;
import org.qi4j.entity.EntityComposite;
import org.qi4j.injection.scope.This;
import org.qi4j.library.swing.visualizer.school.domain.model.user.User;

/**
 * @author edward.yakop@gmail.com
 */
@Mixins( UserEntity.UserMixin.class )
interface UserEntity extends User, EntityComposite
{
    class UserMixin
        implements User
    {
        @This private UserDetailState state;

        public final String firstName()
        {
            return state.firstName().get();
        }

        public final String lastName()
        {
            return state.lastName().get();
        }

        public final String userName()
        {
            return state.userName().get();
        }
    }
}
