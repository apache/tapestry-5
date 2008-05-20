// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.integration;

import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.mixins.RenderDisabled;
import org.apache.tapestry5.integration.app1.pages.RenderErrorDemo;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.test.AbstractIntegrationTestSuite;
import org.testng.annotations.Test;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

/**
 * Note: If these tests fail with BindException when starting Jetty, it could be Skype. At least on my system, Skype is
 * listening on localhost:80.
 */
@Test(timeOut = 50000, sequential = true, groups = { "integration" })
public class IntegrationTests extends AbstractIntegrationTestSuite
{
    public IntegrationTests()
    {
        super("src/test/app1");
    }

    @Test
    public void assets() throws Exception
    {
        start("AssetDemo");

        // Test for https://issues.apache.org/jira/browse/TAPESTRY-1935

        assertSourcePresent("<link href=\"/css/app.css\" rel=\"stylesheet\" type=\"text/css\">");

        assertAttribute("//img[@id='icon']/@src", "/images/tapestry_banner.gif");

        // doesn't prove that the image shows up in the client browser (it does, but
        // it could just as easily be a broken image). Haven't figured out how Selenium
        // allows to be verified. Note that the path below represents some aliasing
        // of the raw classpath resource path.

        assertAttribute("//img[@id='button']/@src", "/assets/app1/pages/nested/tapestry-button.png");

        // Read the byte stream for the asset and compare to the real copy.

        URL url = new URL("http", "localhost", JETTY_PORT, "/assets/app1/pages/nested/tapestry-button.png");

        byte[] downloaded = readContent(url);

        Resource classpathResource = new ClasspathResource(
                "org/apache/tapestry5/integration/app1/pages/nested/tapestry-button.png");

        byte[] actual = readContent(classpathResource.toURL());

        assertEquals(downloaded, actual);
    }

    /**
     * Tests the ability to inject a Block, and the ability to use the block to control rendering.
     */
    @Test
    public void block_rendering() throws Exception
    {
        start("BlockDemo");

        assertTextPresent("[]");

        select("//select[@id='blockName']", "fred");
        waitForPageToLoad(PAGE_LOAD_TIMEOUT);

        assertTextPresent("[Block fred.]");

        select("//select[@id='blockName']", "barney");
        waitForPageToLoad(PAGE_LOAD_TIMEOUT);

        assertTextPresent("[Block barney.]");

        // TAPESETRY-1583

        assertTextPresent("before it is defined: [Block wilma].");
    }

    @Test
    public void component_parameter_default_from_method() throws Exception
    {
        start("ParameterDefault");

        assertTextPresent("Echo component default: [ParameterDefault:echo]");
    }

    @Test
    public void embedded_components()
    {
        start("Countdown Page");

        assertTextPresent("regexp:\\s+5\\s+4\\s+3\\s+2\\s+1\\s+");

        assertTextPresent("Brought to you by the org.apache.tapestry5.integration.app1.components.Count");
    }

    @Test
    public void encoded_loop_inside_a_form()
    {
        test_loop_inside_form("ToDo List");
    }

    @Test
    public void environmental()
    {
        start("Environmental Annotation Usage");

        assertSourcePresent("[<strong>A message provided by the RenderableProvider component.</strong>]");
    }

    @Test
    public void exception_report()
    {
        start("BadTemplate Page");

        assertTextPresent("org.apache.tapestry5.ioc.internal.util.TapestryException",
                          "Failure parsing template classpath:org/apache/tapestry5/integration/app1/pages/BadTemplate.tml, line 7, column 15",
                          "<t:foobar>content from template</t:foobar>",
                          "Element <t:foobar> is in the Tapestry namespace, but is not a recognized Tapestry template element.");
    }

    @Test
    public void expansion()
    {
        start("Expansion Page");

        assertTextPresent("[value provided by a template expansion]");
    }

    /**
     * {@link org.apache.tapestry5.internal.transform.InjectContainerWorker} is largely tested by the forms tests
     * ({@link RenderDisabled} is built on it). test is for the failure case, where a mixin class is used with the wrong
     * type of component.
     */
    @Test
    public void inject_container_failure() throws Exception
    {
        start("InjectContainerMismatch");

        // And exception message:

        assertTextPresent(
                "Component InjectContainerMismatch is not assignable to field org.apache.tapestry5.corelib.mixins.RenderDisabled.field (of type org.apache.tapestry5.Field).");
    }

    @Test
    public void inject_component_failure() throws Exception
    {
        start("InjectComponentMismatch");

        assertTextPresent(
                "Unable to inject component 'form' into field form of component InjectComponentMismatch. Class org.apache.tapestry5.corelib.components.BeanEditForm is not assignable to a field of type org.apache.tapestry5.corelib.components.Form.",
                "ClassCastException");
    }

    @Test
    public void injection() throws Exception
    {
        start("Inject Demo");

        // is a test for a named @Inject:
        assertTextPresent("<Proxy for Request(org.apache.tapestry5.services.Request)>");

        // is a test for an anonymous @Inject and ComponentResourcesInjectionProvider
        assertTextPresent("ComponentResources[InjectDemo]");

        // Another test, DefaultInjectionProvider
        assertTextPresent("<Proxy for BindingSource(org.apache.tapestry5.services.BindingSource)>");

        // Prove that injection using a marker annotation (to match against a marked service) works.

        assertTextPresent("Injection via Marker: Bonjour!");
    }

    @Test
    public void instance_mixin()
    {
        start("InstanceMixin");

        final String[] dates = { "Jun 13, 1999", "Jul 15, 2001", "Dec 4, 2005" };

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
        start("Localization");

        assertTextPresent("Via injected Messages property: [Accessed via injected Messages]");
        assertTextPresent("Via message: binding prefix: [Accessed via message: binding prefix]");
        assertTextPresent("From Application Message Catalog: [Application Catalog Working]");
        assertTextPresent("Page locale: [en]");
        clickAndWait("link=French");
        assertTextPresent("Page locale: [fr]");
        clickAndWait("link=English");
        assertTextPresent("Page locale: [en]");
    }

    @Test
    public void page_injection() throws Exception
    {
        start("Inject Demo");

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
        start("NumberSelect");

        clickAndWait("link=5");

        assertTextPresent("You chose 5.");
    }

