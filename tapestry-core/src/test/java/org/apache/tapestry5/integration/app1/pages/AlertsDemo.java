package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.ReorderProperties;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.beaneditor.Width;
import org.apache.tapestry5.ioc.annotations.Inject;

/**
 *
 */
@ReorderProperties("severity,duration,message")
public class AlertsDemo
{
    @Inject
    private AlertManager alertManager;

    @Property
    @Validate("required")
    private Duration duration;

    @Property
    @Validate("required")
    private Severity severity;

    @Property
    @Validate("required")
    @Width(80)
    private String message;

    void onSuccessFromTraditional()
    {
        alertManager.info("Traditional form submission");
        alertManager.alert(duration, severity, message);
    }
}
