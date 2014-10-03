package org.apache.tapestry5.integration.app1.pages

import org.apache.tapestry5.Field
import org.apache.tapestry5.ValidationTracker
import org.apache.tapestry5.annotations.Environmental
import org.apache.tapestry5.annotations.InjectComponent
import org.apache.tapestry5.annotations.Property
import org.apache.tapestry5.corelib.components.Zone


class AjaxValidationDemo {

    @Property
    private String name;

    @Environmental
    @Property
    private ValidationTracker tracker;

    @InjectComponent("name")
    private Field nameField;

    @InjectComponent
    private Zone formZone;

    def onValidateFromForm() {
        tracker.recordError nameField, "Server-side validation error."
    }

    def onSubmitFromForm() { formZone.body }
}