    @Test
    public void password_field()
    {
        start("PasswordFieldDemo");

        type("userName", "howard");
        type("password", "wrong-password");

        clickAndWait(SUBMIT);

        assertFieldValue("userName", "howard");
        // Verify that password fields do not render a non-blank password, even when it is known.
        assertFieldValue("password", "");

        assertTextPresent("[howard]");
        assertTextPresent("[wrong-password]");

        type("password", "tapestry");

        clickAndWait(SUBMIT);

        assertTextPresent("You have provided the correct user name and password.");
    }

    @Test
    public void render_phase_method_returns_a_component() throws Exception
    {
        start("RenderComponentDemo");

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
        start("RenderPhaseOrder");


        assertTextPresent(
                "[BEGIN-TRACER-MIXIN BEGIN-ABSTRACT-TRACER BEGIN-TRACER BODY AFTER-TRACER AFTER-ABSTRACT-TRACER AFTER-TRACER-MIXIN]");
    }

    @Test
    public void server_side_validation_for_textfield_and_textarea() throws Exception
    {
        start("ValidForm");

        clickAndWait(SUBMIT);
        assertTextPresent("You must provide a value for Email.");
        // is an overridden validation error message:
        assertTextPresent("Please provide a detailed description of the incident.");

        // Check on decorations via the default validation decorator:

        assertAttribute("//label[1]/@class", "t-error");
        assertAttribute("//label[2]/@class", "t-error");
        assertAttribute("//input[@id='email']/@class", "t-error");
        assertAttribute("//textarea[@id='message']/@class", "t-error");

        type("email", "foo@bar.baz");
        type("message", "Show me the money!");
        type("hours", "foo");

        clickAndWait(SUBMIT);

        assertTextPresent("[false]");
        assertTextPresent("The input value 'foo' is not parseable as an integer value.");

        assertAttribute("//input[@id='hours']/@value", "foo");

        type("hours", " 19 ");
        click("//input[@id='urgent']");
        clickAndWait(SUBMIT);

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

    @Test
    public void simple_component_event()
    {
        final String YOU_CHOSE = "You chose: ";

        start("Action Page");

        assertFalse(isTextPresent(YOU_CHOSE));

        for (int i = 2; i < 5; i++)
        {
            clickAndWait("link=" + i);

            assertTextPresent(YOU_CHOSE + i);
        }
    }

    /**
     * Tests for forms and form submissions and basic form control components. also tests a few other things, such as
     * computed default bindings and invisible instrumentation.
     */
    @Test
    public void simple_form()
    {
        start("SimpleForm");

        assertText("//label[@id='disabled:label']", "Disabled");

        // This demonstrates TAPESTRY-1642:
        assertText("//label[@id='email:label']", "User Email");

        assertText("//label[@id='message:label']", "Incident Message");
        assertText("//label[@id='operatingSystem:label']", "Operating System");
        assertText("//label[@id='department:label']", "Department");
        assertText("//label[@id='urgent:label']", "Urgent Processing Requested");

        assertFieldValue("email", "");
        assertFieldValue("message", "");
        assertFieldValue("operatingSystem", "osx");
        assertFieldValue("department", "");
        assertFieldValue("urgent", "on");

        clickAndWait(SUBMIT);

        assertTextPresent("department: []");

        type("email", "foo@bar.baz");
        type("message", "Message for you, sir!");
        select("operatingSystem", "Windows NT");
        select("department", "R&D");
        click("urgent");

        clickAndWait(SUBMIT);

        assertFieldValue("email", "foo@bar.baz");
        assertFieldValue("message", "Message for you, sir!");
        assertFieldValue("urgent", "off");

        // Tried to use "email:" and "exact:email:" but Selenium 0.8.1 doesn't seem to accept that.

        assertTextPresent("[foo@bar.baz]", "[Message for you, sir!]", "[false]", "[winnt]", "[RESEARCH_AND_DESIGN]");

        // Haven't figured out how to get selenium to check that fields are disabled.
    }

    @Test
    public void subclass_inherits_parent_template()
    {
        start("ExpansionSubclass");

        assertTextPresent("[value provided, in the subclass, via a template expansion]");
    }

    @Test
    public void template_overridden()
    {
        start("Template Overridden by Class Page");

        assertTextPresent("Output: ClassValue");
    }

    @Test
    public void volatile_loop_inside_a_form()
    {
        test_loop_inside_form("ToDo List (Volatile)");
    }

    /**
     * also verifies the use of meta data to set the default strategy.
     */
    @Test
    public void flash_persistence()
    {
        start("FlashDemo");

        assertTextPresent("[]");

        clickAndWait("link=show the message");

        assertTextPresent("[You clicked the link!]");

        clickAndWait("link=refresh the page");

        assertTextPresent("[]");
    }

    private byte[] readContent(URL url) throws Exception
    {
        InputStream is = new BufferedInputStream(url.openStream());

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[10000];

        while (true)
        {
            int length = is.read(buffer);

            if (length < 0) break;

            os.write(buffer, 0, length);
        }

        os.close();
        is.close();

        return os.toByteArray();
    }

    private void test_loop_inside_form(String linkLabel)
    {
        start(linkLabel);

        clickAndWait("link=reset the database");

        assertFieldValue("title", "End World Hunger");
        assertFieldValue("title_0", "Develop Faster-Than-Light Travel");
        assertFieldValue("title_1", "Cure Common Cold");

        type("title", "End World Hunger - today");
        type("title_0", "Develop Faster-Than-Light Travel - immediately");
        type("title_1", "Cure Common Cold - post haste");

        clickAndWait("//input[@value='Update ToDos']");

        assertFieldValue("title", "End World Hunger - today");
        assertFieldValue("title_0", "Develop Faster-Than-Light Travel - immediately");
        assertFieldValue("title_1", "Cure Common Cold - post haste");

        clickAndWait("//input[@value='Add new ToDo']");

        type("title_2", "Conquer World");

        clickAndWait("//input[@value='Update ToDos']");

        assertFieldValue("title", "End World Hunger - today");
        assertFieldValue("title_0", "Develop Faster-Than-Light Travel - immediately");
        assertFieldValue("title_1", "Cure Common Cold - post haste");
        assertFieldValue("title_2", "Conquer World");
    }

    /**
     * Tests the bean editor. Along the way, tests a bunch about validation, loops, blocks, and application state
     * objects.
     */
    @Test
    public void bean_editor()
    {
        start("BeanEditor Demo", "Clear Data");
        clickAndWait(SUBMIT);

        // Part of the override for the firstName property

        assertAttribute("//input[@id='firstName']/@size", "40");

        // Check that the @Width annotation works

        assertAttribute("//input[@id='birthYear']/@size", "4");

        // Check override of the submit label

        assertAttribute("//input[@type='submit']/@value", "Register");


        type("firstName", "a");
        type("lastName", "b");
        type("birthYear", "");
        select("sex", "label=Martian");
        click("citizen");
        type("password", "abracadabra");
        type("notes", "line1\nline2\nline3");

        clickAndWait(SUBMIT);

        assertTextPresent("You must provide at least 3 characters for First Name.",
                          "You must provide at least 5 characters for Last Name.",
                          "You must provide a value for Year of Birth.");

        type("firstName", "Howard");
        type("lastName", "Lewis Ship");
        type("birthYear", "1966");
        type("password", "supersecret");

        clickAndWait(SUBMIT);

        // The XPath support is too weak for //div[@class='t-beandisplay-value'][%d], so we
        // just look for the text itself.

        assertTextPresent("Howard", "Lewis Ship", "1966", "Martian", "U. S. Citizen", "***********", "line1", "line2",
                          "line3");

    }

    @Test
    public void bean_editor_property_reorder_remove()
    {
        start("BeanEdit Remove/Reorder", "Clear Data");

        // Looks like a bug in Selenium; we can see //label[1] but not //label[2].
        // assertTextSeries("//label[%d]", 1, "Last Name", "First Name", "Sex", "U.S. Citizen");

        type("firstName", "Howard");
        type("lastName", "Lewis Ship");
        type("password", "supersecret");

        clickAndWait("//input[@type=\'submit\']");

        assertTextPresent("Howard", "Lewis Ship", "0", "100% He-Man", "U. S. Citizen");
    }

    @Test
    public void pageloaded_lifecycle_method_invoked()
    {
        start("PageLoaded Demo");

        assertTextPresent("[pageLoaded() was invoked.]");
    }


    /**
     * Basic Grid rendering, with a column render override. Also tests sorting.
     */
    @Test
    public void basic_grid()
    {
        start("Grid Demo");

        // "Sort Rating" via the header cell override (TAPESTRY-2081)

        assertTextSeries("//th[%d]", 1, "Title", "Album", "Artist", "Genre", "Play Count", "Sort Rating");

        // Strange: I thought tr[1] was the header row ???

        assertTextSeries("//tr[1]/td[%d]", 1, "Bug Juice", "Late Lounge (2 of 2)", "45 Dip", "Electronica", "4", "-");

        // Here were checking that the page splits are correct

        clickAndWait("link=3");

        // Last on page 3:
        assertText("//tr[25]/td[1]", "Blood Red River");

        clickAndWait("link=4");
        assertText("//tr[1]/td[1]", "Devil Song");

        clickAndWait("link=7");
        clickAndWait("link=10");

        // Here's one with a customized rating cell

        assertTextSeries("//tr[25]/td[%d]", 1, "Smoked", "London (Original Motion Picture Soundtrack)",
                         "The Crystal Method", "Soundtrack", "30", "****");

        clickAndWait("link=69");

        assertText("//tr[22]/td[1]", "radioioAmbient");

        // Sort ascending (and we're on the last page, with the highest ratings).

        clickAndWait("link=Sort Rating");


        assertTextSeries("//tr[22]/td[%d]", 1, "Mona Lisa Overdrive", "Labyrinth", "Juno Reactor", "Dance", "31",
                         "*****");

        // Toggle to sort descending

        clickAndWait("link=Sort Rating");

        assertTextSeries("//tr[1]/td[%d]", 1, "Hey Blondie", "Out from Out Where");

        clickAndWait("link=Title");

        // The lack of a leading slash indicates that the path was optimized, see TAPESTRY-1502

        assertAttribute("//img[@id='title:sort']/@src", "assets/tapestry/UNKNOWN/corelib/components/sort-asc.png");
        assertAttribute("//img[@id='title:sort']/@alt", "[Asc]");

        clickAndWait("link=1");

        assertText("//tr[1]/td[1]", "(untitled hidden track)");

        clickAndWait("link=Title");

        assertAttribute("//img[@id='title:sort']/@src", "assets/tapestry/UNKNOWN/corelib/components/sort-desc.png");
        assertAttribute("//img[@id='title:sort']/@alt", "[Desc]");

        clickAndWait("link=reset the Grid");

        // Back to where we started.

        assertTextSeries("//tr[1]/td[%d]", 1, "Bug Juice", "Late Lounge (2 of 2)", "45 Dip", "Electronica", "4", "-");
    }

    @Test
    public void grid_remove_reorder()
    {
        start("Grid Remove/Reorder Demo");

        assertTextSeries("//th[%d]", 1, "Rating", "Title", "Album", "Artist", "Genre");
    }

    @Test
    public void grid_from_explicit_interface_model()
    {
        start("SimpleTrack Grid Demo");

        assertTextSeries("//th[%d]", 1, "Title", "Album", "Rating");

        assertTextSeries("//tr[1]/td[%d]", 1, "Bug Juice", "Late Lounge (2 of 2)", "-");
    }

    @Test
    public void grid_enum_display()
    {
        start("Grid Enum Demo", "reset");

        assertTextSeries("//tr[1]/td[%d]", 1, "End World Hunger", "Medium");
        assertTextSeries("//tr[2]/td[%d]", 1, "Develop Faster-Than-Light Travel", "Ultra Important");
        assertTextSeries("//tr[3]/td[%d]", 1, "Cure Common Cold", "Low");
    }

    @Test
    public void null_grid() throws Exception
    {
        start("Null Grid");

        assertTextPresent("There is no data to display.");
    }

    @Test
    public void grid_set() throws Exception
    {
        start("Grid Set Demo");

        assertFalse(isTextPresent("Exception"));

        // Also check for TAPESTRY-2228

        assertAttribute("//table/@informal", "supported");
    }

    @Test
    public void navigation_response_from_page_activate() throws Exception
    {
        start("Protected Page");

        assertText("pagetitle", "Security Alert");

        // The message is set by Protected, but is rendered by SecurityAlert.

        assertTextPresent("Access to Protected page is denied");
    }

    @Test
    public void mixed_page_activation_context_and_component_context()
    {
        start("Kicker");

        clickAndWait("link=kick target");

        assertTextSeries("//li[%d]", 1, "betty", "wilma", "betty/wilma", "\u82B1\u5B50");
        assertTextPresent("No component context.");

        clickAndWait("link=go");

        assertTextSeries("//li[%d]", 1, "betty", "wilma", "betty/wilma", "\u82B1\u5B50");
        assertTextSeries("//ul[2]/li[%d]", 1, "fred", "barney", "clark kent", "fred/barney", "\u592A\u90CE");
    }

    @Test
    public void page_link_with_explicit_empty_context()
    {
        start("Kicker");

        clickAndWait("link=kick target");

        assertTextSeries("//li[%d]", 1, "betty", "wilma", "betty/wilma", "\u82B1\u5B50");

        clickAndWait("link=Target base, no context");

        assertTextPresent("No activation context.");
    }

    @Test
    public void page_link_with_explicit_activation_context()
    {
        start("PageLink Context Demo", "no context");

        assertTextPresent("No activation context.");

        clickAndWait("link=PageLink Context Demo");

        clickAndWait("link=literal context");

        assertText("//li[1]", "literal context");

        clickAndWait("link=PageLink Context Demo");

        clickAndWait("link=computed context");

        assertTextSeries("//li[%d]", 1, "fred", "7", "true");

        clickAndWait("link=PageLink Context Demo");

        clickAndWait("link=unsafe characters");

        assertText("//li[1]", "unsafe characters: !\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~");

        clickAndWait("link=PageLink Context Demo");

        clickAndWait("link=japanese kanji");

        assertText("//li[1]", "japanese kanji: \u65E5\u672C\u8A9E");

        // TAPESTRY-2221

        clickAndWait("link=PageLink Context Demo");

        clickAndWait("link=Null in context");

        assertTextPresent("Context values (which are added to the request URL) may not be null or blank.");
    }

    @Test
    public void page_context_in_form()
    {
        start("Page Context in Form");

        assertTextSeries("//li[%d]", 1, "betty", "wilma", "context with spaces", "context/with/slashes");
        assertFieldValue("t:ac", "betty/wilma/context with spaces/context%2Fwith%2Fslashes");

        clickAndWait(SUBMIT);

        assertTextSeries("//li[%d]", 1, "betty", "wilma", "context with spaces", "context/with/slashes");
        assertFieldValue("t:ac", "betty/wilma/context with spaces/context%2Fwith%2Fslashes");
    }

    @Test
    public void client_side_validation()
    {
        start("Client Validation Demo");

        // The lack of a leading slash indicates that the path was optimized, see TAPESTRY-1502

        assertAttributeSeries("//script[%d]/@src", 1, "assets/scriptaculous/UNKNOWN/prototype.js",
                              "assets/scriptaculous/UNKNOWN/scriptaculous.js");

        clickAndWait("link=Clear Data");

        // Notice: click, not click and wait.

        click(SUBMIT);

        // Looks like more weaknesses in Selenium, can only manage the first match not the others.
        assertTextSeries("//div[@class='t-error-popup'][%d]/span", 1, "You must provide a value for First Name."
                         //, "Everyone has to have a last name!",
                         //       "Year of Birth requires a value of at least 1900."
        );

        type("firstName", "Howard");
        type("lastName", "Lewis Ship");
        type("birthYear", "1000");
        type("password", "supersecret");

        click(SUBMIT);

        type("birthYear", "1966");
        click("citizen");

        clickAndWait(SUBMIT);

        assertTextPresent("Howard", "Lewis Ship", "1966", "U. S. Citizen");
    }

    @Test
    public void recursive_components_are_identified_as_errors()
    {
        start("Recursive Demo");

        assertTextPresent("An unexpected application exception has occurred.",
                          "The template for component org.apache.tapestry5.integration.app1.components.Recursive is recursive (contains another direct or indirect reference to component org.apache.tapestry5.integration.app1.components.Recursive). is not supported (components may not contain themselves).",
                          "component is <t:recursive>recursive</t:recursive>, so we\'ll see a failure.");
    }

    @Test
    public void render_phase_method_may_return_renderable()
    {
        start("Renderable Demo");

        assertTextPresent("Renderable Demo", "[proves it works.]");
    }

    @Test
    public void verify_event_handler_invocation_order_and_circumstance()
    {
        String clear = "link=clear";

        start("EventHandler Demo");

        clickAndWait(clear);

        clickAndWait("link=No Context");
        assertTextPresent(
                "[parent.eventHandlerZero(), parent.onAction(), child.eventHandlerZeroChild(), child.onAction()]");

        clickAndWait(clear);
        clickAndWait("link=Single context value");

        assertTextPresent(
                "[parent.eventHandlerOne(String), parent.eventHandlerZero(), parent.onAction(String), parent.onAction(), child.eventHandlerOneChild(), child.eventHandlerZeroChild(), child.onAction(String), child.onAction()]");

        clickAndWait(clear);
        clickAndWait("link=Two value context");
        assertTextPresent(
                "[parent.eventHandlerOne(String), parent.eventHandlerZero(), parent.onAction(String), parent.onAction(), child.eventHandlerOneChild(), child.eventHandlerZeroChild(), child.onAction(String), child.onAction()]");

        clickAndWait(clear);
        clickAndWait("link=Two value context (from fred)");

        assertTextPresent(
                "[parent.eventHandlerOne(String), parent.eventHandlerZero(), parent.onAction(String), parent.onAction(), child.eventHandlerForFred(), child.eventHandlerOneChild(), child.eventHandlerZeroChild(), child.onAction(String), child.onAction(), child.onActionFromFred(String), child.onActionFromFred()]");
    }

    @Test
    public void inherited_bindings()
    {
        start("Inherited Bindings Demo");

        assertTextPresent("Bound: [ value: the-bound-value, bound: true ]", "Unbound: [ value: null, bound: false ]");
    }

    @Test
    public void client_persistence()
    {
        start("Client Persistence Demo");

        assertTextPresent("Persisted value: []", "Session: [false]");

        clickAndWait("link=store string");

        assertTextPresent("Persisted value: [A String]", "Session: [false]");
    }

    @Test
    public void attribute_expansions()
    {
        start("Attribute Expansions Demo");

        assertAttribute("//div[@id='mixed-expansion']/@style", "color: blue;");
        assertAttribute("//div[@id='single']/@class", "red");
        assertAttribute("//div[@id='consecutive']/@class", "goober-red");
        assertAttribute("//div[@id='trailer']/@class", "goober-green");
        assertText("//div[@id='formal']", "ALERT-expansions work inside formal component parameters as well");

        // An unrelated test, but fills in a bunch of minor gaps.

        assertSourcePresent("<!-- A comment! -->");
    }

    @Test
    public void palette_component()
    {
        start("Palette Demo", "reset");

        addSelection("languages:avail", "label=Haskell");
        addSelection("languages:avail", "label=Javascript");
        click("languages:select");

        clickAndWait(SUBMIT);
        assertTextPresent("Selected Languages: [HASKELL, JAVASCRIPT]");

        addSelection("languages", "label=Javascript");
        click("languages:deselect");

        addSelection("languages:avail", "label=Perl");
        removeSelection("languages:avail", "label=Javascript");
        addSelection("languages:avail", "label=Erlang");
        addSelection("languages:avail", "label=Java");
        addSelection("languages:avail", "label=Lisp");
        addSelection("languages:avail", "label=Ml");
        addSelection("languages:avail", "label=Python");
        addSelection("languages:avail", "label=Ruby");

        click("languages:select");

        clickAndWait(SUBMIT);

        assertTextPresent("[ERLANG, HASKELL, JAVA, LISP, ML, PERL, PYTHON, RUBY]");

        check("reorder");
        clickAndWait(SUBMIT);

        addSelection("languages", "label=Ruby");

        for (int i = 0; i < 6; i++)
            click("languages:up");

        removeSelection("languages", "label=Ruby");
        addSelection("languages", "label=Perl");

        click("languages:down");

        clickAndWait(SUBMIT);

        assertTextPresent("[ERLANG, RUBY, HASKELL, JAVA, LISP, ML, PYTHON, PERL]");
    }

    @Test
    public void event_handler_return_types()
    {

        open(BASE_URL);

        assertTextPresent("Tapestry 5 Integration Application 1");

        clickAndWait("link=Return Types");
        assertTextPresent("Return Type Tests");

        clickAndWait("link=null");
        assertTextPresent("Return Type Tests");

        clickAndWait("link=string");
        assertTextPresent("Tapestry 5 Integration Application 1");
        goBack();
        waitForPageToLoad();

        clickAndWait("link=class");
        assertTextPresent("Tapestry 5 Integration Application 1");
        goBack();
        waitForPageToLoad();

        clickAndWait("link=page");
        assertTextPresent("Tapestry 5 Integration Application 1");
        goBack();
        waitForPageToLoad();

        clickAndWait("link=link");
        assertTextPresent("Tapestry 5 Integration Application 1");
        goBack();
        waitForPageToLoad();

        clickAndWait("link=stream");
        assertTextPresent("Success!");
        goBack();
        waitForPageToLoad();

        // This has been failing?  Why?

        // clickAndWait("link=URL");
        // assertTextPresent("Google");
        // goBack();
        // waitForPageToLoad();

        clickAndWait("link=bad");
        assertTextPresent("An unexpected application exception has occurred.",
                          "An event handler for component org.apache.tapestry5.integration.app1.pages.Start returned the value 20 (from method org.apache.tapestry5.integration.app1.pages.Start.onActionFromBadReturnType() (at Start.java:34)). Return type java.lang.Integer can not be handled.");

    }

    @Test
    public void access_to_page_name()
    {
        open(BASE_URL);

        assertText("activePageName", "Start");

        clickAndWait("link=Grid Demo");

        assertText("activePageName", "GridDemo");
    }

    @Test
    public void form_encoding_type()
    {
        start("Form Encoding Type");

        assertAttribute("//form/@enctype", "x-override");
    }

    @Test
    public void radio_button_and_group()
    {
        start("RadioDemo");

        String update = SUBMIT;

        // in a loop ...
        click("//label[.='Accounting']");
        clickAndWait(update);
        assertTextPresent("Selected department: ACCOUNTING");

        click("//label[.='Sales And Marketing']");
        clickAndWait(update);
        assertTextPresent("Selected department: SALES_AND_MARKETING");

        // not in a loop ...
        click("//label[.='Temp']");
        clickAndWait(update);
        assertTextPresent("Selected position: TEMP");

        click("//label[.='Lifer']");
        clickAndWait(update);
        assertTextPresent("Selected position: LIFER");
    }

    @Test
    public void regexp_validator()
    {
        start("Regexp Demo");

        String update = SUBMIT;

        type("zipCode", "abc");

        click(update); // but don't wait

        assertTextPresent("A zip code consists of five or nine digits, eg: 02134 or 90125-4472.");

        type("zipCode", "12345");

        clickAndWait(update);

        assertTextPresent("Zip code: [12345]");

        type("zipCode", "12345-9876");

        clickAndWait(update);

        assertTextPresent("Zip code: [12345-9876]");
    }

    @Test
    public void multiple_beaneditor_components()
    {
        start("MultiBeanEdit Demo", "Clear Data");

        type("firstName", "Howard");
        type("lastName", "Lewis Ship");
        type("path", "/var/www");
        clickAndWait("//input[@value='Set Access']");

        waitForPageToLoad("30000");

        assertTextSeries("//li[%d]", 1, "First Name: [Howard]", "Last Name: [Lewis Ship]", "Path: [/var/www]",
                         "Role: [GRANT]");
    }

    @Test
    public void grid_inside_form()
    {
        start("Grid Form Demo", "reset", "2");

        // The first input field is the form's hidden field.

        assertFieldValue("title", "ToDo # 6");
        assertFieldValueSeries("title_%d", 0, "ToDo # 7", "ToDo # 8", "ToDo # 9", "ToDo # 10");

        type("title_0", "Cure Cancer");
        select("urgency_0", "Top Priority");

        type("title_1", "Pay Phone Bill");
        select("urgency_1", "Low");

        clickAndWait(SUBMIT);

        assertFieldValueSeries("title_%d", 0, "Cure Cancer", "Pay Phone Bill");
        assertFieldValueSeries("urgency_%d", 0, "HIGH", "LOW");
    }

    @Test
    public void missing_template_for_page()
    {
        start("Missing Template Demo");

        assertTextPresent(
                "Page MissingTemplate did not generate any markup when rendered. This could be because its template file could not be located, or because a render phase method in the page prevented rendering.");
    }

    /**
     * This can test some output and parsing capability of the DateField component, but not the interesting client-side
     * behavior.
     */
    @Test
    public void basic_datefield()
    {
        start("DateField Demo");

        type("birthday", "12/24/66");
        type("asteroidImpact", "05/28/2046");

        clickAndWait(SUBMIT);

        assertTextPresent("Birthday: [12/24/1966]");
        assertTextPresent("Impact: [05/28/2046]");

        assertFieldValue("birthday", "12/24/1966");
        assertFieldValue("asteroidImpact", "05/28/2046");
    }

    /**
     * This also checks that the date type is displayed correctly by BeanDisplay and Grid.
     */
    @Test
    public void date_field_inside_bean_editor()
    {
        start("BeanEditor / Date Demo", "clear");

        type("name", "Howard Lewis Ship");
        type("date", "12/24/66");

        clickAndWait(SUBMIT);

        // Notice the date output format; that is controlled by the date Block on the
        // PropertyDisplayBlocks page.

        assertTextPresent("Howard Lewis Ship", "Dec 24, 1966");
    }

    /**
     * This basically checks that the services status page does not error.
     */
    @Test
    public void services_status()
    {
        open(BASE_URL + "servicestatus");

        assertTextPresent("Tapestry IoC Services Status");
    }

    @Test
    public void event_based_translate() throws Exception
    {
        start("EventMethod Translator");

        type("count", "123");
        clickAndWait(SUBMIT);

        assertTextPresent("Count: [123]");

        type("count", "0");
        clickAndWait(SUBMIT);

        assertTextPresent("Count: [0]");

        assertFieldValue("count", "zero");

        type("count", "456");
        clickAndWait(SUBMIT);

        assertTextPresent("Count: [456]");

        assertFieldValue("count", "456");

        type("count", "ZERO");
        clickAndWait(SUBMIT);

        assertTextPresent("Count: [0]");

        assertFieldValue("count", "zero");

        // Try the server-side custom exception reporting.

        type("count", "13");
        clickAndWait(SUBMIT);

        assertTextPresent("Event Handler Method Translate", "Thirteen is an unlucky number.");

        type("count", "i");
        clickAndWait(SUBMIT);

        assertTextPresent("Event Handler Method Translate", "Rational numbers only, please.");
    }

    @Test
    public void autocomplete_mixin()
    {
        start("Autocomplete Mixin Demo");

        // And that's as far as we can go currently, because
        // of limitations in Selenium 0.8.3 and bugs in Selenium 0.9.2.
    }

    @Test
    public void zone_updates()
    {
        start("Zone Demo");

        assertTextPresent("No name has been selected.");

        // Hate doing this, but selecting by the text isn't working, perhaps because of the
        // HTML entities.
        click("select_0");

        // And that's as far as we can go currently, because
        // of limitations in Selenium 0.8.3 and bugs in Selenium 0.9.2.

        // assertTextPresent("Selected: Mr. &lt;Roboto&gt;");

        click("link=Direct JSON response");
    }

    /**
     * Tests TAPESTRY-1934.
     */
    @Test
    public void base_class_must_be_in_controlled_package() throws Exception
    {
        open(BASE_URL + "invalidsuperclass");

        assertTextPresent(
                "Base class org.apache.tapestry5.integration.app1.WrongPackageForBaseClass (super class of org.apache.tapestry5.integration.app1.pages.InvalidSuperClass) is not in a controlled package and is therefore not valid. You should try moving the class to package org.apache.tapestry5.integration.app1.base.");
    }

    @Test
    public void xml_content() throws Exception
    {
        open(BASE_URL + "xmlcontent");

        // Commented out ... Selenium can't seem to handle an XML response.

        // assertSourcePresent("<![CDATA[< & >]]>");
    }

    /**
     * Tests TAPESTRY-2005.
     */
    @Test
    public void components_passed_as_parameters() throws Exception
    {
        start("ComponentParameter Demo");

        // This component is inside a block, and is only rendered because it is passed as a parameter, of type ActionLink,
        // to an ActionLinkIndirect component.

        clickAndWait("link=click me");

        assertTextPresent("Link was clicked.");
    }

    /**
     * Tests TAPESTRY-1546
     */
    @Test
    public void inherit_informals() throws Exception
    {
        start("Inherit Informal Parameters Demo");

        assertAttribute("//span[@id='target']/@class", "inherit");
    }

    @Test
    public void disabled_fields() throws Exception
    {
        start("Disabled Fields");

        String[] paths = new String[] { "//input[@id='textfield']",

                "//input[@id='passwordfield']",

                "//textarea[@id='textarea']",

                "//input[@id='checkbox']",

                "//select[@id='select']",

                "//input[@id='radio1']",

                "//input[@id='radio2']",

                "//input[@id='datefield']",

                "//select[@id='palette:avail']",

                "//button[@id='palette:select']",

                "//button[@id='palette:deselect']",

                "//select[@id='palette']",

                "//input[@id='submit']" };

        for (String path : paths)
        {
            String locator = String.format("%s/@disabled", path);

            assertAttribute(locator, "disabled");
        }
    }

    /**
     * TAPESTRY-2013
     */
    @Test
    public void bean_editor_overrides()
    {
        start("BeanEditor Override", "Clear Data");

        assertTextPresent("[FirstName Property Editor Override]");
    }

    /**
     * TAPESTRY-1830
     */
    @Test
    public void var_binding()
    {
        start("Var Binding Demo");

        assertTextSeries("//li[%d]", 1, "1", "2", "3");
    }

    /**
     * TAPESTRY-2021
     */
    @Test
    public void lean_grid()
    {
        start("Lean Grid Demo");

        assertTextSeries("//th[%d]", 1, "Title", "Album", "Artist", "Genre", "Play Count", "Rating");

        // Selenium makes it pretty hard to check for a missing class.

        // assertText("//th[1]/@class", "");
    }

    @Test
    public void unless_component()
    {
        start("Unless Demo");

        assertText("//p[@id='false']", "false is rendered");

        assertText("//p[@id='true']", "");
    }

    /**
     * TAPESTRY-2044
     */
    @Test
    public void action_links_on_non_active_page()
    {
        start("Action Links off of Active Page");

        String contextSpan = "//span[@id='context']";

        assertText(contextSpan, "0");

        clickAndWait("link=3");

        assertText(contextSpan, "3");

        clickAndWait("link=refresh");

        assertText(contextSpan, "3");

        clickAndWait("link=1");

        assertText(contextSpan, "1");

        clickAndWait("link=refresh");

        assertText(contextSpan, "1");
    }

    /**
     * TAPESTRY-2333
     */
    @Test
    public void action_links_on_custom_url()
    {
        open(BASE_URL + "nested/actiondemo/");

        clickAndWait("link=2");

        assertTextPresent("Number: 2");
    }

    /**
     * TAPESTRY-1598
     */
    @Test
    public void value_encoder_via_type_coercer()
    {
        start("Magic ValueEncoder Demo");

        select("number", "25");

        clickAndWait(SUBMIT);

        String locator = "//span[@id='selectednumber']";

        assertText(locator, "25");

        select("number", "100");
        clickAndWait(SUBMIT);

        assertText(locator, "100");
    }

    /**
     * TAPESTRY-2056
     */
    @Test
    public void null_field_strategy()
    {
        start("Null Field Strategy Demo");

        String locator = "//span[@id='value']";

        assertText(locator, "");

        assertAttribute("//input[@id='number']/@value", "0");

        type("number", "");

        clickAndWait(SUBMIT);

        assertText(locator, "0");
    }

    /**
     * TAPESTRY-1647
     */
    @Test
    public void label_invokes_validation_decorator_at_correct_time()
    {
        start("Override Validation Decorator");

        // This is sub-optimal, as it doesn't esnure that the before/after field values really do wrap around
        // the field (they do, but that's hard to prove!).

        // Along the way we are also testing:
        // - primitive types are automatically required
        // - AbstractTextField.isRequired() and the logic inside ComponentFieldValidator.isRequired()

        assertSourcePresent(
                "[Before label for Value]<label for=\"value\" id=\"value:label\">Value</label>[After label for Value]",
                "[Before field Value]", "[After field Value (optional)]",
                "[Before label for Required Value]<label for=\"requiredValue\" id=\"requiredValue:label\">Required Value</label>[After label for Required Value]",
                "[Before field Required Value]", "[After field Required Value (required)]");
    }

    /**
     * TAPESTRY-1724
     */
    @Test
    public void component_event_errors()
    {
        start("Exception Event Demo", "enable", "force invalid activation context");

        assertTextPresent(
                "Exception: Exception in method org.apache.tapestry5.integration.app1.pages.ExceptionEventDemo.onActivate(float)");

        clickAndWait("link=force invalid event context");

        assertTextPresent(
                "Exception: Exception in method org.apache.tapestry5.integration.app1.pages.ExceptionEventDemo.onActionFromFail(float)");

        // Revert to normal handling: return null from the onException() event handler method.

        clickAndWait("link=disable");

        clickAndWait("link=force invalid event context");

        assertTextPresent("An unexpected application exception has occurred.",
                          "org.apache.tapestry5.ioc.internal.util.TapestryException",
                          "java.lang.NumberFormatException");

    }

    /**
     * TAPESTRY-1416
     */

    @Test
    public void adding_new_columns_to_grid_programattically()
    {
        start("Added Grid Columns Demo", "Title Length");

        assertTextSeries("//th[%d]", 1, "Title", "View", "Title Length", "Dummy");

        // The rendered &nbsp; becomes just a blank string.
        assertTextSeries("//tr[1]/td[%d]", 1, "7", "view", "1", "");
    }

    /**
     * TAPESTRY-1518
     */
    @Test
    public void generic_page_type()
    {
        start("Generic Page Class Demo");

        assertTextPresent("Editor for org.apache.tapestry5.integration.app1.data.Track");

        assertSourcePresent("<label for=\"title\" id=\"title:label\">Title</label>");
    }

    /**
     * TAPESTRY-2088
     */
    @Test
    public void primitive_array_as_parameter_type()
    {
        start("Primitive Array Parameter Demo");

        assertSourcePresent("<ul><li>1</li><li>3</li><li>5</li><li>7</li><li>9</li></ul>");
    }

    /**
     * TAPESTRY-2097
     */
    @Test
    public void render_queue_exception()
    {
        start("Render Error Demo");

        assertTextPresent("An unexpected application exception has occurred");

        // Just sample a smattering of the vast amount of data in the exception report.

        assertTextPresent("RenderErrorDemo", "class " + RenderErrorDemo.class.getName(), "RenderErrorDemo:border",
                          "RenderErrorDemo:echo");
    }

    /**
     * TAPESTRY-1594
     */
    @Test
    public void ignored_paths_filter()
    {
        start("Unreachable Page");

        assertTextPresent("HTTP ERROR: 404");
    }

    /**
     * TAPESTRY-2085
     */
    @Test
    public void render_phase_methods_may_throw_checked_exceptions()
    {
        start("Render Phase Method Exception Demo");

        assertTextPresent(
                "Render queue error in BeginRender[RenderPhaseMethodExceptionDemo]: java.sql.SQLException: Simulated JDBC exception while rendering.");
    }

    /**
     * TAPESTRY-2085
     */
    @Test
    public void wrapper_types_with_text_field()
    {
        start("TextField Wrapper Types", "clear");

        assertFieldValue("count", "");
        assertText("value", "null");

        type("count", "0");
        clickAndWait(SUBMIT);

        assertFieldValue("count", "0");
        assertText("value", "0");

        type("count", "1");
        clickAndWait(SUBMIT);

        assertFieldValue("count", "1");
        assertText("value", "1");

        clickAndWait("link=clear");

        assertFieldValue("count", "");
        assertText("value", "null");
    }

    /**
     * TAPESTRY-1901
     */
    @Test
    public void delete_rows_from_grid()
    {
        start("Delete From Grid", "setup the database", "2");

        for (int i = 6; i <= 10; i++)
            clickAndWait("link=ToDo #" + i);

        // A rather clumsy way to ensure we're back on the first page.

        for (int i = 1; i <= 5; i++)
            assertTextPresent("ToDo #" + i);
    }

    /**
     * TAPESTRY-2114
     */
    @Test
    public void boolean_properties_can_use_get_or_is_as_method_name_prefix()
    {
        start("Boolean Property Demo", "clear");

        assertText("usingGet", "false");
        assertText("usingIs", "false");

        clickAndWait("link=set");

        assertText("usingGet", "true");
        assertText("usingIs", "true");
    }

    @Test
    public void form_fragment()
    {
        start("Form Fragment Demo", "Clear");

        type("name", "Fred");
        // Really, you can't type in the field because it is not visible, but
        // this checks that invisible fields are not processed.
        type("email", "this field is ignored");

        clickAndWait(SUBMIT);

        assertText("name", "Fred");
        assertText("email", "");

        clickAndWait("link=Back");
        clickAndWait("link=Clear");

        click("subscribeToEmail");
        click("on");
        type("name", "Barney");
        type("email", "rubble@bedrock.gov");
        type("code", "ABC123");

        click("off");
        sleep(1000);

        clickAndWait(SUBMIT);

        assertText("name", "Barney");
        assertText("email", "rubble@bedrock.gov");
        assertText("code", "");
    }

    @Test
    public void zone_inject_component_from_template()
    {
        start("Inject Component Demo");

        assertTextPresent(Form.class.getName() + "[form--form]");
    }


    /**
     * This may need to be disabled or dropped from the test suite, I don't know that Selenium, especially Selenium
     * running headless on the CI server, can handle the transition to HTTPS: there's warnings that pop up about
     * certificates.
     * <p/>
     * <p/>
     * Verified: Selenium can't handle this, even with a user manually OK-ing the certificate warning dialogs.
     */
    @Test(enabled = false)
    public void secure_page_access()
    {
        start("Secure Page Demo");

        assertText("secure", "secure");

        assertText("message", "Triggered from Start");

        clickAndWait("link=click");

        assertText("secure", "secure");

        assertText("message", "Link clicked");

        clickAndWait(SUBMIT);

        assertText("secure", "secure");
        assertText("message", "Form submitted");

        clickAndWait("link=Back to index");

        // Back to the insecure home page.

        assertText("//h1", "Tapestry 5 Integration Application 1");
    }

    /**
     * TAPESTRY-2184
     */
    @Test
    public void create_action_link_while_not_rendering()
    {
        start("Action via Link Demo", "via explicit Link creation");

        assertText("message", "from getActionURL()");
    }

    /**
     * TAPESTRY-1475
     */
    @Test
    public void discard_persistent_field_changes()
    {
        start("Persistent Demo");

        assertText("message", "");

        clickAndWait("link=Update the message field");

        assertText("message", "updated");

        clickAndWait("link=Refresh page");

        assertText("message", "updated");

        clickAndWait("link=Discard persistent field changes");

        assertText("message", "");
    }

    /**
     * TAPESTRY-2150.  Also demonstrates how to add a ValueEncoder for an entity object, to allow seamless encoding of
     * the entity's id into the URL.
     */
    @Test
    public void nested_page_names()
    {
        start("Music Page", "2");

        assertText("activePageName", "Music");

        clickAndWait("link=The Gift");

        assertText("activePageName", "music/Details");
    }

    /**
     * TAPESTRY-1869
     */
    @Test
    public void null_fields_and_bean_editor()
    {
        start("Number BeanEditor Demo");

        clickAndWait(SUBMIT);

        // Hard to check for anything here.

        clickAndWait("link=Back to form");

        type("value", "237");

        clickAndWait(SUBMIT);

        assertText("//div[@class='t-beandisplay-value value']", "237");
    }

    /**
     * TAPESTRY-1999
     */
    @Test
    public void list_as_event_context()
    {
        start("List Event Context Demo");

        assertTextSeries("//ul[@id='eventcontext']/li[%d]", 1, "1", "2", "3");
    }

    @Test
    public void form_injector()
    {
        start("FormInjector Demo");

        assertText("sum", "0.0");

        click("link=Add a row");

        sleep(1000);

        type("//input[@type='text'][1]", "5.1");

        // I wanted to add two rows, but Selenium didn't want to play.

        clickAndWait(SUBMIT);

        assertText("sum", "5.1");
    }

    /**
     * TAPESTRY-2196
     */
    @Test
    public void protected_field_in_page_class()
    {
        start("Protected Fields Demo", "Trigger the Exception");

        assertTextPresent("An unexpected application exception has occurred.",
                          "Class org.apache.tapestry5.integration.app1.pages.ProtectedFields contains field(s) (_field) that are not private. You should change these fields to private, and add accessor methods if needed.");
    }


    /**
     * TAPESTRY-2078
     */
    @Test
    public void noclassdeffound_exception_is_linked_to_underlying_cause()
    {
        start("Class Transformation Exception Demo");

        assertTextPresent(
                "Class org.apache.tapestry5.integration.app1.pages.Datum contains field(s) (_value) that are not private. You should change these fields to private, and add accessor methods if needed.");
    }

    /**
     * TAPESTRY-2244
     */
    @Test
    public void cached()
    {
        start("Cached Annotation");

        assertText("value", "000");
        assertText("value2size", "111");

        assertText("//span[@class='watch'][1]", "0");
        assertText("//span[@class='watch'][2]", "0");
        assertText("//span[@class='watch'][3]", "1");

        clickAndWait("link=Back to index");

        // TAPESTRY-2338: Make sure the data is cleared.

        clickAndWait("link=Cached Annotation");

        assertText("value", "000");
        assertText("value2size", "111");

        assertText("//span[@class='watch'][1]", "0");
        assertText("//span[@class='watch'][2]", "0");
        assertText("//span[@class='watch'][3]", "1");

    }

    /**
     * TAPESTRY-2244
     */
    @Test
    public void override_method_with_cached()
    {
        start("Cached Annotation2");

        assertText("value", "111");

        clickAndWait("link=Back to index");

        // TAPESTRY-2338: Make sure the data is cleared.

        clickAndWait("link=Cached Annotation2");

        assertText("value", "111");
    }

    private void sleep(long timeout)
    {
        try
        {
            Thread.sleep(timeout);
        }
        catch (InterruptedException ex)
        {
        }
    }

    /**
     * TAPESTRY-2338
     */
    @Test
    public void cached_properties_cleared_at_end_of_request()
    {
        start("Clean Cache Demo");

        String time1_1 = getText("time1");
        String time1_2 = getText("time1");

        // Don't know what they are but they should be the same.

        assertEquals(time1_2, time1_1);

        click("link=update");

        sleep(250);

        String time2_1 = getText("time1");
        String time2_2 = getText("time1");

        // Check that @Cache is still working

        assertEquals(time2_2, time2_1);

        assertFalse(time2_1.equals(time1_1),
                    "After update the nanoseconds time did not change, meaning @Cache was broken.");

    }

    @Test
    public void inplace_grid()
    {
        start("In-Place Grid Demo");

        String timestamp = getText("lastupdate");

        click("link=2");
        sleep(100);
        click("link=Album");
        sleep(100);

        assertEquals(getText("lastupdate"), timestamp,
                     "Timestamp should not have changed because updates are in-place.");
    }


    @Test
    public void method_advice()
    {
        start("Method Advice Demo");

        // @ReverseStrings intercepted and reversed the result:
        assertText("message", "!olleH");

        // @ReverseStrings doesn't do anything for non-Strings
        assertText("version", "5");

        // @ReverseStrings filtered the checked exception to a string result
        assertText("cranky",
                   "Invocation of method getCranky() failed with org.apache.tapestry5.integration.app1.services.DearGodWhyMeException.");

        // Now to check advice on a setter that manipulates parameters

        type("text", "Tapestry");
        clickAndWait(SUBMIT);

        assertText("output-text", "yrtsepaT");
    }
}
