package org.apache.tapestry5.integration.app1.pages;


import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.ioc.annotations.Inject;

/**
 * Tests that the canceled event works correctly.
 */
public class CanceledEventDemo
{
    @Inject
    private AlertManager alertManager;

    @Property
    @Validate("required")
    private String requiredText;

    Object onCanceledFromForm()
    {
        alertManager.info("Form was canceled.");

        return this;
    }

}
