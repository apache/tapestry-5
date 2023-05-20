package t5.webresources.tests

import org.apache.tapestry5.internal.webresources.CssCompressor

import spock.lang.Issue;
import spock.lang.Specification;

class CssCompressorSpec extends Specification {

    @Issue('TAP5-2524')
    def "minify CSS with keyframes "() {
        given:
        def css = '''@keyframes anim {
    0% { opacity: 0; }
  100% { opacity: 1; }
}'''
        def expected = "@keyframes anim{0%{opacity:0}100%{opacity:1}}" 

        when:
        def result = CssCompressor.compress(css)

        then:
        result == expected
    }

    @Issue('TAP5-2753')
    def "preserve space for calc operators"() {
        given:
        def is = CssCompressorSpec.class.getResourceAsStream("/t5/webresources/css/tap5-2753.css")
        def expected = CssCompressorSpec.class.getResourceAsStream("/t5/webresources/css/tap5-2753.css.min").text.strip()

        when:
        def result = CssCompressor.compress(is)

        then:
        result == expected
    }

    def "bootstrap.css integry check"() {
        given:
        def is = CssCompressorSpec.class.getResourceAsStream("/t5/webresources/css/bootstrap.css")
        def expected = CssCompressorSpec.class.getResourceAsStream("/t5/webresources/css/bootstrap.css.min").text.strip()

        when:
        def result = CssCompressor.compress(is)

        then:
        result == expected
    }

    def "yui compressor test '#rawFile'"() {
        given:
        def is = CssCompressorSpec.class.getResourceAsStream("/t5/webresources/css/yui/$rawFile")
        def expected = CssCompressorSpec.class.getResourceAsStream("/t5/webresources/css/yui/${rawFile}.min").text.strip()

        when:
        def result = CssCompressor.compress(is)

        then:
        result == expected

        where:
        rawFile << [
        "background-position.css",
        "border-none.css",
        "box-model-hack.css",
        "bug2527974.css",
        "bug2527991.css",
        "bug2527998.css",
        "bug2528034.css",
        "bug-flex.css",
        "bug-nested-pseudoclass.css",
        "bug-preservetoken-calc.css",
        "charset-media.css",
        "color.css",
        "color-keyword.css",
        "color-simple.css",
        "comment.css",
        "concat-charset.css",
        "dataurl-base64-doublequotes.css",
        "dataurl-base64-eof.css",
        "dataurl-base64-linebreakindata.css",
        "dataurl-base64-noquotes.css",
        "dataurl-base64-singlequotes.css",
        "dataurl-base64-twourls.css",
        "dataurl-dbquote-font.css",
        "dataurl-nonbase64-doublequotes.css",
        "dataurl-nonbase64-noquotes.css",
        "dataurl-nonbase64-singlequotes.css",
        "dataurl-noquote-multiline-font.css",
        "dataurl-realdata-doublequotes.css",
        "dataurl-realdata-noquotes.css",
        "dataurl-realdata-singlequotes.css",
        "dataurl-realdata-yuiapp.css",
        "dataurl-singlequote-font.css",
        "decimals.css",
        "dollar-header.css",
        "font-face.css",
        // "hsla-issue81.css.FAIL",
        "ie5mac.css",
        "ie-backslash9-hack.css",
        "issue151.css",
        // "issue172.css.FAIL",
        "issue180.css",
        "issue205.css",
        "issue221.css",
        "issue222.css",
        "issue-59.css",
        "lowercasing.css",
        "media-empty-class.css",
        "media-multi.css",
        "media-test.css",
        "old-ie-filter-matrix.css",
        "opacity-filter.css",
        "opera-pixel-ratio.css",
        "pointzeros.css",
        "preserve-case.css",
        "preserve-important.css",
        "preserve-new-line.css",
        "preserve-strings.css",
        "pseudo.css",
        "pseudo-first.css",
        // "rgb-issue81.css.FAIL",
        "special-comments.css",
        "star-underscore-hacks.css",
        "string-in-comment.css",
        "webkit-transform.css",
        "zeros.css"
        ]
    }
}
