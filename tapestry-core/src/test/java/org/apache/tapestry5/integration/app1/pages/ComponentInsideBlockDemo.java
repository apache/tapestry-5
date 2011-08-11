package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ComponentInsideBlockDemo
{
    @Inject
    private AlertManager mgr;

    @Persist
    private boolean which;

    @Inject
    private Block zen, never;

    public Block getBlockToRender()
    {
        return which ? never : zen;
    }

    void pageReset()
    {
        which = false;
    }

    void onActionFromGoForBroke()
    {
        mgr.info("Go For Broke Clicked");

        which = true;
    }

    @OnEvent(component = "neverForm", value = EventConstants.SUBMIT)
    void neverFormSubmitted()
    {
        mgr.info("Never Form Submitted");

        which = false;
    }
}
