package org.apache.tapestry5.internal.services

import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test

class PathConstructorImplSpec extends Assert {

    @DataProvider
     Object[][] data() {
        return [
            ["", "", "/foo/bar", "/foo/bar"],
            ["", "myapp", "/myapp/foo/bar", "/myapp/foo/bar"],
            ["/ctx", "", "/ctx/foo/bar", "/foo/bar"],
            ["/ctx", "myapp", "/ctx/myapp/foo/bar", "/myapp/foo/bar"],
            // TAP5-2079
            ["/", "myapp", "/myapp/foo/bar", "/myapp/foo/bar"]
        ] as Object[][]
    }


    @Test(dataProvider = "data")
    void doTest(String contextPath, String appFolder, String expectedClientPath, String expectedDispatchPath) {
        def pc = new PathConstructorImpl(contextPath, appFolder)

        assert pc.constructClientPath("foo", "bar") == expectedClientPath
        assert pc.constructDispatchPath("foo", "bar") == expectedDispatchPath
    }
}
