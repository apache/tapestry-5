package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ComponentInsideBlockDemo
{
    @Inject
    private AlertManager mgr;

    void onActionFromGoForBroke() {
        mgr.info("Go For Broke Clicked");
    }
}
