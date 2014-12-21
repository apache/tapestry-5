package org.apache.tapestry5.integration.pagelevel;

import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.test.PageTester;
import org.testng.Assert;
import org.testng.annotations.Test;

public class OverrideMethodsTest extends Assert {
    /** TAP5-901 */
    @Test
    public void override_abstract_methods() {
        PageTester tester = new PageTester(TestConstants.APP2_PACKAGE, TestConstants.APP2_NAME);
        try {
            Document doc = tester.renderPage("OverrideAbstractMethods");
            assertEquals("6", doc.getElementById("length").getChildMarkup());
        } finally {
            tester.shutdown();
        }
    }

}
