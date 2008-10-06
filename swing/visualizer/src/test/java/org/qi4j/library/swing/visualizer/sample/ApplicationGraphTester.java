/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.library.swing.visualizer.sample;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import javax.swing.UIManager;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.Composite;
import org.qi4j.composite.Mixins;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.association.AssociationMixin;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.library.constraints.annotation.NotNull;
import org.qi4j.library.swing.visualizer.ApplicationGraph;
import org.qi4j.property.Property;
import org.qi4j.structure.Application;
import static org.qi4j.structure.Visibility.application;

/**
 * TODO
 */
public class ApplicationGraphTester
{
    public static void main( String[] args )
        throws Throwable
    {
        UIManager.setLookAndFeel( new Plastic3DLookAndFeel() );

        Energy4Java qi4j = new Energy4Java();

        ApplicationAssembly assembly = qi4j.newApplicationAssembly();
        assembly.setName( "My Qi4j App" );

        LayerAssembly infrastructureLayer = assembly.newLayerAssembly( "Infrastructure" );
        ModuleAssembly database = infrastructureLayer.newModuleAssembly( "Database" );

        LayerAssembly domainLayer = assembly.newLayerAssembly( "Domain" );

        ModuleAssembly someDomain = domainLayer.newModuleAssembly( "Some domain" );
        someDomain.addServices( MemoryEntityStoreService.class ).visibleIn( application );
        someDomain.addEntities( MyEntity.class ).visibleIn( application );
        someDomain.addComposites( ADomainComposite.class, BDomainComposite.class );

        LayerAssembly guiLayer = assembly.newLayerAssembly( "UI" );

        ModuleAssembly swingModule = guiLayer.newModuleAssembly( "Swing" );

        ModuleAssembly plugin1 = guiLayer.newModuleAssembly( "Plugin 1" );

        ModuleAssembly plugin2 = guiLayer.newModuleAssembly( "Plugin 2" );
        plugin2.addComposites( UIComposite.class );

        guiLayer.uses( infrastructureLayer );
        guiLayer.uses( domainLayer );
        domainLayer.uses( infrastructureLayer );

//        addMoreLayers( assembly );

        Application app = qi4j.newApplication( assembly );
        new ApplicationGraph().show( app );
    }

    private static void addMoreLayers( ApplicationAssembly assembly )
    {
        LayerAssembly layer = assembly.newLayerAssembly( "Layer 1" );
        for( int i = 1; i < 5; i++ )
        {
            LayerAssembly anotherLayer = assembly.newLayerAssembly( "Layer 1." + i );
            layer.uses( anotherLayer );
            layer = anotherLayer;
        }

        for( int i = 2; i < 6; i++ )
        {
            LayerAssembly newLayer = assembly.newLayerAssembly( "Layer " + i );
            for( int k = 1; k < 5; k++ )
            {
                newLayer.newModuleAssembly( "Module " + k );
            }
        }
    }

    @Mixins( A.AMixin.class )
    private static interface A
    {
        @NotNull Property<String> a();

        void a( @NotNull String s, int i );

        public abstract class AMixin implements A
        {
            public void a( @NotNull String s, int i )
            {
                System.out.println( s );
            }
        }
    }

    private static interface ADomainComposite extends A, Composite
    {

    }

    @Mixins( AssociationMixin.class )
    private static interface BDomainComposite extends Composite
    {
        @NotNull Association<ADomainComposite> aComposites();
    }

    private static interface MyEntity extends EntityComposite
    {
    }


    private static interface UIComposite extends Composite
    {
    }
}
