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
    
    public String getTitle()
    {
        return "some<span><script>window.alert('ouch1');</script></span><em onclick=\"window.alert('ouch2')\">thing</em> else";
    }
}
