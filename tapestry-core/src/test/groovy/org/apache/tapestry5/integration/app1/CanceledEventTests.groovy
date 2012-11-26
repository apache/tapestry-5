package org.apache.tapestry5.integration.app1

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.testng.annotations.Test

class CanceledEventTests extends TapestryCoreTestCase
{
    @Test
    void cancel_button()
    {
        openLinks "Canceled Event Demo"

        sleep 100

        clickAndWait SUBMIT

        assertFirstAlert "Form was canceled."
    }

    @Test
    void cancel_link()
    {
        openLinks "Canceled Event Demo"

        sleep 100

        clickAndWait "link=Cancel Form"

        assertFirstAlert "Form was canceled."
    }
}
