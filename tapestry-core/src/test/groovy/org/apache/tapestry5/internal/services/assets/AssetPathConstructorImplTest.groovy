package org.apache.tapestry5.internal.services.assets

import org.apache.tapestry5.ioc.Resource
import org.apache.tapestry5.ioc.test.TestBase
import org.apache.tapestry5.services.BaseURLSource
import org.apache.tapestry5.services.PathConstructor
import org.apache.tapestry5.services.Request
import org.apache.tapestry5.services.assets.AssetChecksumGenerator
import org.testng.annotations.Test

class AssetPathConstructorImplTest extends TestBase {

  @Test
  void "construct an asset path with no application folder"() {

    def request = newMock(Request)

    def baseURLSource = newMock(BaseURLSource)

    def pc = newMock(PathConstructor)

    def gen = newMock(AssetChecksumGenerator)

    def virtExtra = newMock(Resource)

    def virtb = newMock(Resource)

    expect(pc.constructClientPath("assets", "")).andReturn("/assets/")

    expect(gen.generateChecksum(virtExtra)).andReturn("abc")

    replay()

    def apc = new AssetPathConstructorImpl(request, baseURLSource, false, "assets", null, pc, pathConverter)

    assert apc.constructAssetPath("virt", "extra.png", virtExtra) == "/assets/virt/abc/extra.png"

    verify()
  }

  @Test
  void "fully qualified path has base URL prepended"() {
    def request = newMock(Request)
    def baseURLSource = newMock(BaseURLSource)

    def pc = newMock(PathConstructor)

    def gen = newMock(AssetChecksumGenerator)

    def r = newMock(Resource)

    expect(pc.constructClientPath("assets", "")).andReturn("/assets/")

    expect(request.secure).andReturn(false)
    expect(baseURLSource.getBaseURL(false)).andReturn("http://localhost:8080")

    expect(gen.generateChecksum(r)).andReturn("911")

    replay()

    def apc = new AssetPathConstructorImpl(request, baseURLSource, true, "assets", null, pc, pathConverter)

    assert apc.constructAssetPath("virt", "icon.gif", r) == "http://localhost:8080/assets/virt/911/icon.gif"

    verify()

  }
}
