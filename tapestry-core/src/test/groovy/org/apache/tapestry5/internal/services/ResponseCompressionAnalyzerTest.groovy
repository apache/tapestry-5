package org.apache.tapestry5.internal.services

import org.apache.tapestry5.ioc.test.TestBase
import org.apache.tapestry5.services.ResponseCompressionAnalyzer
import org.testng.annotations.Test

import javax.servlet.http.HttpServletRequest

class ResponseCompressionAnalyzerTest extends TestBase {

  @Test
  void "HTTP/1.0 protocol disables gzip compression"() {

    HttpServletRequest request = newMock(HttpServletRequest)

    expect(request.getProtocol()).andReturn("HTTP/1.0").once()

    replay()

    ResponseCompressionAnalyzer rca = new ResponseCompressionAnalyzerImpl(request, null, null, true)

    assert rca.isGZipSupported() == false

    verify()
  }
}
