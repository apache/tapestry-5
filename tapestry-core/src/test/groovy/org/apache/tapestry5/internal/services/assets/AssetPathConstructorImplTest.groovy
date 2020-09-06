package org.apache.tapestry5.internal.services.assets

import org.apache.tapestry5.http.services.BaseURLSource
import org.apache.tapestry5.http.services.Request
import org.apache.tapestry5.internal.services.IdentityAssetPathConverter
import org.apache.tapestry5.services.PathConstructor
import org.apache.tapestry5.services.assets.AssetChecksumGenerator
import org.apache.tapestry5.services.assets.CompressionStatus
import org.apache.tapestry5.services.assets.StreamableResource
import org.apache.tapestry5.test.ioc.TestBase
import org.testng.annotations.Test

class AssetPathConstructorImplTest extends TestBase {

    def pathConverter = new IdentityAssetPathConverter()

    @Test
    void "construct an asset path with no application folder"() {

        def pc = newMock(PathConstructor)

        def gen = newMock(AssetChecksumGenerator)

        def r = newMock(StreamableResource)

        expect(pc.constructClientPath("assets", "")).andReturn("/assets/")

        expect(r.compression).andReturn(CompressionStatus.COMPRESSED)

        expect(r.checksum).andReturn("abc")

        replay()

        def apc = new AssetPathConstructorImpl(null, null, false, "assets", pc, pathConverter)

        assert apc.constructAssetPath("virt", "extra.png", r) == "/assets/virt/zabc/extra.png"

        verify()
    }

    @Test
    void "fully qualified path has base URL prepended"() {
        def request = newMock(Request)

        def baseURLSource = newMock(BaseURLSource)

        def pc = newMock(PathConstructor)

        def r = newMock(StreamableResource)

        expect(pc.constructClientPath("assets", "")).andReturn("/assets/")

        expect(request.secure).andReturn(false)
        expect(baseURLSource.getBaseURL(false)).andReturn("http://localhost:8080")

        expect(r.compression).andReturn(CompressionStatus.NOT_COMPRESSABLE)

        expect(r.checksum).andReturn("abc")

        replay()

        def apc = new AssetPathConstructorImpl(request, baseURLSource, true, "assets", pc, pathConverter)

        assert apc.constructAssetPath("virt", "icon.gif", r) == "http://localhost:8080/assets/virt/abc/icon.gif"

        verify()

    }
}
