/*
 * Copyright 2011-2014 Paul Merlin.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.uowfile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.concern.UnitOfWorkConcern;
import org.qi4j.api.unitofwork.concern.UnitOfWorkPropagation;
import org.qi4j.api.unitofwork.concern.UnitOfWorkRetry;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.io.Inputs;
import org.qi4j.io.Outputs;
import org.qi4j.library.uowfile.bootstrap.UoWFileAssembler;
import org.qi4j.library.uowfile.internal.ConcurrentUoWFileModificationException;
import org.qi4j.library.uowfile.plural.HasUoWFilesLifecycle;
import org.qi4j.library.uowfile.plural.UoWFilesLocator;
import org.qi4j.test.EntityTestAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HasUoWFilesTest
    extends AbstractUoWFileTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger( HasUoWFilesTest.class );
    private static final URL CREATION_CONTENT_URL = HasUoWFilesTest.class.getResource( "creation.txt" );
    private static final URL MODIFICATION_CONTENT_URL = HasUoWFilesTest.class.getResource( "modification.txt" );

    // START SNIPPET: uowfile
    public enum MyEnum
    {
        fileOne, fileTwo
    }

    // START SNIPPET: entity
    public interface TestedEntity
        extends HasUoWFilesLifecycle<MyEnum> // END SNIPPET: entity
        , Identity
    // START SNIPPET: entity
    {
        Property<String> name();
    }
    // END SNIPPET: entity
    // END SNIPPET: uowfile

    // START SNIPPET: locator
    public static abstract class TestedFilesLocatorMixin
        implements UoWFilesLocator<MyEnum>
    {
        @This
        private Identity meAsIdentity;

        @Override
        public Iterable<File> locateAttachedFiles()
        {
            List<File> list = new ArrayList<>();
            for( MyEnum eachValue : MyEnum.values() )
            {
                list.add( new File( baseTestDir, meAsIdentity.identity().get() + "." + eachValue.name() ) );
            }
            return list;
        }

        @Override
        public File locateAttachedFile( MyEnum key )
        {
            return new File( baseTestDir, meAsIdentity.identity().get() + "." + key.name() );
        }
    }
    // END SNIPPET: locator

    @Mixins( TestServiceMixin.class )
    @Concerns( UnitOfWorkConcern.class )
    public interface TestService
    {
        void modifyFile( String entityId )
            throws IOException;

        @UnitOfWorkPropagation
        @UnitOfWorkRetry
        void modifyFileWithRetry( String entityId, long sleepBefore, long sleepAfter )
            throws IOException;
    }

    public static class TestServiceMixin
        implements TestService
    {
        @Structure
        private Module module;

        @Override
        public void modifyFile( String entityId )
            throws IOException
        {
            modifyFileImmediatly( entityId );
        }

        @Override
        public void modifyFileWithRetry( String entityId, long sleepBefore, long sleepAfter )
            throws IOException
        {
            LOGGER.info( "Waiting " + sleepBefore + "ms before file modification" );
            if( sleepBefore > 0 )
            {
                try
                {
                    Thread.sleep( sleepBefore );
                }
                catch( InterruptedException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
            modifyFileImmediatly( entityId );
            LOGGER.info( "Waiting " + sleepAfter + "ms after file modification" );
            if( sleepAfter > 0 )
            {
                try
                {
                    Thread.sleep( sleepAfter );
                }
                catch( InterruptedException ex )
                {
                    throw new RuntimeException( ex );
                }
            }
        }

        private void modifyFileImmediatly( String entityId )
            throws IOException
        {
            TestedEntity entity = module.currentUnitOfWork().get( TestedEntity.class, entityId );
            // START SNIPPET: api
            File attachedFileTwo = entity.attachedFile( MyEnum.fileTwo );
            File managedFileOne = entity.managedFile( MyEnum.fileOne );
            // END SNIPPET: api
            Inputs.text( MODIFICATION_CONTENT_URL ).transferTo( Outputs.text( managedFileOne ) );
        }
    }

    @Override
    // START SNIPPET: assembly
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new UoWFileAssembler().assemble( module );

        module.entities( TestedEntity.class ).withMixins( TestedFilesLocatorMixin.class );
        // END SNIPPET: assembly
        module.services( TestService.class );
        new EntityTestAssembler().assemble( module );
        // START SNIPPET: assembly
    }
    // END SNIPPET: assembly

    private TestService testService;

    @Before
    public void beforeTest()
    {
        testService = module.<TestService>findService( TestService.class ).get();
    }

    @Test
    public void testCreation()
        throws UnitOfWorkCompletionException, IOException
    {
        LOGGER.info( "# Test Creation ##############################################################################" );
        File attachedFile;

        // Test discarded creation
        try( UnitOfWork uow = module.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Creation Rollback" );
            attachedFile = entity.attachedFile( MyEnum.fileOne );
        }
        assertFalse( "File still exists after discarded creation UoW", attachedFile.exists() );

        // Test completed creation
        try( UnitOfWork uow = module.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Creation" );
            attachedFile = entity.attachedFile( MyEnum.fileOne );
            uow.complete();
        }
        assertTrue( "File content was not the good one", isFileFirstLineEqualsTo( attachedFile, "Creation" ) );
    }

    @Test
    public void testModification()
        throws UnitOfWorkCompletionException, IOException
    {
        LOGGER.info( "# Test Modification ##########################################################################" );
        final String entityId;
        File attachedFile;

        // Create new
        try( UnitOfWork uow = module.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Modification" );
            entityId = entity.identity().get();
            attachedFile = entity.attachedFile( MyEnum.fileOne );
            uow.complete();
        }

        // Testing discarded modification
        try( UnitOfWork uow = module.newUnitOfWork() )
        {
            testService.modifyFile( entityId );
        }
        assertTrue( "File content after discarded modification was not the good one", isFileFirstLineEqualsTo( attachedFile, "Creation" ) );

        // Testing completed modification
        try( UnitOfWork uow = module.newUnitOfWork() )
        {
            testService.modifyFile( entityId );
            uow.complete();
        }
        assertTrue( "Modified file content was not the good one", isFileFirstLineEqualsTo( attachedFile, "Modification" ) );
    }

    @Test
    public void testDeletion()
        throws UnitOfWorkCompletionException, IOException
    {
        LOGGER.info( "# Test Deletion ##############################################################################" );
        final String entityId;
        File attachedFile;

        // Create new
        try( UnitOfWork uow = module.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Deletion" );
            entityId = entity.identity().get();
            attachedFile = entity.attachedFile( MyEnum.fileOne );
            uow.complete();
        }

        // Testing discarded deletion
        try( UnitOfWork uow = module.newUnitOfWork() )
        {
            TestedEntity entity = uow.get( TestedEntity.class, entityId );
            uow.remove( entity );
        }
        assertTrue( "File do not exists after discarded deletion", attachedFile.exists() );

        // Testing completed deletion
        try( UnitOfWork uow = module.newUnitOfWork() )
        {
            TestedEntity entity = uow.get( TestedEntity.class, entityId );
            uow.remove( entity );
            uow.complete();
        }
        assertFalse( "File still exists after deletion", attachedFile.exists() );
    }

    @Test
    public void testConcurrentModification()
        throws IOException, UnitOfWorkCompletionException
    {
        LOGGER.info( "# Test Concurrent Modification ###############################################################" );
        final String entityId;

        // Create new
        try( UnitOfWork uow = module.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Concurrent Modification" );
            entityId = entity.identity().get();
            uow.complete();
        }

        // Testing concurrent modification
        UnitOfWork uow, uow2;
        TestedEntity entity;

        uow = module.newUnitOfWork();
        entity = uow.get( TestedEntity.class, entityId );
        Inputs.text( MODIFICATION_CONTENT_URL ).transferTo( Outputs.text( entity.managedFile( MyEnum.fileOne ) ) );

        uow2 = module.newUnitOfWork();
        entity = uow2.get( TestedEntity.class, entityId );
        Inputs.text( MODIFICATION_CONTENT_URL ).transferTo( Outputs.text( entity.managedFile( MyEnum.fileOne ) ) );

        uow.complete();
        try
        {
            uow2.complete();
            fail( "A ConcurrentUoWFileModificationException should have been raised" );
        }
        catch( ConcurrentUoWFileModificationException expected )
        {
            uow2.discard();
        }
    }

    @Test
    public void testRetry()
        throws IOException, UnitOfWorkCompletionException, InterruptedException
    {
        LOGGER.info( "# Test Retry #################################################################################" );
        final String entityId;
        File attachedFile;

        // Create new
        try( UnitOfWork uow = module.newUnitOfWork() )
        {
            TestedEntity entity = createTestedOneEntityTwoFilesEntity( uow, "Testing Concurrent Modification" );
            entityId = entity.identity().get();
            attachedFile = entity.attachedFile( MyEnum.fileOne );
            uow.complete();
        }

        final List<Exception> ex = new ArrayList<>();
        Thread t1 = new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    testService.modifyFileWithRetry( entityId, 0, 10000 );
                }
                catch( Exception ex1 )
                {
                    ex.add( ex1 );
                }
            }
        }, "job1" );
        Thread t2 = new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    testService.modifyFileWithRetry( entityId, 5000, 0 );
                }
                catch( Exception ex1 )
                {
                    ex.add( ex1 );
                }
            }
        }, "job2" );

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        for( Exception eachEx : ex )
        {
            eachEx.printStackTrace();
        }

        assertTrue( "There were errors during TestRetry", ex.isEmpty() );
        assertTrue( "Modified file content was not the good one", isFileFirstLineEqualsTo( attachedFile, "Modification" ) );
    }

    private TestedEntity createTestedOneEntityTwoFilesEntity( UnitOfWork uow, String name )
        throws IOException
    {
        EntityBuilder<TestedEntity> builder = uow.newEntityBuilder( TestedEntity.class );
        TestedEntity entity = builder.instance();
        entity.name().set( name );
        entity = builder.newInstance();
        Inputs.text( CREATION_CONTENT_URL ).transferTo( Outputs.text( entity.managedFile( MyEnum.fileOne ) ) );
        return entity;
    }

}
