package org.apache.tapestry5.services.javascript;


import org.apache.tapestry5.internal.services.javascript.ModuleAssetRequestHandler
import org.apache.tapestry5.ioc.internal.QuietOperationTracker
import org.apache.tapestry5.ioc.test.TestBase
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class ModuleAssetRequestHandlerTest extends TestBase {

    @Test(dataProvider = "unknownPaths")
    void "invalid extension is ignored"(path) {
        def handler = new ModuleAssetRequestHandler(null, null, null)

        assertEquals handler.handleAssetRequest(null, null, path), false
    }

    @DataProvider
    Object[][] unknownPaths() {
        [
            "foo/bar.xyz",
            "foo",
            "foo/bar",
            "/",
            ""
        ].collect({ it -> [it] as Object[]}) as Object[][]
    }

    @Test
    void "returns false if no module is found"() {

        def manager = newMock(ModuleManager)

        def handler = new ModuleAssetRequestHandler(manager, null, new QuietOperationTracker())

        expect(manager.findResourceForModule("foo/bar")).andReturn null

        replay()

        assertEquals handler.handleAssetRequest(null, null, "foo/bar.js"), false

        verify()
    }
}
