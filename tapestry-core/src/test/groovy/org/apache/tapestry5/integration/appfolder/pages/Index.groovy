package org.apache.tapestry5.integration.appfolder.pages

import org.apache.tapestry5.alerts.AlertManager
import org.apache.tapestry5.ioc.annotations.Inject

class Index
{
    @Inject
    private AlertManager alertManager

    void onActionFromShowAlert()
    {
        alertManager.info "index page alert"
    }
}
