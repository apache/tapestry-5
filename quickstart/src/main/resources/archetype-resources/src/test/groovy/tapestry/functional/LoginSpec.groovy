package ${package}.geb

import geb.Page
import geb.spock.GebSpec

import org.springframework.boot.test.context.SpringBootTest

import ${package}.AppConfiguration

class IndexPage extends Page {
    static url = 'http://localhost:8080/'
    static at = { title.startsWith('Index') }
}

class LoginPage extends Page {
    static url = 'http://localhost:8080/login'
    static at = { title.startsWith('Login') }
    static content = {
        email { $('#email') }
        password { $('#password') }
        submit { $("#login input[type=submit]") }
    }
}

@SpringBootTest(classes = AppConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class LoginSpec extends GebSpec {
    def 'go to login'() {
        when:
        to LoginPage

        then:
        at LoginPage
    }

    def 'success login'() {
        setup:
        to LoginPage

        when:
        email().value "users@tapestry.apache.org"
        password().value "Tapestry5"
        submit().click()

        then:
        at IndexPage
    }

    def 'fail login'() {
        setup:
        to LoginPage

        when:
        email().value "users@tapestry.apache.org"
        password().value "xxx"
        submit().click()

        then:
        at LoginPage
    }
}
