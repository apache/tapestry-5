package org.apache.tapestry5.internal.services.assets

import org.apache.tapestry5.Asset
import org.apache.tapestry5.commons.Resource
import org.apache.tapestry5.internal.test.InternalBaseTestCase
import org.apache.tapestry5.services.AssetSource
import org.testng.annotations.Test

import spock.lang.Specification


class ChecksumPathSpec extends Specification  {

    def "TAP5-2713: don't throw exception on bad extraPath"() {

        when:
        def path = new ChecksumPath(null, basePath, extraPath)

        then:
        noExceptionThrown()
        nonExisting == (path.resourcePath == ChecksumPath.NON_EXISTING_RESOURCE)

        where:
        basePath | extraPath | nonExisting
        "META-INF/assets/tapestry" | "8ce2e6e2/tapestry.png" | false
        "META-INF/"                | "8ce2e6e2/tapestry.jpg" | false
        "META-INF/assets/tapestry" | "/valid.jpg"            | false // valid, because 'empty' checksum
        "META-INF/assets/tapestry" | "valid.jpg"             | true  // no checksum at all
        "META-INF/assets/tapestry" | ""                      | true  // nothing at all
        "META-INF/assets/tapestry" | "/"                     | true  // folder
    }
}
