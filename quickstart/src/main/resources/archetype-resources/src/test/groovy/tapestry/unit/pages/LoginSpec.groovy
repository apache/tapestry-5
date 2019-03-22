package ${package}.pages

import com.formos.tapestry.testify.core.ForComponents
import com.formos.tapestry.testify.core.TapestryTester
import com.formos.tapestry.xpath.TapestryXPath

import org.apache.tapestry5.alerts.AlertManager
import org.apache.tapestry5.dom.Document
import org.apache.tapestry5.dom.Element
import org.apache.tapestry5.corelib.components.Form
import org.apache.tapestry5.corelib.components.TextField
import org.apache.tapestry5.corelib.components.PasswordField

import static spock.mock.MockingApi.Mock
import static spock.mock.MockingApi.Stub
import spock.lang.Shared
import spock.lang.Specification

import ${package}.services.QaModule

class LoginSpec extends Specification {

    @Shared
    private TapestryTester tester

    @ForComponents
    private AlertManager alertManager

    def setupSpec() {
        tester = new TapestryTester("${package}", "app", "src/main/webapp", QaModule.class)
    }

    def setup() {
        tester.injectInto(this);
        alertManager = Mock(AlertManager)
        tester.collectForComponentsFrom(this);
    }

    def cleanup() {
        tester.endTest()
    }

    def "login success"() {
        given:
        Document document = tester.renderPage("Login");
        Element form = TapestryXPath.xpath("//form").selectSingleElement(document)

        when:
        def fields = [email: 'users@tapestry.apache.org', password: 'Tapestry5']
        Document result = tester.submitForm(form, fields);

        and:
        String title = TapestryXPath.xpath("/html/head/title").selectSingleElement(result).getChildMarkup()

        then:
        title.startsWith("Index")
        1 * alertManager.success(_)

    }

    def "login error"() {
        given:
        Document document = tester.renderPage("Login");
        Element form = TapestryXPath.xpath("//form").selectSingleElement(document)

        when:
        def fields = [email: 'xxx', password: 'xxx']
        Document result = tester.submitForm(form, fields);

        and:
        String title = TapestryXPath.xpath("/html/head/title").selectSingleElement(result).getChildMarkup()

        then:
        title.startsWith("Login")
        1 * alertManager.error(_)
    }
}
