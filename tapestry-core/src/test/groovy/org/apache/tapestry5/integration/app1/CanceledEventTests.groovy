package org.apache.tapestry5.integration.app1

import org.testng.annotations.Test

class CanceledEventTests extends App1TestCase
{
    @Test
    void cancel_button()
    {
        openLinks "Canceled Event Demo"

        clickAndWait SUBMIT

        assertFirstAlert "Form was canceled."
    }

    @Test
    void cancel_link()
    {
        openLinks "Canceled Event Demo"

        clickAndWait "link=Cancel Form"

        assertFirstAlert "Form was canceled."
    }
}
