/*
 * Copyright (c) 2010 Paul Merlin <paul@nosphere.org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.qi4j.library.shiro.tests.x509;

import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.library.shiro.domain.permissions.Permission;
import org.qi4j.library.shiro.domain.permissions.PermissionFactory;
import org.qi4j.library.shiro.domain.permissions.Role;
import org.qi4j.library.shiro.domain.permissions.RoleFactory;
import org.qi4j.library.shiro.domain.x509.X509LightFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@Mixins( X509Fixtures.Mixin.class )
public interface X509Fixtures
        extends ServiceComposite, Activatable
{

    String PERMISSION = "gizmo";
    String ROLE = "admin";

    abstract class Mixin
            implements X509Fixtures
    {

        private static final Logger LOGGER = LoggerFactory.getLogger( X509Fixtures.Mixin.class );
        @Structure
        private UnitOfWorkFactory uowf;
        @Service
        private PermissionFactory permissionFactory;
        @Service
        private RoleFactory roleFactory;
        @Service
        private X509LightFactory x509Factory;

        public void activate()
                throws Exception
        {

            LOGGER.debug( "Will inject X509Fixtures" );

            // Create Test User
            UnitOfWork uow = uowf.newUnitOfWork();

            Permission permission = permissionFactory.create( PERMISSION );
            Role role = roleFactory.create( ROLE, Arrays.asList( new Permission[]{ permission } ) );

            EntityBuilder<UserEntity> userBuilder = uow.newEntityBuilder( UserEntity.class );
            UserEntity user = userBuilder.instance();
            user.x509().set( x509Factory.create( X509FixturesData.testUserCertificate() ) );
            user = userBuilder.newInstance();

            role.assignTo( user );

            uow.complete();
        }

        public void passivate()
                throws Exception
        {
        }

    }

}
