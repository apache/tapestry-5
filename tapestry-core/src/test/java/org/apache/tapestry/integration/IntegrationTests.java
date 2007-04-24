// Copyright 2006, 2007 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.integration;

import static java.lang.String.format;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.tapestry.corelib.mixins.RenderDisabled;
import org.apache.tapestry.internal.services.InjectComponentWorker;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.internal.util.ClasspathResource;
import org.openqa.selenium.server.SeleniumServer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * Note: If these tests fail with BindException when starting Jetty, it could be Skype. At least on
 * my system, Skype is listening on localhost:80.
 */
@Test(timeOut = 50000, sequential = true, groups =
{ "integration" })
public class IntegrationTests extends Assert
{
    /** 60 seconds */
    public static final String PAGE_LOAD_TIMEOUT = "600000";

    private static final int JETTY_PORT = 9999;

    private static final String BASE_URL = format("http://localhost:%d/", JETTY_PORT);

    private JettyRunner _jettyRunner;

    private Selenium _selenium;

    private SeleniumServer _server;

    @Test
    public void any_component()
    {

        _selenium.open(BASE_URL);

        clickAndWait("link=AnyDemo");

        assertSourcePresent(
                "<span class=\"title\">Page Title</span>",
                "<div class=\"heading\">Heading</div>",
                "<h2 class=\"section\">Section</h2>",
                "<li id=\"item\">Item (item)</li>",
                "<li id=\"item_0\">Item (item_0)</li>",
                "<li id=\"item_1\">Item (item_1)</li>");
    }

    @Test
    public void assets() throws Exception
    {
        _selenium.open(BASE_URL);
        clickAndWait("link=AssetDemo");

        assertText("//img[@id='img']/@src", "/images/tapestry_banner.gif");

        // This doesn't prove that the image shows up in the client browser (it does, but
        // it could just as easily be a broken image). Haven't figured out how Selenium
        // allows this to be verified. Note that the path below represents some aliasing
        // of the raw classpath resource path.

        assertText("//img[@id='img_0']/@src", "/assets/app1/pages/tapestry-button.png");

        // Read the byte stream for the asset and compare to the real copy.

        URL url = new URL("http", "localhost", JETTY_PORT, "/assets/app1/pages/tapestry-button.png");

        byte[] downloaded = readContent(url);

        Resource classpathResource = new ClasspathResource(
                "org/apache/tapestry/integration/app1/pages/tapestry-button.png");

        byte[] actual = readContent(classpathResource.toURL());

        assertEquals(downloaded, actual);
    }

    @Test
    public void basic_parameters() throws Exception
    {

        // OK ... this could be a separate test, but for efficiency, we'll mix it in here.
        // It takes a while to start up Selenium RC (and a Firefox browser).

        _selenium.open(BASE_URL);

        clickAndWait("link=Count Page");

        assertTextPresent("Ho! Ho! Ho!");
    }

    /**
     * Tests the ability to inject a Block, and the ability to use the block to control rendering.
     */
    @Test
    public void block_rendering() throws Exception
    {
        _selenium.open(BASE_URL);
        clickAndWait("link=BlockDemo");

        assertTextPresent("[]");

        _selenium.select("//select[@id='blockName']", "fred");
        _selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);

        assertTextPresent("[Block fred.]");

