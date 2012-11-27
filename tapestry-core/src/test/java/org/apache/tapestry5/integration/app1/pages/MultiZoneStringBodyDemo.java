package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;

public class MultiZoneStringBodyDemo
{

    @Property
    private String[] list = {
            "Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten"
    };

    @Property
    private int index;

    @Property
    private String item;

    @InjectComponent
    private Zone wholeLoopZone;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    public String getRowId()
    {
        return "row-" + index;
    }

    void onClick(int i)
    {
        while (i < list.length)
        {
            ajaxResponseRenderer.addRender("row-" + (i), Integer.toString(i) + " is the integer value");

            ++i;
        }
    }

    public Block onReset()
    {
        return wholeLoopZone.getBody();
    }

}
