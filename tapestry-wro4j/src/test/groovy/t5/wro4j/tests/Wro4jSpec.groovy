package t5.wro4j.tests

import geb.spock.GebSpec
import org.apache.tapestry5.test.Jetty7Runner
import spock.lang.Shared

class Wro4jSpec extends GebSpec {

    @Shared
    def runner;

    def setupSpec() {
        runner = new Jetty7Runner("src/test/webapp", "/", 8080, 8081);

        runner.start()
    }

    def cleanupSpec() {
        runner.stop()
    }

    def "CoffeeScript compilation"() {

        when:

        // Open index page
        go()

        waitFor { !$("body[data-page-initialized]").empty }

        then:

        // This text is buried inside a CoffeeScript file; for it to be present in the DOM
        // means that the CoffeeScript was compiled to JS and executed.
        $("#banner").text().trim() == "Index module loaded, bare!"
    }

    def "Less compilation"() {

        when:

        go()

        waitFor { !$("body[data-page-initialized]").empty }

        $(".navbar .dropdown-toggle").click()

        $(".navbar .dropdown-menu a", text: "MultiLess").click()

        waitFor { !$(".demo").empty }

        then:

        $(".demo").jquery.css("background-color") == "rgb(179, 179, 255)"
    }
}
