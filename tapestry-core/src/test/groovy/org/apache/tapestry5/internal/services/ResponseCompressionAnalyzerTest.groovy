package org.apache.tapestry5.internal.services

import javax.servlet.http.HttpServletRequest

import org.apache.tapestry5.http.internal.services.ResponseCompressionAnalyzerImpl
import org.apache.tapestry5.http.services.ResponseCompressionAnalyzer
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.test.ioc.TestBase
import org.testng.annotations.Test

class ResponseCompressionAnalyzerTest extends TestBase {

  @Test
  void "HTTP 1_0 protocol disables gzip compression"() {

    HttpServletRequest request = newMock(HttpServletRequest)

    expect(request.getProtocol()).andReturn("HTTP/1.0").once()

    replay()

    ResponseCompressionAnalyzer rca = new ResponseCompressionAnalyzerImpl(request, true, null)

    assert rca.isGZipSupported() == false

    verify()
  }

  @Test
  //TAP5-2264
  void "InternalConstants#SUPPRESS_COMPRESSION attribute disables gzip compression"() {

    HttpServletRequest request = newMock(HttpServletRequest)

    expect(request.getProtocol()).andReturn("HTTP/1.1").once()
    expect(request.getAttribute(InternalConstants.SUPPRESS_COMPRESSION)).andReturn("yes").once()

    replay()

    ResponseCompressionAnalyzer rca = new ResponseCompressionAnalyzerImpl(request, true, null)

    assert rca.isGZipSupported() == false

    verify()
  }
}
