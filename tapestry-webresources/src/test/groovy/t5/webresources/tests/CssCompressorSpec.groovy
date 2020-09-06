package t5.webresources.tests

import geb.spock.GebReportingSpec

import org.apache.tapestry5.internal.webresources.CssCompressor
import org.apache.tapestry5.test.JettyRunner

import spock.lang.Issue;
import spock.lang.Shared
import spock.lang.Specification;

class CssCompressorSpec extends Specification {

    @Issue('TAP5-2524')
    def "minify CSS with keyframes "() {
        setup:
        def css = '''@keyframes anim {
    0% { opacity: 0; }
  100% { opacity: 1; }
}'''
        StringWriter writer = new StringWriter()

        when:
        new CssCompressor(new StringReader(css)).compress(writer, -1)

        then:
        writer.toString() == '''@keyframes anim{0%{opacity:0}100%{opacity:1}}'''
    }
}
