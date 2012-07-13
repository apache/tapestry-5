package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Import;

@Import(library = {
        "context:qunit/qunit-1.9.0.js", "test-pubsub.js"}, stylesheet = "context:qunit/qunit-1.9.0.css")
public class JavaScriptTests
{
}
