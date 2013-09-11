package org.apache.tapestry5.integration.app1

import org.testng.annotations.Test

class PageLoadErrorTests extends App1TestCase
{

    @Test
    void check_correct_reporting_of_missing_embedded_component_with_InjectComponent()
    {
        openLinks "Missing Embedded Component"

        assertTextPresent "Unable to inject component into field missing of class org.apache.tapestry5.integration.app1.pages.MissingEmbeddedComponent", "Component MissingEmbeddedComponent does not contain embedded component 'missing'."
    }
}
