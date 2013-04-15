package org.apache.tapestry5.services.javascript

import org.apache.tapestry5.internal.services.javascript.ModuleAssetRequestHandler
import org.apache.tapestry5.ioc.internal.QuietOperationTracker
import org.apache.tapestry5.ioc.test.TestBase
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ModuleAssetRequestHandlerTests extends TestBase {

    @Test(dataProvider = "unknownPaths")
    void "invalid extension is ignored"(extraPath) {
        def handler = new ModuleAssetRequestHandler(null, null, new QuietOperationTracker())

        assert handler.handleAssetRequest(null, null, extraPath) == false
    }

    @DataProvider
    Object[][] unknownPaths() {
        [
            "foo/bar.xyz",
            "foo",
            "foo/bar",
            ""
        ].collect({ it -> ["/modules/$it"] as Object[] }) as Object[][]
    }

    @Test
    void "returns false if no module is found"() {

        def manager = newMock ModuleManager

        expect(manager.findResourceForModule("foo/bar")).andReturn null

        replay()

        def handler = new ModuleAssetRequestHandler(manager, null, new QuietOperationTracker())

        assert handler.handleAssetRequest(null, null, "foo/bar.js") == false

        verify()
    }
}
