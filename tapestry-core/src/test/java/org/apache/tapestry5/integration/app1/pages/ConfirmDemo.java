package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ConfirmDemo
{
    @Inject
    AlertManager alertManager;

    void onActionFromConfirmed()
    {
        alertManager.info("Action was confirmed.");
    }
}
