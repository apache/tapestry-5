package org.apache.tapestry5.integration.app1

import org.openqa.selenium.By
import org.testng.annotations.Test


class AjaxGroovyTests extends App1TestCase {

    /** TAP5-2231 */
    @Test
    void radio_buttons_in_ajax_form() {

        openLinks "Ajax Radio Demo"
        webDriver.findElements(By.cssSelector('label')).find{it.text.contains('It')}.click()
        def labelTemp = webDriver.findElements(By.cssSelector('label')).find{it.text.contains('Temp')}
        scrollIntoView(labelTemp)
        labelTemp.click()

        click SUBMIT

        waitForElementToAppear "selected-department"

        assertText "selected-department", "IT"
        assertText "selected-position", "TEMP"
    }

    /** TAP5-1404 */
    @Test
    void async_link_update() {
        openLinks "Async Links and Forms Demo"

        click "link=Link Update"

        waitForAjaxRequestsToComplete()

        assertText "css=#target > p", "You clicked the link."
    }

    @Test
    void async_form_submit() {
        openLinks "Async Links and Forms Demo"

        click SUBMIT

        waitForAjaxRequestsToComplete()

        assertText "css=#target > p", "You submitted the form."
    }

    @Test
    void ajax_form_validation() {
        openLinks "Ajax Validation"

        click SUBMIT

        waitForAjaxRequestsToComplete()

        assertText "css=.form-group.has-error .help-block", "Server-side validation error."

    }

}
