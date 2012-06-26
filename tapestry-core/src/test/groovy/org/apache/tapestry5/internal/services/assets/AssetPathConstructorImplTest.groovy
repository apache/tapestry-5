package org.apache.tapestry5.internal.services.assets

import org.apache.tapestry5.ioc.test.TestBase
import org.apache.tapestry5.services.Request
import org.testng.annotations.Test
import org.apache.tapestry5.services.BaseURLSource

class AssetPathConstructorImplTest extends TestBase {

  @Test
  void "construct an asset path with no application folder"() {

    def request = newMock(Request)

    def apc = new AssetPathConstructorImpl(request, null, "123", "", false, "/assets/")

    expect(request.contextPath).andReturn("").atLeastOnce()

    replay()

    assert apc.constructAssetPath("virt", "extra") == "/assets/123/virt/extra"

    assert apc.constructAssetPath("virtb", "") == "/assets/123/virtb"

    verify()
  }

  @Test
  void "construct an asset path with no extra path"() {

    def request = newMock(Request)

    def apc = new AssetPathConstructorImpl(request, null, "123", "", false, "/assets/")

    expect(request.contextPath).andReturn("")

    replay()

    assert apc.constructAssetPath("virtb", "") == "/assets/123/virtb"

    verify()
  }
  @Test
  void "construct asset path with an application folder"() {
    def request = newMock(Request)

    def apc = new AssetPathConstructorImpl(request, null, "123", "myapp", false, "/assets/")

    expect(request.contextPath).andReturn("").atLeastOnce()

    replay()

    assert apc.constructAssetPath("virt", "extra") == "/myapp/assets/123/virt/extra"

    verify()
  }

  @Test
  void "construct asset path with a context path"() {
    def request = newMock(Request)

    def apc = new AssetPathConstructorImpl(request, null, "123", "myapp", false, "/assets/")

    expect(request.contextPath).andReturn("/ctx").atLeastOnce()

    replay()

    assert apc.constructAssetPath("virt", "extra") == "/ctx/myapp/assets/123/virt/extra"

    verify()
  }

  @Test
  void "fully qualified path has base URL prepended"() {
    def request = newMock(Request)
    def baseURLSource = newMock(BaseURLSource)

    def apc = new AssetPathConstructorImpl(request, baseURLSource, "123", "myapp", true, "/assets/")

    expect(request.secure).andReturn(false)
    expect(request.contextPath).andReturn("")
    expect(baseURLSource.getBaseURL(false)).andReturn("http://localhost:8080")

    replay()

    assert apc.constructAssetPath("virt", "extra") == "http://localhost:8080/myapp/assets/123/virt/extra"

    verify()

  }
}
