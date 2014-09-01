package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.integration.app1.base.OverrideEventHandlerDemoBaseClass;

public class OverrideEventHandlerDemo extends OverrideEventHandlerDemoBaseClass
{
    @Override
    public Object onActionFromTrigger()
    {
        add("sub-class");
        add("DONE");

        return null;
    }
}
