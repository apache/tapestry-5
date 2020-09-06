package org.apache.tapestry5.integration.app1.pages

import org.apache.tapestry5.Field
import org.apache.tapestry5.ValidationTracker
import org.apache.tapestry5.alerts.AlertManager
import org.apache.tapestry5.annotations.Environmental
import org.apache.tapestry5.annotations.InjectComponent
import org.apache.tapestry5.beaneditor.Validate
import org.apache.tapestry5.ioc.annotations.Inject
import org.apache.tapestry5.corelib.components.Zone

/**
 * Used to demonstrate Ajax updates to a form including decoration.
 */
public class ZoneFormDemo {

    @Validate("required")
    String handle

    @Validate("required")
    Integer rating

    @InjectComponent
    Zone zone

    @Inject
    AlertManager alertManager

    @Environmental
    ValidationTracker tracker

    @InjectComponent("handle")
    Field handleField

    @InjectComponent("rating")
    Field ratingField

    def onValidateFromForm() {

        if (handle.startsWith("x")) {
            tracker.recordError(handleField, "No X here, please.")
        }

        if (rating < 3) {
            tracker.recordError(ratingField, "Too stringy!")
        }

        if (rating > 8 && !(handle.startsWith("hl"))) {
            tracker.recordError(ratingField, "Too nice!")
        }
    }

    def onSuccessFromForm() {

        alertManager.info("Rated '${handle}' as ${rating}.  Rate another!")

        handle = null
        rating = null
    }

    def onSubmitFromForm() {
        zone.body
    }

}
