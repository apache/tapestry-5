package org.apache.tapestry5.integration.app1.pages

import org.apache.tapestry5.alerts.AlertManager
import org.apache.tapestry5.ioc.annotations.Inject

class DateFieldValidationDemo {

    Date date;

    @Inject
    private AlertManager alertManager;

    void onSuccess() {
        alertManager.success(String.format("Submitted date as '%s'.", date));
    }
}
