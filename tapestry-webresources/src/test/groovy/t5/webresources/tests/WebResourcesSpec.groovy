package t5.webresources.tests

import geb.spock.GebReportingSpec
import org.apache.tapestry5.test.JettyRunner
import spock.lang.Shared

class WebResourcesSpec extends GebReportingSpec {

    @Shared
    def runner;

    def setupSpec() {
        runner = new JettyRunner("src/test/webapp", "/", 8080, 8081);

        runner.start()
    }

    def cleanupSpec() {
        if (runner != null)
            runner.stop()
    }

    def "CoffeeScript compilation"() {

        when:

        // Open index page
        go()

        waitFor { $('body').@'data-page-initialized' == 'true' }

        then:

        // This text is buried inside a CoffeeScript file; for it to be present in the DOM
        // means that the CoffeeScript was compiled to JS and executed.
        $("#banner").text().trim() == "Index module loaded, bare!"
    }

    def "Less compilation"() {

        when:

        go()

        waitFor { $('body').@'data-page-initialized' == 'true' }

        // Because the CoffeeScript may already be pre-compiled, it can outrace the Less compilation.
        // For some reason, the navbar is invisible (at least to Selenium) until the CSS loads.

        // waitFor { $(".navbar .dropdown-toggle").visible() }

        $(".navbar .dropdown-toggle").click()

        $(".navbar .dropdown-menu a", text: "MultiLess").click()

        waitFor { !$(".demo").empty }

        then:

        $(".demo").css("background-color") == "rgb(179, 179, 255)"
    }
}
