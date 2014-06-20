package org.apache.tapestry5.integration.app5

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.apache.tapestry5.test.TapestryTestConfiguration
import org.testng.annotations.Test


@TapestryTestConfiguration(webAppFolder = "src/test/app5")
class ResourceCustomizerTests extends TapestryCoreTestCase {


    @Test
    void response_header_present() {

        openBaseURL()

        def src = getAttribute("//img[@id='banner']/@src")

        // println "src=$src"

        def url = new URL("$baseURL${src.substring(1)}")

        def connection = url.openConnection()

        connection.connect()

        def tag = connection.getHeaderField("X-Robots-Tag")

        // println connection.getHeaderFields()

        assertEquals tag, "noindex"

    }
}
