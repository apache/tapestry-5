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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.apache.tapestry.corelib.mixins.RenderDisabled;
import org.apache.tapestry.internal.services.InjectComponentWorker;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.internal.util.ClasspathResource;
import org.apache.tapestry.test.AbstractIntegrationTestSuite;
import org.testng.annotations.Test;

/**
 * Note: If these tests fail with BindException when starting Jetty, it could be Skype. At least on
 * my system, Skype is listening on localhost:80.
 */
@Test(timeOut = 50000, sequential = true, groups =
{ "integration" })
public class IntegrationTests extends AbstractIntegrationTestSuite
{
    @Test
    public void assets() throws Exception
    {
        open(BASE_URL);
        clickAndWait("link=AssetDemo");

        assertText("//img[@id='icon']/@src", "/images/tapestry_banner.gif");

        // This doesn't prove that the image shows up in the client browser (it does, but
        // it could just as easily be a broken image). Haven't figured out how Selenium
        // allows this to be verified. Note that the path below represents some aliasing
        // of the raw classpath resource path.

        assertText("//img[@id='button']/@src", "/assets/app1/pages/tapestry-button.png");

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

        open(BASE_URL);

        clickAndWait("link=Count Page");

        assertTextPresent("Ho! Ho! Ho!");
    }

    /**
     * Tests the ability to inject a Block, and the ability to use the block to control rendering.
     */
    @Test
    public void block_rendering() throws Exception
    {
        open(BASE_URL);
        clickAndWait("link=BlockDemo");

        assertTextPresent("[]");

        select("//select[@id='blockName']", "fred");
        waitForPageToLoad(PAGE_LOAD_TIMEOUT);

        assertTextPresent("[Block fred.]");

        select("//select[@id='blockName']", "barney");
        waitForPageToLoad(PAGE_LOAD_TIMEOUT);

        assertTextPresent("[Block barney.]");

    }

    @Test
    public void component_parameter_default_from_method() throws Exception
    {
        open(BASE_URL);
        clickAndWait("link=ParameterDefault");

        assertTextPresent("Echo component default: [org.apache.tapestry.integration.app1.pages.ParameterDefault:echo]");
    }

    @Test
    public void embedded_components()
    {
        open(BASE_URL);

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
        open(BASE_URL);

        clickAndWait("link=Environmental Annotation Useage");

        assertSourcePresent("[<strong>A message provided by the RenderableProvider component.</strong>]");
    }

    @Test
    public void exception_report()
    {
        open(BASE_URL);

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
        open(BASE_URL);

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
        open(BASE_URL);
        clickAndWait("link=InjectComponentMismatch");

        // And exception message:

        assertTextPresent("Component org.apache.tapestry.integration.app1.pages.InjectComponentMismatch is not assignable to field org.apache.tapestry.corelib.mixins.RenderDisabled._field (of type org.apache.tapestry.Field).");
    }

    @Test
    public void injection() throws Exception
    {
        open(BASE_URL);

        clickAndWait("link=Inject Demo");

        // This is a test for a named @Inject:
        assertTextPresent("<Proxy for Request(org.apache.tapestry.services.Request)>");

        // This is a test for an annonymous @Inject and ComponentResourcesInjectionProvider
        assertTextPresent("ComponentResources[org.apache.tapestry.integration.app1.pages.InjectDemo]");

        // Another test, DefaultInjectionProvider
        assertTextPresent("<Proxy for BindingSource(org.apache.tapestry.services.BindingSource)>");
    }

    @Test
    public void instance_mixin()
    {
        open(BASE_URL);

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
        open(BASE_URL);
        clickAndWait("link=Localization");

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
        open(BASE_URL);

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
        open(BASE_URL);
        clickAndWait("link=NumberSelect");
        clickAndWait("link=5");
        assertTextPresent("You chose 5.");
    }

    @Test
    public void password_field()
    {
        open(BASE_URL);

        clickAndWait("link=PasswordFieldDemo");

        type("userName", "howard");
        type("password", "wrong-password");

        clickAndWait("//input[@type='submit']");

        assertFieldValue("userName", "howard");
        // Verify that password fields do not render a non-blank password, even when it is known.
        assertFieldValue("password", "");

        assertTextPresent("[howard]");
        assertTextPresent("[wrong-password]");

        type("password", "tapestry");

        clickAndWait("//input[@type='submit']");

        assertTextPresent("You have provided the correct user name and password.");
    }

