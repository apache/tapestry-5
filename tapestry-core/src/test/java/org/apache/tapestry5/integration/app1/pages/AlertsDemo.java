package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.AlertStorage;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.beaneditor.ReorderProperties;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.beaneditor.Width;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;

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
    
    @Property
    private boolean markup;

    @Persist(PersistenceConstants.FLASH)
    @Property
    private boolean showErrorComponent;

    @InjectComponent
    private Zone formZone;

    @SessionState
    private AlertStorage storage;

    @Property
    private boolean redirectToIndex;

    void onSuccessFromTraditional()
    {
        alertManager.info("Traditional form submission");
        alertManager.alert(duration, severity, message, markup);
    }

    Object onSuccessFromAjax()
    {
        alertManager.info("Ajax form submission");
        alertManager.alert(duration, severity, message, markup);

        if (redirectToIndex)
        {
            return Index.class;
        }

        return formZone.getBody();
    }

    void onActionFromReset()
    {
        storage.dismissAll();
    }

    void onShowErrorComponent()
    {
        showErrorComponent = true;
    }
}
