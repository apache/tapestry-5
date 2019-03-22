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

class NavigationSpec extends Specification {

    @Shared
    private TapestryTester tester

    def setupSpec() {
        tester = new TapestryTester("${package}", "app", "src/main/webapp", QaModule.class)
    }

    def "go to #pageName"() {
        when:
        Document document = tester.renderPage(pageName)
        String title = TapestryXPath.xpath("/html/head/title").selectSingleElement(document).getChildMarkup()

        then:
        title.startsWith(pageName)

        where:
        pageName << ["Index", "Login", "About", "Error404"]
    }
}