    @Test
    public void render_phase_method_returns_a_component() throws Exception
    {
        open(BASE_URL);
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
        open(BASE_URL);

        clickAndWait("link=RenderPhaseOrder");

        assertTextPresent("[BEGIN-TRACER-MIXIN BEGIN-ABSTRACT-TRACER BEGIN-TRACER BODY AFTER-TRACER AFTER-ABSTRACT-TRACER AFTER-TRACER-MIXIN]");
    }

    @Test
    public void server_side_validation_for_textfield_and_textarea() throws Exception
    {
        open(BASE_URL);
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

        type("email", "foo@bar.baz");
        type("message", "Show me the money!");
        type("hours", "foo");

        clickAndWait("//input[@type='submit']");

        assertTextPresent("[false]");
        assertTextPresent("The input value 'foo' is not parseable as an integer value.");

        assertText("//input[@id='hours']/@value", "foo");

        type("hours", " 19 ");
        click("//input[@id='urgent']");
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

    @Test
    public void simple_component_event()
    {
        final String YOU_CHOSE = "You chose: ";

        open(BASE_URL);

        clickAndWait("link=Action Page");

        assertFalse(isTextPresent(YOU_CHOSE));

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
        open(BASE_URL);

        clickAndWait("link=SimpleForm");

        assertText("//label[@id='disabled:label']", "Disabled");
        assertText("//label[@id='email:label']", "Email");
        assertText("//label[@id='message:label']", "Incident Message");
        assertText("//label[@id='operatingSystem:label']", "Operating System");
        assertText("//label[@id='department:label']", "Department");
        assertText("//label[@id='urgent:label']", "Urgent Processing Requested");

        assertFieldValue("email", "");
        assertFieldValue("message", "");
        assertFieldValue("operatingSystem", "osx");
        assertFieldValue("department", "ACCOUNTING");
        assertFieldValue("urgent", "on");

        type("email", "foo@bar.baz");
        type("message", "Message for you, sir!");
        select("operatingSystem", "Windows NT");
        select("department", "R&D");
        click("urgent");

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

    @Test
    public void subclass_inherits_parent_template()
    {
        open(BASE_URL);

        clickAndWait("link=ExpansionSubclass");

        assertTextPresent("[value provided, in the subclass, via a template expansion]");
    }

    @Test
    public void template_overridden()
    {
        open(BASE_URL);

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
        open(BASE_URL);

        clickAndWait("link=FlashDemo");

        assertTextPresent("[]");

        clickAndWait("show");

        assertTextPresent("[You clicked the link!]");

        clickAndWait("refresh");

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
        open(BASE_URL);

        clickAndWait("link=" + linkLabel);
        clickAndWait("reset");

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

        clickAndWait("addNew");

        type("title_2", "Conquer World");

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
        String submitButton = "//input[@type='submit']";

        open(BASE_URL);
        clickAndWait("link=BeanEditor Demo");
        clickAndWait("link=Clear Data");
        clickAndWait(submitButton);

        assertTextPresent(
                "(First Name is Required)",
                "You must provide a value for First Name.",
                "Everyone has to have a last name!",
                "Year of Birth requires a value of at least 1900.");

        // Part of the override for the firstName property

        assertText("//input[@id='firstName']/@size", "40");

        // Check override of the submit label

        assertText("//input[@type='submit']/@value", "Register");

        type("firstName", "a");
        type("lastName", "b");
        type("birthYear", "");
        select("sex", "label=Martian");
        click("citizen");

        clickAndWait(submitButton);

        assertTextPresent(
                "You must provide at least 3 characters for First Name.",
                "You must provide at least 5 characters for Last Name.",
                "You must provide a value for Year of Birth.");

        type("firstName", "Howard");
        type("lastName", "Lewis Ship");
        type("birthYear", "1966");

        clickAndWait(submitButton);

        assertTextPresent("[Howard]", "[Lewis Ship]", "[1966]", "[MARTIAN]", "[true]");
    }

    @Test
    public void pageloaded_lifecycle_method_invoked()
    {
        open(BASE_URL);
        clickAndWait("link=PageLoaded Demo");

        assertTextPresent("[pageLoaded() was invoked.]");
    }

    /**
     * Basic Grid rendering, with a column render override. Also tests sorting.
     */
    @Test
    public void basic_grid()
    {
        open(BASE_URL);
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

        assertText("//img[@id='rating:sort']/@src", "/assets/tapestry/corelib/components/sort-asc.png");
        assertText("//img[@id='rating:sort']/@alt", "[Asc]");

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

        assertText("//img[@id='rating:sort']/@src", "/assets/tapestry/corelib/components/sort-desc.png");
        assertText("//img[@id='rating:sort']/@alt", "[Desc]");

        assertTextSeries("//tr[1]/td[%d]", 1, "Hey Blondie", "Out from Out Where");

        clickAndWait("link=Title");

        assertText("//img[@id='title:sort']/@src", "/assets/tapestry/corelib/components/sort-asc.png");
        assertText("//img[@id='title:sort']/@alt", "[Asc]");

        clickAndWait("link=1");

        assertText("//tr[1]/td[1]", "(untitled hidden track)");
    }

    @Test
    public void grid_from_explicit_interface_model()
    {
        open(BASE_URL);
        clickAndWait("link=SimpleTrack Grid Demo");

        assertTextSeries("//th[%d]", 1, "Title", "Album", "Rating");

        assertTextSeries("//tr[1]/td[%d]", 1, "Bug Juice", "Late Lounge (2 of 2)", "-");
    }

    @Test
    public void grid_enum_display()
    {
        open(BASE_URL);
        clickAndWait("link=Grid Enum Demo");
        clickAndWait("link=reset");

        assertTextSeries("//tr[1]/td[%d]", 2, "End World Hunger", "Medium");
        assertTextSeries("//tr[2]/td[%d]", 2, "Develop Faster-Than-Light Travel", "High");
        assertTextSeries("//tr[3]/td[%d]", 2, "Cure Common Cold", "Low");
    }

    @Test
    public void null_grid() throws Exception
    {
        open(BASE_URL);

        clickAndWait("link=Null Grid");

        assertTextPresent("There is no data to display.");
    }

    @Test
    public void navigation_response_from_page_activate() throws Exception
    {
        open(BASE_URL);

        clickAndWait("link=Protected Page");

        assertText("//h1", "Security Alert");

        // The message is set by Protected, but is rendered by SecurityAlert.

        assertTextPresent("Access to Protected page is denied");
    }

    @Test
    public void mixed_page_activation_context_and_component_context()
    {
        open(BASE_URL);

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
        open(BASE_URL);

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

    @Test
    public void client_side_validation()
    {
        open(BASE_URL);

        clickAndWait("link=Client Validation Demo");

        assertTextSeries(
                "//script[%d]/@src",
                1,
                "/assets/scriptaculous/prototype.js",
                "/assets/scriptaculous/scriptaculous.js");

        clickAndWait("link=Clear Data");

        // Notice: click, not click and wait.

        click("//input[@type='submit']");

        assertTextSeries(
                "//li[%d]",
                1,
                "You must provide a value for First Name.",
                "Everyone has to have a last name!",
                "Year of Birth requires a value of at least 1900.");

        type("firstName", "Howard");
        type("lastName", "Lewis Ship");
        type("birthYear", "1000");
        click("//input[@type='submit']");

        assertText("//li", "Year of Birth requires a value of at least 1900.");

        type("birthYear", "1966");
        click("citizen");

        clickAndWait("//input[@type='submit']");

        assertTextPresent("First Name: [Howard]");
    }

    @Test
    public void recursive_components_are_identified_as_errors()
    {
        open(BASE_URL);
        clickAndWait("link=Recursive Demo");

        assertTextPresent(
                "An unexpected application exception has occurred.",
                "The template for component org.apache.tapestry.integration.app1.components.Recursive is recursive (contains another direct or indirect reference to component org.apache.tapestry.integration.app1.components.Recursive). This is not supported (components may not contain themselves).",
                "This component is <t:recursive>recursive</t:recursive>, so we\'ll see a failure.");
    }

    @Test
    public void render_phase_method_may_return_renderable()
    {
        open(BASE_URL);
        clickAndWait("link=Renderable Demo");

        assertTextPresent("Renderable Demo", "[This proves it works.]");
    }

    @Test
    public void verify_event_handler_invocation_order_and_circumstance()
    {
        String clear = "link=clear";

        open(BASE_URL);
        clickAndWait("link=EventHandler Demo");
        clickAndWait(clear);

        clickAndWait("wilma");
        assertTextPresent("[parent.eventHandlerZero(), parent.onAction(), child.eventHandlerZeroChild(), child.onAction()]");

        clickAndWait(clear);
        clickAndWait("barney");

        assertTextPresent("[parent.eventHandlerOne(String), parent.eventHandlerZero(), parent.onAction(), parent.onAction(String), child.eventHandlerOneChild(), child.eventHandlerZeroChild(), child.onAction(), child.onAction(String)]");

        clickAndWait(clear);
        clickAndWait("betty");
        assertTextPresent("[parent.eventHandlerOne(String), parent.eventHandlerZero(), parent.onAction(), parent.onAction(String), child.eventHandlerOneChild(), child.eventHandlerZeroChild(), child.onAction(), child.onAction(String)]");

        clickAndWait(clear);
        clickAndWait("fred");

        assertTextPresent("[parent.eventHandlerOne(String), parent.eventHandlerZero(), parent.onAction(), parent.onAction(String), child.eventHandlerForFred(), child.eventHandlerOneChild(), child.eventHandlerZeroChild(), child.onAction(), child.onAction(String), child.onActionFromFred(), child.onActionFromFred(String), child.onAnyEventFromFred(), child.onAnyEventFromFred(String)]");
    }

    @Test
    public void inherited_bindings()
    {
        open(BASE_URL);
        clickAndWait("link=Inherited Bindings Demo");

        assertTextPresent(
                "Bound: [ value: the-bound-value, bound: true ]",
                "Unbound: [ value: null, bound: false ]");
    }

    @Test
    public void client_persistence()
    {
        open(BASE_URL);
        clickAndWait("link=Client Persistence Demo");

        assertTextPresent("Persisted value: []", "Session: [false]");

        clickAndWait("link=store string");

        assertTextPresent("Persisted value: [A String]", "Session: [false]");
    }

    @Test
    public void attribute_expansions()
    {
        open(BASE_URL);
        clickAndWait("link=Attribute Expansions Demo");

        assertText("//div[@id='mixed-expansion']/@style", "color: blue;");
        assertText("//div[@id='single']/@class", "red");
        assertText("//div[@id='consecutive']/@class", "goober-red");
        assertText("//div[@id='trailer']/@class", "goober-green");
        assertText(
                "//div[@id='formal']/text()",
                "ALERT-expansions work inside formal component parameters as well");

        // An unrelated test, but fills in a bunch of minor gaps.

        assertSourcePresent("<!-- A comment! -->");
    }

    @Test
    public void palette_component()
    {
        open(BASE_URL);
        clickAndWait("link=Palette Demo");
        clickAndWait("link=reset");

        addSelection("languages:avail", "label=Haskell");
        addSelection("languages:avail", "label=Javascript");
        click("languages:select");

        clickAndWait("//input[@type='submit']");
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
        
        clickAndWait("//input[@type='submit']");
        
        assertTextPresent("[ERLANG, HASKELL, JAVA, LISP, ML, PERL, PYTHON, RUBY]");
  
        check("reorder");
        clickAndWait("//input[@type='submit']");
        
        addSelection("languages", "label=Ruby");

        for (int i = 0; i < 6; i++)
            click("languages:up");

        removeSelection("languages", "label=Ruby");
        addSelection("languages", "label=Perl");

        click("languages:down");
        
        clickAndWait("//input[@type='submit']");

        assertTextPresent("[ERLANG, RUBY, HASKELL, JAVA, LISP, ML, PYTHON, PERL]");
    }

    @Test
    public void event_handler_return_types() {

        open(BASE_URL);
        assertTextPresent("Tapestry 5 Integration Application 1");

        clickAndWait("link=Return Types");
        assertTextPresent("Return Type Tests");

        clickAndWait("link=null");
        assertTextPresent("Return Type Tests");

        clickAndWait("link=string");
        assertTextPresent("Tapestry 5 Integration Application 1");
        goBack();

        clickAndWait("link=class");
        assertTextPresent("Tapestry 5 Integration Application 1");
        goBack();

        clickAndWait("link=page");
        assertTextPresent("Tapestry 5 Integration Application 1");
        goBack();

        clickAndWait("link=link");
        assertTextPresent("Tapestry 5 Integration Application 1");
        goBack();

        clickAndWait("link=stream");
        assertTextPresent("Success!");
        goBack();

        clickAndWait("link=bad");
        assertTextPresent(
                "An unexpected application exception has occurred.",
                "An event handler for component org.apache.tapestry.integration.app1.pages.Start returned the value 20 (from method org.apache.tapestry.integration.app1.pages.Start.onActionFromBadReturnType() (at Start.java:34)). Return type java.lang.Integer can not be handled.");

    }

}
