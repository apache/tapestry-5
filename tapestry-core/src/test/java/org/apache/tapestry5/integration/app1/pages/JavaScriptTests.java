package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Import;

@Import(library = {
        "context:qunit/qunit-1.9.0.js",
        "qunit-config.js",

        "test-dom.js",
        "test-messages.js",
        "test-validation.js",
        "test-utils.js",

        // This must come last:
        "qunit-driver.js"},
        stylesheet = "context:qunit/qunit-1.9.0.css")
public class JavaScriptTests
{
}
