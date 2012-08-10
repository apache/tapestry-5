package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.json.JSONArray
import org.apache.tapestry5.json.JSONObject
import org.apache.tapestry5.services.javascript.InitializationPriority
import org.apache.tapestry5.services.javascript.StylesheetLink
import org.apache.tapestry5.services.javascript.StylesheetOptions
import org.testng.Assert
import org.testng.annotations.Test

class PartialMarkupDocumentLinkerTest extends Assert {

    @Test(expectedExceptions = UnsupportedOperationException)
    void script() {
        PartialMarkupDocumentLinker linker = new PartialMarkupDocumentLinker()

        linker.addScript(InitializationPriority.NORMAL, "foo();")

        throw new IllegalStateException("Unreachable code.")
    }

    @Test
    void script_link() {
        PartialMarkupDocumentLinker linker = new PartialMarkupDocumentLinker()

        linker.addLibrary("foo.js")
        linker.addLibrary("bar.js")

        JSONObject reply = new JSONObject()

        linker.commit(reply)

        assert reply.toCompactString() == /{"_tapestry":{"libraries":["foo.js","bar.js"]}}/

    }

    @Test
    void stylesheet_link() {
        PartialMarkupDocumentLinker linker = new PartialMarkupDocumentLinker()

        linker.addStylesheetLink(new StylesheetLink("foo.css", new StylesheetOptions("print")))
        linker.addStylesheetLink(new StylesheetLink("bar.css"))

        JSONObject reply = new JSONObject()

        linker.commit(reply)

        JSONObject expected = new JSONObject(/{"_tapestry":{'stylesheets':[{'href':'foo.css', 'media':'print'}, {'href':'bar.css'}]}}/)

        assert reply == expected
    }

    @Test
    void set_initialization() {
        PartialMarkupDocumentLinker linker = new PartialMarkupDocumentLinker()

        JSONObject reply = new JSONObject()

        linker.addInitialization(InitializationPriority.NORMAL, "core/init", "order", new JSONArray("['normal']"))
        linker.addInitialization(InitializationPriority.IMMEDIATE, "core/init", "order", new JSONArray("['immediate']"))

        linker.commit(reply)

        JSONArray expected = new JSONArray("[['core/init:order', 'immediate'], ['core/init:order', 'normal']]")

        assert reply.in("_tapestry").get("inits") == expected
    }
}
