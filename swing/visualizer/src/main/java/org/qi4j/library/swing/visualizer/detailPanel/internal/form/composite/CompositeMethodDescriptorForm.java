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
package org.qi4j.library.swing.visualizer.detailPanel.internal.form.composite;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import static com.jgoodies.forms.factories.DefaultComponentFactory.getInstance;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.Component;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.qi4j.library.swing.visualizer.detailPanel.internal.common.ToStringUtils;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.CompositeMethodDetailDescriptor;

/**
 * TODO: Use prefuse to draw invocation stack per method
 *
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class CompositeMethodDescriptorForm
{
    private Component methodSeparator;
    private JTextField compositeMethod;
    private JTextField composite;

    private JPanel methodPanel;

    public final void updateModel( CompositeMethodDetailDescriptor aDescriptor )
    {
        String compositeName = null;
        String methodSignature = null;

        if( aDescriptor != null )
        {
            Method compositeMethod = aDescriptor.descriptor().method();
            methodSignature = ToStringUtils.methodToString( compositeMethod );
            CompositeDetailDescriptor composite = aDescriptor.composite();
            compositeName = composite.descriptor().type().getName();
        }

        compositeMethod.setText( methodSignature );
        composite.setText( compositeName );
    }

    private void createUIComponents()
    {
        DefaultComponentFactory cmpFactory = getInstance();
        // TODO: Localization
        methodSeparator = cmpFactory.createSeparator( "Composite Method" );
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        createUIComponents();
        methodPanel = new JPanel();
        methodPanel.setLayout( new FormLayout( "fill:4px:noGrow,fill:max(p;4px):noGrow,left:4dlu:noGrow,fill:max(p;75dlu):noGrow,left:4dlu:noGrow,fill:d:grow,left:4px:noGrow", "center:max(d;4px):noGrow,top:p:noGrow,top:4dlu:noGrow,center:p:noGrow,top:4dlu:noGrow,center:p:noGrow,center:max(d;4px):noGrow" ) );
        ( (FormLayout) methodPanel.getLayout() ).setRowGroups( new int[][]{ new int[]{ 4, 6 }, new int[]{ 1, 7 } } );
        ( (FormLayout) methodPanel.getLayout() ).setColumnGroups( new int[][]{ new int[]{ 3, 5 }, new int[]{ 1, 7 } } );
        CellConstraints cc = new CellConstraints();
        methodPanel.add( methodSeparator, cc.xyw( 2, 2, 5 ) );
        final JLabel label1 = new JLabel();
        label1.setText( "Method" );
        methodPanel.add( label1, cc.xy( 2, 4 ) );
        final JLabel label2 = new JLabel();
        label2.setText( "Composite" );
        methodPanel.add( label2, cc.xy( 2, 6 ) );
        compositeMethod = new JTextField();
        compositeMethod.setEditable( false );
        methodPanel.add( compositeMethod, cc.xy( 4, 4, CellConstraints.FILL, CellConstraints.DEFAULT ) );
        composite = new JTextField();
        composite.setEditable( false );
        methodPanel.add( composite, cc.xy( 4, 6, CellConstraints.FILL, CellConstraints.DEFAULT ) );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return methodPanel;
    }
}