        _selenium.select("//select[@id='blockName']", "barney");
        _selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);

        assertTextPresent("[Block barney.]");

    }

    @Test
    public void component_parameter_default_from_method() throws Exception
    {
        _selenium.open(BASE_URL);
        clickAndWait("link=ParameterDefault");

        assertTextPresent("Echo component default: [org.apache.tapestry.integration.app1.pages.ParameterDefault:echo]");
    }

    @Test
    public void embedded_components()
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=Countdown Page");

        assertTextPresent("regexp:\\s+5\\s+4\\s+3\\s+2\\s+1\\s+");

        assertTextPresent("Brought to you by the org.apache.tapestry.integration.app1.components.Count");
    }

    @Test
    public void encoded_loop_inside_a_form()
    {
        test_loop_inside_form("ToDo List");
    }

    @Test
    public void environmental()
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=Environmental Annotation Useage");

        assertSourcePresent("[<strong>A message provided by the RenderableProvider component.</strong>]");
    }

    @Test
    public void exception_report()
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=BadTemplate Page");

        assertTextPresent(
                "org.apache.tapestry.ioc.internal.util.TapestryException",
                "Failure parsing template classpath:org/apache/tapestry/integration/app1/pages/BadTemplate.html, line 7, column 15",
                "<t:foobar>content from template</t:foobar>",
                "Element <t:foobar> is in the Tapestry namespace, but is not a recognized Tapestry template element.");
    }

    @Test
    public void expansion()
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=Expansion Page");

        assertTextPresent("[value provided by a template expansion]");
    }

    /**
     * {@link InjectComponentWorker} is largely tested by the forms tests ({@link RenderDisabled}
     * is built on it). This test is for the failure case, where a mixin class is used with the
     * wrong type of component.
     */
    @Test
    public void inject_component_failure() throws Exception
    {
        _selenium.open(BASE_URL);
        clickAndWait("link=InjectComponentMismatch");

        // And exception message:

        assertTextPresent("Component org.apache.tapestry.integration.app1.pages.InjectComponentMismatch is not assignable to field org.apache.tapestry.corelib.mixins.RenderDisabled._field (of type org.apache.tapestry.Field).");
    }

    @Test
    public void injection() throws Exception
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=Inject Demo");

        // This is a test for a named @Inject:
        assertTextPresent("<Proxy for tapestry.Request(org.apache.tapestry.services.Request)>");

        // This is a test for an annonymous @Inject and ComponentResourcesInjectionProvider
        assertTextPresent("ComponentResources[org.apache.tapestry.integration.app1.pages.InjectDemo]");

        // Another test, DefaultInjectionProvider
        assertTextPresent("<Proxy for tapestry.BindingSource(org.apache.tapestry.services.BindingSource)>");
    }

    @Test
    public void instance_mixin()
    {
        _selenium.open(BASE_URL);

        final String[] dates =
        { "Jun 13, 1999", "Jul 15, 2001", "Dec 4, 2005" };

        clickAndWait("link=InstanceMixin");

        for (String date : dates)
        {
            String snippet = String.format("[%s]", date);

            assertSourcePresent(snippet);
        }

        clickAndWait("link=Toggle emphasis");

        for (String date : dates)
        {
            String snippet = String.format("[<em>%s</em>]", date);
            assertSourcePresent(snippet);
        }
    }

    @Test
    public void localization()
    {
        _selenium.open(BASE_URL);
        clickAndWait("link=Localization");

        assertTextPresent("Via injected Messages property: [Accessed via injected Messages]");
        assertTextPresent("Via message: binding prefix: [Accessed via message: binding prefix]");
        assertTextPresent("Page locale: [en]");
        clickAndWait("link=French");
        assertTextPresent("Page locale: [fr]");
        clickAndWait("link=English");
        assertTextPresent("Page locale: [en]");
    }

    @Test
    public void page_injection() throws Exception
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=Inject Demo");

        clickAndWait("link=Fred");

        assertTextPresent("You clicked Fred.");

        clickAndWait("link=Back");
        clickAndWait("link=Barney");

        assertTextPresent("You clicked Barney.");

        clickAndWait("link=Back");
        clickAndWait("link=Wilma");
        assertTextPresent("You clicked Wilma.");
    }

    @Test
    public void passivate_activate() throws Exception
    {
        _selenium.open(BASE_URL);
        clickAndWait("link=NumberSelect");
        clickAndWait("link=5");
        assertTextPresent("You chose 5.");
    }

    @Test
    public void password_field()
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=PasswordFieldDemo");

        _selenium.type("userName", "howard");
        _selenium.type("password", "wrong-password");

        clickAndWait("//input[@type='submit']");

        assertFieldValue("userName", "howard");
        // Verify that password fields do not render a non-blank password, even when it is known.
        assertFieldValue("password", "");

        assertTextPresent("[howard]");
        assertTextPresent("[wrong-password]");

        _selenium.type("password", "tapestry");

        clickAndWait("//input[@type='submit']");

        assertTextPresent("You have provided the correct user name and password.");
    }

    @Test
    public void render_phase_method_returns_a_component() throws Exception
    {
        _selenium.open(BASE_URL);
        clickAndWait("link=RenderComponentDemo");

        assertText("//span[@id='container']", "[]");

        // Sneak in a little test for If and parameter else:

        assertTextPresent("Should be blank:");

        clickAndWait("enabled");

        // After clicking the link (which submits the form), the page re-renders and shows us
        // the optional component from inside the NeverRender, resurrected to render on the page
        // after all.

        assertText("//span[@id='container']/span", "Optional Text");

        assertTextPresent("Should now show up:");
    }

    @Test
    public void render_phase_order()
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=RenderPhaseOrder");

        assertTextPresent("[BEGIN-TRACER-MIXIN BEGIN-ABSTRACT-TRACER BEGIN-TRACER BODY AFTER-TRACER AFTER-ABSTRACT-TRACER AFTER-TRACER-MIXIN]");
    }

    @Test
    public void server_side_validation_for_textfield_and_textarea() throws Exception
    {
        _selenium.open(BASE_URL);
        clickAndWait("link=ValidForm");
        clickAndWait("//input[@type='submit']");
        assertTextPresent("You must provide a value for Email.");
        // This is an overrdden validation error message:
        assertTextPresent("Please provide a detailed description of the incident.");

        // Check on decorations via the default validation decorator:

        assertText("//label[1]/@class", "t-error");
        assertText("//label[2]/@class", "t-error");
        assertText("//input[@id='email']/@class", "t-error");
        assertText("//textarea[@id='message']/@class", "t-error");

        _selenium.type("email", "foo@bar.baz");
        _selenium.type("message", "Show me the money!");
        _selenium.type("hours", "foo");

        clickAndWait("//input[@type='submit']");

        assertTextPresent("[false]");
        assertTextPresent("The input value 'foo' is not parseable as an integer value.");

        assertText("//input[@id='hours']/@value", "foo");

        _selenium.type("hours", " 19 ");
        _selenium.click("//input[@id='urgent']");
        clickAndWait("//input[@type='submit']");

        // Make sure the decoration went away.

        // Sorry, not sure how to do that, since the attributes don't exist, we get xpath errors.

        // assertText("//label[1]/@class", "");
        // assertText("//label[2]/@class", "");
        // assertText("//input[@id='email']/@class", "");
        // assertText("//textarea[@id='message']/@class", "");

        assertTextPresent("[foo@bar.baz]");
        assertTextPresent("[Show me the money!]");
        assertTextPresent("[true]");
        assertTextPresent("[19]");
    }

    @AfterClass
    public void cleanup() throws Exception
    {
        // Thread.sleep(10000);
        _selenium.stop();

        _selenium = null;

        _server.stop();
        _server = null;

        _jettyRunner.stop();
        _jettyRunner = null;
    }

    @Test
    public void simple_component_event()
    {
        final String YOU_CHOSE = "You chose: ";

        _selenium.open(BASE_URL);

        clickAndWait("link=Action Page");

        assertFalse(_selenium.isTextPresent(YOU_CHOSE));

        for (int i = 2; i < 5; i++)
        {
            clickAndWait("link=" + i);

            assertTextPresent(YOU_CHOSE + i);
        }
    }

    /**
     * Tests for forms and form submissions and basic form control components. This also tests a few
     * other things, such as computed default bindings and invisible instrumentation.
     */
    @Test
    public void simple_form()
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=SimpleForm");

        assertText("//label[@id='l1']", "Disabled");
        assertText("//label[@id='l2']", "Email");
        assertText("//label[@id='l3']", "Incident Message");
        assertText("//label[@id='l4']", "Operating System");
        assertText("//label[@id='l5']", "Department");
        assertText("//label[@id='l6']", "Urgent Processing Requested");

        assertFieldValue("email", "");
        assertFieldValue("message", "");
        assertFieldValue("operatingSystem", "osx");
        assertFieldValue("department", "ACCOUNTING");
        assertFieldValue("urgent", "on");

        _selenium.type("email", "foo@bar.baz");
        _selenium.type("message", "Message for you, sir!");
        _selenium.select("operatingSystem", "Windows NT");
        _selenium.select("department", "R&D");
        _selenium.click("urgent");

        clickAndWait("//input[@type='submit']");

        assertFieldValue("email", "foo@bar.baz");
        assertFieldValue("message", "Message for you, sir!");
        assertFieldValue("urgent", "off");

        // Tried to use "email:" and "exact:email:" but Selenium 0.8.1 doesn't seem to accept that.

        assertTextPresent(
                "[foo@bar.baz]",
                "[Message for you, sir!]",
                "[false]",
                "[winnt]",
                "[RESEARCH_AND_DESIGN]");

        // Haven't figured out how to get selenium to check that fields are disabled.
    }

    @BeforeClass
    public void setup() throws Exception
    {
        _jettyRunner = new JettyRunner("/", JETTY_PORT, "src/test/app1");

        _server = new SeleniumServer();

        _server.start();

        _selenium = new DefaultSelenium("localhost", SeleniumServer.DEFAULT_PORT, "*firefox",
                BASE_URL);

        _selenium.start();

        // Warm things up; this prevents errors when the first test runs. Seems to be a problem
        // with Selenium: inside the JavaScript or the server code.

        _selenium.open(BASE_URL);
    }

    @Test
    public void subclass_inherits_parent_template()
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=ExpansionSubclass");

        assertTextPresent("[value provided, in the subclass, via a template expansion]");
    }

    @Test
    public void template_overridden()
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=Template Overriden by Class Page");

        assertTextPresent("Output: ClassValue");
    }

    @Test
    public void volatile_loop_inside_a_form()
    {
        test_loop_inside_form("ToDo List (Volatile)");
    }

    /** This also verifies the use of meta data to set the default strategy. */
    @Test
    public void flash_persistence()
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=FlashDemo");

        assertTextPresent("[]");

        clickAndWait("show");

        assertTextPresent("[You clicked the link!]");

        clickAndWait("refresh");

        assertTextPresent("[]");
    }

    private void assertSourcePresent(String... expected)
    {
        String source = _selenium.getHtmlSource();

        for (String snippet : expected)
        {
            if (source.contains(snippet))
                continue;

            System.err.printf("Source content '%s' not found in:\n%s\n\n", snippet, source);

            throw new AssertionError("Page did not contain source '" + snippet + "'.");
        }
    }

    private void assertText(String locator, String expected)
    {
        String actual = null;

        try
        {
            actual = _selenium.getText(locator);
        }
        catch (RuntimeException ex)
        {
            System.err.printf(
                    "Error accessing %s: %s, in:\n\n%s\n\n",
                    locator,
                    ex.getMessage(),
                    _selenium.getHtmlSource());

            throw ex;
        }

        if (actual.equals(expected))
            return;

        System.err.printf(
                "Text for %s should be '%s' but is '%s', in:\n\n%s\n\n",
                locator,
                expected,
                actual,
                _selenium.getHtmlSource());

        throw new AssertionError(String.format("%s was '%s' not '%s'", locator, actual, expected));
    }

    private void assertTextPresent(String... text)
    {
        for (String item : text)
        {
            if (_selenium.isTextPresent(item))
                return;

            System.err.printf("Text pattern '%s' not found in:\n%s\n\n", item, _selenium
                    .getHtmlSource());

            throw new AssertionError("Page did not contain '" + item + "'.");
        }
    }

    private void assertFieldValue(String locator, String expected)
    {
        assertEquals(_selenium.getValue(locator), expected);
    }

    private void clickAndWait(String link)
    {
        _selenium.click(link);
        _selenium.waitForPageToLoad(PAGE_LOAD_TIMEOUT);
    }

    private byte[] readContent(URL url) throws Exception
    {
        InputStream is = new BufferedInputStream(url.openStream());

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[10000];

        while (true)
        {
            int length = is.read(buffer);

            if (length < 0)
                break;

            os.write(buffer, 0, length);
        }

        os.close();
        is.close();

        return os.toByteArray();
    }

    private void test_loop_inside_form(String linkLabel)
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=" + linkLabel);
        clickAndWait("reset");

        assertFieldValue("title", "End World Hunger");
        assertFieldValue("title_0", "Develop Faster-Than-Light Travel");
        assertFieldValue("title_1", "Cure Common Cold");

        _selenium.type("title", "End World Hunger - today");
        _selenium.type("title_0", "Develop Faster-Than-Light Travel - immediately");
        _selenium.type("title_1", "Cure Common Cold - post haste");

        clickAndWait("//input[@value='Update ToDos']");

        assertFieldValue("title", "End World Hunger - today");
        assertFieldValue("title_0", "Develop Faster-Than-Light Travel - immediately");
        assertFieldValue("title_1", "Cure Common Cold - post haste");

        clickAndWait("addNew");

        _selenium.type("title_2", "Conquer World");

        clickAndWait("//input[@value='Update ToDos']");

        assertFieldValue("title", "End World Hunger - today");
        assertFieldValue("title_0", "Develop Faster-Than-Light Travel - immediately");
        assertFieldValue("title_1", "Cure Common Cold - post haste");
        assertFieldValue("title_2", "Conquer World");
    }

    /**
     * Tests the bean editor. Along the way, tests a bunch about validation, loops, blocks, and
     * application state objects.
     */
    @Test
    public void bean_editor()
    {
        String submitButton = "//input[@id='submit']";

        _selenium.open(BASE_URL);
        clickAndWait("link=BeanEditor Demo");
        clickAndWait(submitButton);

        assertTextPresent(
                "(First Name is Required)",
                "You must provide a value for First Name.",
                "Everyone has to have a last name!",
                "Year of Birth requires a value of at least 1900.");

        // Part of the override for the firstName property

        assertText("//input[@id='firstName']/@size", "40");

        // Check override of the submit label

        assertText("//input[@id='submit']/@value", "Register");

        _selenium.type("firstName", "a");
        _selenium.type("lastName", "b");
        _selenium.type("birthYear", "");
        _selenium.select("sex", "label=Martian");
        _selenium.click("citizen");

        clickAndWait(submitButton);

        assertTextPresent(
                "You must provide at least 3 characters for First Name.",
                "You must provide at least 5 characters for Last Name.",
                "You must provide a value for Year of Birth.");

        _selenium.type("firstName", "Howard");
        _selenium.type("lastName", "Lewis Ship");
        _selenium.type("birthYear", "1966");

        clickAndWait(submitButton);

        assertTextPresent("[Howard]", "[Lewis Ship]", "[1966]", "[MARTIAN]", "[true]");
    }

    @Test
    public void pageloaded_lifecycle_method_invoked()
    {
        _selenium.open(BASE_URL);
        clickAndWait("link=PageLoaded Demo");

        assertTextPresent("[pageLoaded() was invoked.]");
    }

    private void assertTextSeries(String idFormat, int startIndex, String... values)
    {
        for (int i = 0; i < values.length; i++)
        {
            String id = String.format(idFormat, startIndex + i);

            assertText(id, values[i]);
        }
    }

    /**
     * Basic Grid rendering, with a column render override. Also tests sorting.
     */
    @Test
    public void basic_grid()
    {
        _selenium.open(BASE_URL);
        clickAndWait("link=Grid Demo");

        assertTextSeries("//th[%d]", 1, "Title", "Album", "Artist", "Genre", "Play Count", "Rating");

        // Strange: I thought tr[1] was the header row ???

        assertTextSeries(
                "//tr[1]/td[%d]",
                1,
                "Bug Juice",
                "Late Lounge (2 of 2)",
                "45 Dip",
                "Electronica",
                "4",
                "-");

        // Here were checking that the page splits are correct

        clickAndWait("link=3");

        // Last on page 3:
        assertText("//tr[25]/td[1]", "Blood Red River");

        clickAndWait("link=4");
        assertText("//tr[1]/td[1]", "Devil Song");

        clickAndWait("link=7");
        clickAndWait("link=10");

        // Here's one with a customized rating cell

        assertTextSeries(
                "//tr[25]/td[%d]",
                1,
                "Smoked",
                "London (Original Motion Picture Soundtrack)",
                "The Crystal Method",
                "Soundtrack",
                "30",
                "****");

        clickAndWait("link=69");

        assertText("//tr[22]/td[1]", "radioioAmbient");

        // Sort ascending (and we're on the last page, with the highest ratings).

        clickAndWait("link=Rating");

        assertText("//img[@id='img']/@src", "/assets/tapestry/corelib/components/sort-asc.png");
        assertText("//img[@id='img']/@alt", "[Asc]");

        assertTextSeries(
                "//tr[22]/td[%d]",
                1,
                "Mona Lisa Overdrive",
                "Labyrinth",
                "Juno Reactor",
                "Dance",
                "31",
                "*****");

        // Toggle to sort descending

        clickAndWait("link=Rating");

        assertText("//img[@id='img']/@src", "/assets/tapestry/corelib/components/sort-desc.png");
        assertText("//img[@id='img']/@alt", "[Desc]");

        assertTextSeries("//tr[1]/td[%d]", 1, "Hey Blondie", "Out from Out Where");

        clickAndWait("link=Title");

        assertText("//img[@id='img']/@src", "/assets/tapestry/corelib/components/sort-asc.png");
        assertText("//img[@id='img']/@alt", "[Asc]");

        clickAndWait("link=1");

        assertText("//tr[1]/td[1]", "(untitled hidden track)");
    }

    @Test
    public void grid_from_explicit_interface_model()
    {
        _selenium.open(BASE_URL);
        clickAndWait("link=SimpleTrack Grid Demo");

        assertTextSeries("//th[%d]", 1, "Title", "Album", "Rating");

        assertTextSeries("//tr[1]/td[%d]", 1, "Bug Juice", "Late Lounge (2 of 2)", "-");
    }

    @Test
    public void grid_enum_display()
    {
        _selenium.open(BASE_URL);
        clickAndWait("link=Grid Enum Demo");
        clickAndWait("link=reset");

        assertTextSeries("//tr[1]/td[%d]", 2, "End World Hunger", "Medium");
        assertTextSeries("//tr[2]/td[%d]", 2, "Develop Faster-Than-Light Travel", "High");
        assertTextSeries("//tr[3]/td[%d]", 2, "Cure Common Cold", "Low");
    }

    @Test
    public void stream_response() throws Exception
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=Text Stream Response");

        assertText("//body", "Success!");
    }

    @Test
    public void null_grid() throws Exception
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=Null Grid");

        assertTextPresent("There is no data to display.");
    }

    @Test
    public void navigation_response_from_page_activate() throws Exception
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=Protected Page");

        assertText("//h1", "Security Alert");

        // The message is set by Protected, but is rendered by SecurityAlert.

        assertTextPresent("Access to Protected page is denied");
    }

    @Test
    public void mixed_page_activation_context_and_component_context()
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=Kicker");

        clickAndWait("actionlink");

        assertTextSeries("//li[%d]", 1, "betty", "wilma");
        assertTextPresent("No component context.");

        clickAndWait("link=go");

        assertTextSeries("//li[%d]", 1, "betty", "wilma");
        assertTextSeries("//ul[2]/li[%d]", 1, "fred", "barney", "clark kent");
    }

    @Test
    public void page_link_with_explicit_activation_context()
    {
        _selenium.open(BASE_URL);

        clickAndWait("link=PageLink Context Demo");

        clickAndWait("link=no context");

        assertTextPresent("No activation context.");

        clickAndWait("link=PageLink Context Demo");

        clickAndWait("link=literal context");

        assertText("//li[1]", "literal context");

        clickAndWait("link=PageLink Context Demo");

        clickAndWait("link=computed context");

        assertTextSeries("//li[%d]", 1, "fred", "7", "true");
    }
}
