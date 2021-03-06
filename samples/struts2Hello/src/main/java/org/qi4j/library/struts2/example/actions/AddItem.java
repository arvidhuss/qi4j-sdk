package org.qi4j.library.struts2.example.actions;

import org.apache.struts2.config.Result;
import org.apache.struts2.config.Results;
import org.apache.struts2.dispatcher.ServletActionRedirectResult;
import org.qi4j.api.composite.Composite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.library.struts2.example.Item;
import org.qi4j.library.struts2.support.add.ProvidesAddingOf;
import org.qi4j.library.struts2.support.add.ProvidesAddingOfMixin;

@Results( {
    @Result( name = "input", value = "/jsp/addItem.jsp" ),
    @Result( name = "error", value = "/jsp/addItem.jsp" ),
    @Result( name = "success", value = "listItems", type = ServletActionRedirectResult.class )
} )
@Mixins( ProvidesAddingOfMixin.class )
public interface AddItem
    extends ProvidesAddingOf<Item>, Composite
{
}
