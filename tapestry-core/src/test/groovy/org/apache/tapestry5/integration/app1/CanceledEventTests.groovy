package org.apache.tapestry5.integration.app1

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.testng.annotations.Test

class CanceledEventTests extends TapestryCoreTestCase
{
    @Test
    void cancel_button()
    {
        openLinks "Canceled Event Demo"

        clickAndWait SUBMIT

        assertText "css=.t-alert-container", "Form was canceled."
    }

    @Test
    void cancel_link()
    {
        openLinks "Canceled Event Demo"

        clickAndWait "link=Cancel Form"

        assertText "css=.t-alert-container", "Form was canceled."
    }
}
