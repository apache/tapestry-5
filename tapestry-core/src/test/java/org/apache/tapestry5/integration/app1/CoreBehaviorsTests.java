// Copyright 2009, 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.integration.app1;

import org.apache.tapestry5.corelib.mixins.RenderDisabled;
import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.apache.tapestry5.integration.app1.pages.RenderErrorDemo;
import org.testng.annotations.Test;

public class CoreBehaviorsTests extends TapestryCoreTestCase
{

    @Test
    public void access_to_page_name()
    {
        openBaseURL();

        assertText("activePageName", "Index");

        clickAndWait("link=Grid Demo");

        assertText("activePageName", "GridDemo");
    }

    /**
     * also verifies the use of meta data to set the default strategy.
     */
    @Test
    public void flash_persistence()
    {
        clickThru("FlashDemo");

        assertTextPresent("[]");

        clickAndWait("link=show the message");

        assertTextPresent("[You clicked the link!]");

        clickAndWait("link=refresh the page");

        assertTextPresent("[]");
    }

    @Test
    public void component_parameter_default_from_method() throws Exception
    {
        clickThru("ParameterDefault");

        assertTextPresent("Echo component default: [ParameterDefault:echo]");
    }

    @Test
    public void embedded_components()
    {
        clickThru("Countdown Page");

        assertTextPresent("regexp:\\s+5\\s+4\\s+3\\s+2\\s+1\\s+");

        assertTextPresent("Brought to you by the org.apache.tapestry5.integration.app1.components.Count");
    }

    /**
     * Tests the ability to inject a Block, and the ability to use the block to
     * control rendering.
     */
    @Test
    public void block_rendering() throws Exception
    {
        clickThru("BlockDemo");

        assertTextPresent("[]");

        select("//select[@id='blockName']", "fred");
        waitForPageToLoad(PAGE_LOAD_TIMEOUT);

        assertTextPresent("[Block fred.]");

        select("//select[@id='blockName']", "barney");
        waitForPageToLoad(PAGE_LOAD_TIMEOUT);

        assertTextPresent("[Block barney.]");

        // TAPESTRY-1583

        assertTextPresent("before it is defined: [Block wilma].");
    }

    @Test
    public void environmental()
    {
        clickThru("Environmental Annotation Usage");

        assertSourcePresent("[<strong>A message provided by the RenderableProvider component.</strong>]");
    }

    @Test
    public void exception_report()
    {
        // mismatched tag.
        clickThru("BadTemplate Page");

        assertTextPresent("org.apache.tapestry5.ioc.internal.OperationException",
                "Failure parsing template classpath:org/apache/tapestry5/integration/app1/pages/BadTemplate.tml",
                "The element type \"t:foobar\" must be terminated by the matching end-tag \"</t:foobar>\"",
                "classpath:org/apache/tapestry5/integration/app1/pages/BadTemplate.tml, line 6",
                "<t:foobar>content from template</foobar>");
    }

    @Test
    public void expansion()
    {
        clickThru("Expansion Page");

        assertTextPresent("[value provided by a template expansion]");
    }

    /**
     * {@link org.apache.tapestry5.internal.transform.InjectContainerWorker} is
     * largely tested by the forms tests
     * ({@link RenderDisabled} is built on it). test is for the failure case,
     * where a mixin class is used with the wrong
     * type of component.
     */
    @Test
    public void inject_container_failure() throws Exception
    {
        clickThru("InjectContainerMismatch");

        // And exception message:

        assertTextPresent("Component InjectContainerMismatch is not assignable to field org.apache.tapestry5.corelib.mixins.RenderDisabled.field (of type org.apache.tapestry5.Field).");
    }

    @Test
    public void inject_component_failure() throws Exception
    {
        clickThru("InjectComponentMismatch");

        assertTextPresent("Unable to inject component 'form' into field form of component InjectComponentMismatch. Class org.apache.tapestry5.corelib.components.BeanEditForm is not assignable to a field of type org.apache.tapestry5.corelib.components.Form.");
    }

    @Test
    public void injection() throws Exception
    {
        clickThru("Inject Demo");

        // is a test for a named @Inject:
        assertTextPresent("<Proxy for Request(org.apache.tapestry5.services.Request)>");

        // is a test for an anonymous @Inject and
        // ComponentResourcesInjectionProvider
        assertTextPresent("ComponentResources[InjectDemo]");

        // Another test, DefaultInjectionProvider
        assertTextPresent("<Proxy for BindingSource(org.apache.tapestry5.services.BindingSource)>");

        // Prove that injection using a marker annotation (to match against a
        // marked service) works.

        assertTextPresent("Injection via Marker: Bonjour!");

        assertText("viaInjectService", "1722 tracks in music library");
    }

    @Test
    public void instance_mixin()
    {
        clickThru("InstanceMixin");

        final String[] dates =
        { "Jun 13, 1999", "Jul 15, 2001", "Dec 4, 2005" };

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
        clickThru("Localization");

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
        clickThru("Inject Demo");

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
        clickThru("NumberSelect");

        clickAndWait("link=5");

        assertTextPresent("You chose 5.");
    }

    @Test
    public void render_phase_method_returns_a_component() throws Exception
    {
        clickThru("RenderComponentDemo");

        assertText("//span[@id='container']", "[]");

        // Sneak in a little test for If and parameter else:

        assertTextPresent("Should be blank:");

        clickAndWait("enabled");

        // After clicking the link (which submits the form), the page re-renders
        // and shows us
        // the optional component from inside the NeverRender, resurrected to
        // render on the page
        // after all.

        assertText("//span[@id='container']/span", "Optional Text");

        assertTextPresent("Should now show up:");
    }

    @Test
    public void render_phase_order()
    {
        clickThru("RenderPhaseOrder");

        assertTextPresent("[BEGIN-TRACER-MIXIN BEGIN-ABSTRACT-TRACER BEGIN-TRACER BODY AFTER-TRACER AFTER-ABSTRACT-TRACER AFTER-TRACER-MIXIN]");
    }

    @Test
    public void simple_component_event()
    {
        final String YOU_CHOSE = "You chose: ";

        clickThru("Action Page");

        assertFalse(isTextPresent(YOU_CHOSE));

        for (int i = 2; i < 5; i++)
        {
            clickAndWait("link=" + i);

            assertTextPresent(YOU_CHOSE + i);
        }
    }

    @Test
    public void subclass_inherits_parent_template()
    {
        clickThru("ExpansionSubclass");

        assertTextPresent("[value provided, in the subclass, via a template expansion]");
    }

    @Test
    public void template_overridden()
    {
        clickThru("Template Overridden by Class Page");

        assertTextPresent("Output: ClassValue");
    }

    @Test
    public void pageloaded_lifecycle_method_invoked()
    {
        clickThru("PageLoaded Demo");

        assertTextPresent("[pageLoaded() was invoked.]");
    }

    @Test
    public void navigation_response_from_page_activate() throws Exception
    {
        clickThru("Protected Page");

        assertText("pagetitle", "Security Alert");

        // The message is set by Protected, but is rendered by SecurityAlert.

        assertTextPresent("Access to Protected page is denied");
    }

    @Test
    public void mixed_page_activation_context_and_component_context()
    {
        clickThru("Kicker");

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
        clickThru("Kicker");

        clickAndWait("link=kick target");

        assertTextSeries("//li[%d]", 1, "betty", "wilma", "betty/wilma", "\u82B1\u5B50");

        clickAndWait("link=Target base, no context");

        assertTextPresent("No activation context.");
    }

    @Test
    public void page_link_with_explicit_activation_context()
    {
        clickThru("PageLink Context Demo", "no context");

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

        assertText("//li[1]", "NULL");
    }

    @Test
    public void recursive_components_are_identified_as_errors()
    {
        clickThru("Recursive Demo");

        assertTextPresent(
                "An unexpected application exception has occurred.",
                "The template for component org.apache.tapestry5.integration.app1.components.Recursive is recursive (contains another direct or indirect reference to component org.apache.tapestry5.integration.app1.components.Recursive). This is not supported (components may not contain themselves).",
                "component is <t:recursive>recursive</t:recursive>, so we\'ll see a failure.");
    }

    @Test
    public void render_phase_method_may_return_renderable()
    {
        clickThru("Renderable Demo");

        assertTextPresent("Renderable Demo", "[This proves it works.]");
    }

    @Test
    public void verify_event_handler_invocation_order_and_circumstance()
    {
        String clear = "link=clear";

        clickThru("EventHandler Demo");

        clickAndWait(clear);

        clickAndWait("link=No Context");
        assertText("methodNames", "[parent.eventHandlerZero(), parent.onAction(), child.eventHandlerZeroChild()]");

        clickAndWait(clear);
        clickAndWait("link=Single context value");

        assertText(
                "methodNames",
                "[parent.eventHandlerOne(String), parent.eventHandlerZero(), parent.onAction(String), parent.onAction(), child.eventHandlerOneChild(), child.eventHandlerZeroChild()]");

        clickAndWait(clear);
        clickAndWait("link=Two value context");

        assertText(
                "methodNames",
                "[parent.eventHandlerOne(String), parent.eventHandlerZero(), parent.onAction(String), parent.onAction(), child.eventHandlerOneChild(), child.eventHandlerZeroChild()]");

        clickAndWait(clear);
        clickAndWait("link=Two value context (from fred)");

        assertText(
                "methodNames",
                "[parent.eventHandlerOne(String), parent.eventHandlerZero(), parent.onAction(String), parent.onAction(), child.eventHandlerForFred(), child.eventHandlerOneChild(), child.eventHandlerZeroChild(), child.onActionFromFred(String), child.onActionFromFred()]");
    }

    @Test
    public void inherited_bindings()
    {
        clickThru("Inherited Bindings Demo");

        assertTextPresent("Bound: [ value: the-bound-value, bound: true ]", "Unbound: [ value: null, bound: false ]");
    }

    @Test
    public void client_persistence()
    {
        clickThru("Client Persistence Demo");
        // can't assume session won't exist because other tests use form
        // components w/ defaults, which means
        // session creation to store the ValidationTracker. So we explicitly
        // clear the session here.
        clickAndWait("link=nix session");

        assertTextPresent("Persisted value: []", "Session: [false]");

        clickAndWait("link=store string");

        assertTextPresent("Persisted value: [A String]", "Session: [false]");
    }

    @Test
    public void attribute_expansions()
    {
        clickThru("Attribute Expansions Demo");

        assertAttribute("//div[@id='mixed-expansion']/@style", "color: blue;");
        assertAttribute("//div[@id='single']/@class", "red");
        assertAttribute("//div[@id='consecutive']/@class", "goober-red");
        assertAttribute("//div[@id='trailer']/@class", "goober-green");
        assertText("//div[@id='formal']", "ALERT-expansions work inside formal component parameters as well");

        // An unrelated test, but fills in a bunch of minor gaps.

        assertSourcePresent("<!-- A comment! -->");
    }

    @Test
    public void event_handler_return_types()
    {
        openBaseURL();

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

        /*
         * clickAndWait("link=URL");
         * assertTextPresent("Google>");
         * goBack();
         * waitForPageToLoad();
         */

        clickAndWait("link=bad");
        assertTextPresent(
                "An unexpected application exception has occurred.",
                "A component event handler method returned the value 20. Return type java.lang.Integer can not be handled.",
                "context:ReturnTypes.tml, line 50");
    }

    @Test
    public void missing_template_for_page()
    {
        clickThru("Missing Template Demo");

        assertTextPresent("Page MissingTemplate did not generate any markup when rendered. This could be because its template file could not be located, or because a render phase method in the page prevented rendering.");
    }

    /**
     * This basically checks that the services status page does not error.
     */
    @Test
    public void services_status()
    {
        open(getBaseURL() + "servicestatus");

        assertTextPresent("Tapestry IoC Services Status");
    }

    /**
     * Tests TAPESTRY-1934.
     */
    @Test
    public void base_class_must_be_in_controlled_package() throws Exception
    {
        open(getBaseURL() + "invalidsuperclass", "true");

        assertTextPresent("Base class org.apache.tapestry5.integration.app1.WrongPackageForBaseClass (super class of org.apache.tapestry5.integration.app1.pages.InvalidSuperClass) is not in a controlled package and is therefore not valid. You should try moving the class to package org.apache.tapestry5.integration.app1.base.");
    }

    /**
     * Tests TAPESTRY-2005.
     */
    @Test
    public void components_passed_as_parameters() throws Exception
    {
        clickThru("ComponentParameter Demo");

        // This component is inside a block, and is only rendered because it is
        // passed as a parameter, of type ActionLink,
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
        clickThru("Inherit Informal Parameters Demo");

        assertAttribute("//span[@id='target']/@class", "inherit");
    }

    /**
     * TAPESTRY-1830
     */
    @Test
    public void var_binding()
    {
        clickThru("Var Binding Demo");

        assertTextSeries("//li[%d]", 1, "1", "2", "3");
    }

    /**
     * TAPESTRY-1724
     */
    @Test
    public void component_event_errors()
    {
        clickThru("Exception Event Demo", "enable", "force invalid activation context");

        assertTextPresent("Exception: Exception in method org.apache.tapestry5.integration.app1.pages.ExceptionEventDemo.onActivate(float)");

        clickAndWait("link=force invalid event context");

        assertTextPresent("Exception: Exception in method org.apache.tapestry5.integration.app1.pages.ExceptionEventDemo.onActionFromFail(float)");

        // Revert to normal handling: return null from the onException() event
        // handler method.

        clickAndWait("link=disable");

        clickAndWait("link=force invalid event context");

        assertTextPresent("An unexpected application exception has occurred.",
                "org.apache.tapestry5.runtime.ComponentEventException", "java.lang.NumberFormatException");
    }

    /**
     * TAPESTRY-1518
     */
    @Test
    public void generic_page_type()
    {
        clickThru("Generic Page Class Demo");

        assertTextPresent("Editor for org.apache.tapestry5.integration.app1.data.Track");

        assertText("//label[@for='title']", "Title");
    }

    /**
     * TAPESTRY-2097
     */
    @Test
    public void render_queue_exception()
    {
        clickThru("Render Error Demo");

        assertTextPresent("An unexpected application exception has occurred");

        // Just sample a smattering of the vast amount of data in the exception
        // report.

        assertTextPresent("RenderErrorDemo", "class " + RenderErrorDemo.class.getName(), "RenderErrorDemo:border",
                "RenderErrorDemo:echo");
    }

    /**
     * TAPESTRY-2088
     */
    @Test
    public void primitive_array_as_parameter_type()
    {
        clickThru("Primitive Array Parameter Demo");

        assertSourcePresent("<ul><li>1</li><li>3</li><li>5</li><li>7</li><li>9</li></ul>");
    }

    /**
     * TAPESTRY-1594
     */
    @Test
    public void ignored_paths_filter()
    {
        clickThru("Unreachable Page");

        // This message changes from one release of Jetty to the next sometimes
        assertText("//title", "Error 404 Not Found");
    }

    /**
     * TAPESTRY-2085
     */
    @Test
    public void render_phase_methods_may_throw_checked_exceptions()
    {
        clickThru("Render Phase Method Exception Demo");

        assertTextPresent("Render queue error in BeginRender[RenderPhaseMethodExceptionDemo]: java.sql.SQLException: Simulated JDBC exception while rendering.");
    }

    /**
     * TAPESTRY-2114
     */
    @Test
    public void boolean_properties_can_use_get_or_is_as_method_name_prefix()
    {
        clickThru("Boolean Property Demo", "clear");

        assertText("usingGet", "false");
        assertText("usingIs", "false");

        clickAndWait("link=set");

        assertText("usingGet", "true");
        assertText("usingIs", "true");
    }

    /**
     * TAPESTRY-1475
     */
    @Test
    public void discard_persistent_field_changes()
    {
        clickThru("Persistent Demo");

        assertText("message", "");

        clickAndWait("link=Update the message field");

        assertText("message", "updated");

        clickAndWait("link=Refresh page");

        assertText("message", "updated");

        clickAndWait("link=Discard persistent field changes");

        assertText("message", "");
    }

    /**
     * TAPESTRY-2150. Also demonstrates how to add a ValueEncoder for an entity
     * object, to allow seamless encoding of
     * the entity's id into the URL.
     */
    @Test
    public void nested_page_names()
    {
        clickThru("Music Page", "2");

        assertText("activePageName", "Music");

        clickAndWait("link=The Gift");

        assertText("activePageName", "music/Details");
    }

    /**
     * TAPESTRY-2235
     */
    @Test
    public void generated_activation_context_handlers()
    {
        clickThru("Music Page", "69");

        assertText("activePageName", "Music");

        clickAndWait("link=Wake Me Up (Copy)");

        assertText("activePageName", "music/Details2");

        assertText("//dd[@class='title']", "Wake Me Up");

        assertText("//dd[@class='artist']", "Norah Jones");
    }

    /**
     * TAPESTRY-1999
     */
    @Test
    public void list_as_event_context()
    {
        clickThru("List Event Context Demo");

        assertTextSeries("//ul[@id='eventcontext']/li[%d]", 1, "1", "2", "3");
    }

    /**
     * TAPESTRY-2196
     */
    @Test
    public void protected_field_in_page_class()
    {
        clickThru("Protected Fields Demo", "Trigger the Exception");

        assertTextPresent(
                "An unexpected application exception has occurred.",
                "Class org.apache.tapestry5.integration.app1.pages.ProtectedFields contains field(s) (_field) that are not private. You should change these fields to private, and add accessor methods if needed.");
    }

    /**
     * TAPESTRY-2078
     */
    @Test
    public void noclassdeffound_exception_is_linked_to_underlying_cause()
    {
        clickThru("Class Transformation Exception Demo");

        assertTextPresent("Class org.apache.tapestry5.integration.app1.pages.Datum contains field(s) (_value) that are not private. You should change these fields to private, and add accessor methods if needed.");
    }

    @Test
    public void method_advice()
    {
        clickThru("Method Advice Demo");

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

    @Test
    public void component_classes_may_not_be_directly_instantiated()
    {
        clickThru("Instantiate Page");

        assertTextPresent("Component class org.apache.tapestry5.integration.app1.pages.Music may not be instantiated directly.");
    }

    /**
     * TAPESTRY-2567
     */
    @Test
    public void field_annotation_conflict()
    {
        clickThru("Field Annotation Conflict");

        assertTextPresent("Field flashDemo of class org.apache.tapestry5.integration.app1.pages.FieldAnnotationConflict is already claimed by @org.apache.tapestry5.annotations.InjectPage and can not be claimed by @org.apache.tapestry5.annotations.Parameter.");
    }

    /**
     * TAPESTRY-2610
     */
    @Test
    public void access_to_informal_parameters()
    {
        clickThru("Informal Parameters Demo");

        assertTextSeries("//dl[@id='informals']/dt[%d]", 1, "barney", "fred", "pageName");
        assertTextSeries("//dl[@id='informals']/dd[%d]", 1, "rubble", "flintstone", "InformalParametersDemo");
    }

    /**
     * TAPESTRY-2517
     */
    @Test
    public void cached_exception_for_loading_failed_page()
    {
        clickThru("Failed Field Injection Demo");

        assertTextPresent("Error obtaining injected value for field org.apache.tapestry5.integration.app1.pages.FailedInjectDemo.buffer: No service implements the interface java.lang.StringBuffer.");

        refresh();
        waitForPageToLoad(PAGE_LOAD_TIMEOUT);

        // Before this bug was fixed, this message would not appear; instead on
        // complaining about _$resources would appear which was very confusing.

        assertTextPresent("Error obtaining injected value for field org.apache.tapestry5.integration.app1.pages.FailedInjectDemo.buffer: No service implements the interface java.lang.StringBuffer.");
    }

    /**
     * TAPESTRTY-2644
     */
    @Test
    public void create_page_link_via_page_class()
    {
        clickThru("PageLink via Class Demo");

        assertTextPresent("Demonstrates the use of the @Inject annotation.");
    }

    /**
     * TAP5-256
     */
    @Test
    public void exception_when_attaching_page()
    {
        clickThru("Page Attach Failure");

        assertTextPresent("Failure inside pageAttached().");
    }

    /**
     * TAP5-284
     */
    @Test
    public void default_method_for_parameter_returns_primitive()
    {
        clickThru("Primitive Default Demo");

        assertText("value", "99");
    }

    /**
     * TAP5-285
     */
    @Test
    public void unhandled_client_events_throw_exceptions()
    {
        clickThru("Unhandled Event Demo", "traditional");

        assertTextPresent("Request event 'action' (on component UnhandledEventDemo:traditional) was not handled; you must provide a matching event handler method in the component or in one of its containers.");

        clickThru("Unhandled Event Demo");

        click("link=ajax");

        waitForCSSSelectedElementToAppear("#t-console li");

        assertTextPresent("Communication with the server failed: Request event 'action' (on component UnhandledEventDemo:ajax) was not handled; you must provide a matching event handler method in the component or in one of its containers.");
    }

    /**
     * TAP5-105
     */
    @Test
    public void component_in_class_but_not_template_is_an_exception()
    {
        clickThru("Components Not In Template Demo");

        assertTextPresent("Embedded component(s) form are defined within component class org.apache.tapestry5.integration.app1.pages.ComponentsNotInTemplateDemo");
    }

    /**
     * TAP5-309
     */
    @Test
    public void conflict_between_property_annotation_and_existing_method()
    {
        clickThru("Getter Method Already Exists");

        assertTextPresent("Unable to create new method public java.lang.String getName() as it already exists in class org.apache.tapestry5.integration.app1.pages.GetterMethodAlreadyExists.");
    }

    /**
     * TAP5-181
     */
    @Test
    public void duplicate_ids_highlight_both_locations()
    {
        clickThru("Duplicate IDs");

        assertTextPresent("Component DuplicateIds already contains a child component with id 'index'. Embedded component ids must be unique (excluding case, which is ignored).");
        assertTextPresent("Component DuplicateIds declared original child component with id 'index' in DuplicateIds.tml on line 6.");
    }

    /**
     * TAP5-487
     */
    @Test
    public void published_parameters()
    {
        clickThru("Publish Parameters Demo");

        assertText("p3-where", "PublishParametersDemo:publish1.publish2.publish3");
        assertText("p3-number", "6");
        assertText("p3-value", "{passed to publish1.value}");
    }

    /**
     * TAP5-487
     */
    @Test
    public void conflicting_published_parameter_names_within_same_component()
    {
        clickThru("Duplicate Published Parameter Name");

        assertTextPresent("Parameter 'value' of embedded component 'passwordfield' can not be published as a parameter of "
                + "component org.apache.tapestry5.integration.app1.components.BadPublishDuplicate, "
                + "as it has previously been published by embedded component 'textfield'.");
    }

    @Test
    public void embedded_type_conflict()
    {
        clickThru("Embedded Component Type Conflict");

        assertTextPresent("Embedded component 'input' provides a type attribute in the template ('passwordfield') "
                + "as well as in the component class ('textfield'). You should not provide a type attribute in "
                + "the template when defining an embedded component within the component class.");
    }

    @Test
    public void publish_unknown_parameter()
    {
        clickThru("Publish Unknown Parameter Demo");

        assertTextPresent("Parameter 'xyzzyx' of component org.apache.tapestry5.integration.app1.components.BadPublishUnknown "
                + "is improperly published from embedded component 'publish1' (where it does not exist). "
                + "This may be a typo in the publishParameters attribute of the @Component annotation.");
    }

    @Test
    public void unknown_mixin_id()
    {
        clickThru("Bad Mixin Id Demo");

        assertTextPresent("Mixin id for parameter 'unknownmixinid.foo' not found. Attached mixins: RenderInformals.");
    }

    @Test
    public void duplicate_mixin()
    {
        clickThru("Duplicate Mixin Demo");

        assertTextPresent("Failure creating embedded component 'form' of "
                + "org.apache.tapestry5.integration.app1.pages.DupeMixinDemo: "
                + "Mixins applied to a component must be unique. Mixin 'RenderInformals' has already been applied.");
    }

    @Test
    public void unsupported_informal_block_parameter()
    {
        clickThru("Unsupported Parameter Block Demo");

        assertTextPresent(
                "Exception assembling root component of page UnsupportedParameterBlockDemo:",
                "Component UnsupportedParameterBlockDemo:outputraw does not include a formal parameter 'unexpected' (and does not support informal parameters).");
    }

    /**
     * TAP5-74
     */
    @Test
    public void component_extends_parent_template()
    {
        clickThru("Template Override Demo");

        // From the parent template (could be overridden, but is not).

        assertText("title", "Template Override Demo");

        // Overriden by <t:replace> in the child component

        assertText("pagecontent", "Content from TemplateOverrideDemo.tml");
    }

    @Test
    public void extend_without_base_template()
    {
        clickThru("Invalid Template Extend Demo");

        assertTextPresent("Component org.apache.tapestry5.integration.app1.pages.InvalidTemplateExtend uses an extension template, but does not have a parent component.");
    }

    /**
     * TAP5-578
     */
    @Test
    public void abstract_component_class()
    {
        clickThru("Abstract Component Demo");

        assertTextPresent(
                "java.lang.RuntimeException",
                "Component class org.apache.tapestry5.integration.app1.components.AbstractComponent is abstract and can not be instantiated.");
    }

    @Test
    public void multi_level_parameter_inheritance()
    {
        clickThru("Multi-Level Inherit Demo");

        assertText("prop.middle.bottom", "bound value");
        assertText("literal.middle.bottom", "some text");
    }

    @Test
    public void bindparameter()
    {
        clickThru("BindParameter mixin annotation");
        // implicit parameter name
        assertEchoMixins("testmixin", "mypropertyvalue", 0, -1, -1, 1, true);
        assertText("mypropertyoutput", "mypropertyvalue");

        // explicit parameter name
        assertEchoMixins("testmixin2", "10", -1, 0, -1, 2, true);
        assertText("mypropertyoutput2", "10");

        // multiple parameter names; first one found wins.
        assertEchoMixins("testmixin3", "hello", -1, -1, 0, 3, true);

        // multiple mixins
        assertEchoMixins("multimixins", "supervalue", 0, 1, 2, 3, true);
        assertText("mypropertyoutput4", "supervalue");

        // finally, binding to default bindings (which is tricky because of page
        // load invocation order)
        assertEchoMixins("defaultbinding", "goodbye", 0, -1, -1, 1, false);
        assertText("mypropertyoutput5", "goodbye");
    }

    /**
     * asserts that the "echo value" mixins are properly functioning (ie
     * 
     * @BindParameter, and mixin ordering).
     *                 each integer value specifies the echo mixin number (echovalue => 1,
     *                 echovalue2 => 2, echovalue3 => 3; 0 is the original value)
     *                 from which the specified echo mixin is expected to "receive" its value.
     *                 So if echo1From is 2, then the "original value"
     *                 printed by echo1 is expected to be the value set by echo2. If a given
     *                 "from" is < 0, checking the corresponding mixin values is disabled.
     */

    private void assertEchoMixins(String fieldName, String originalValue, int echo1From, int echo2From, int echo3From,
            int fieldFrom, boolean isField)
    {
        String[] vals =
        { originalValue, "temporaryvaluefromechovaluemixin", "3", "world" };
        String before = fieldName + "_before";
        String after = fieldName + "_after";
        if (echo1From > -1)
        {
            assertText(before, vals[echo1From] + "-before");
            assertText(after, vals[echo1From] + "-after");
        }
        if (echo2From > -1)
        {
            assertText(before + "2", "echo2-" + vals[echo2From] + "-before");
            assertText(after + "2", "echo2-" + vals[echo2From] + "-after");
        }
        if (echo3From > -1)
        {
            assertText(before + "3", "echo3-" + vals[echo3From] + "-before");
            assertText(after + "3", "echo3-" + vals[echo3From] + "-after");
        }
        if (isField)
            assertFieldValue(fieldName, vals[fieldFrom]);
        else
            assertText(fieldName, vals[fieldFrom]);
    }

    @Test
    public void missing_componentclass()
    {
        clickThru("Missing Component Class Exception");
        assertTextPresent(
                "An unexpected application exception has occurred",
                "Failure creating embedded component 'componentwithnotype' of org.apache.tapestry5.integration.app1.pages.MissingComponentClassException: You must specify the type via t:type, the element, or @Component");
    }

    @Test
    public void session_attribute()
    {
        clickThru("SessionAttribute Demo");

        assertTextPresent("Foo");
        assertTextPresent("Bar");

        clickAndWait("link=Read SessionAttribute");

        assertTextPresent("read Foo");
        assertTextPresent("read Bar");
    }

    /**
     * TAPESTRY-1598
     */
    @Test
    public void value_encoder_via_type_coercer()
    {
        clickThru("Magic ValueEncoder Demo");

        select("number", "25");

        clickAndWait(SUBMIT);

        String locator = "//span[@id='selectednumber']";

        assertText(locator, "25");

        select("number", "100");
        clickAndWait(SUBMIT);

        assertText(locator, "100");
    }

    /**
     * TAPESTRY-2184
     */
    @Test
    public void create_action_link_while_not_rendering()
    {
        clickThru("Action via Link Demo", "via explicit Link creation");

        assertText("message", "from getActionURL()");
    }

    /**
     * TAPESTRY-2244
     */
    @Test
    public void cached()
    {
        clickThru("Cached Annotation");

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
        clickThru("Cached Annotation2");

        assertText("value", "111");

        clickAndWait("link=Back to index");

        // TAPESTRY-2338: Make sure the data is cleared.

        clickAndWait("link=Cached Annotation2");

        assertText("value", "111");
    }

    /**
     * TAPESTRY-2542
     */
    @Test
    public void has_body()
    {
        clickThru("Has Body Demo");

        assertText("nobody", "false");
        assertText("somebody", "true");
    }

    @Test
    public void bindparameter_nomatchingparameter()
    {
        clickThru("BindParameter error handling");

        assertTextPresent(
                "An unexpected application exception has occurred.",

                "Failure binding parameter field 'boundParameter' of mixin BindParameterNoSuchParameter:throwexception$echovalue2 (type org.apache.tapestry5.integration.app1.mixins.EchoValue2)",

                "Containing component org.apache.tapestry5.corelib.components.Any does not contain a formal parameter matching any of boundParameter, value.");
    }

    @Test
    public void bindparameter_on_componentfield_throws_exception()
    {
        clickThru("BindParameter on component");

        assertTextPresent(
                "An unexpected application exception has occurred.",
                "@BindParameter was used on field 'value' of component class 'org.apache.tapestry5.integration.app1.components.BindParameterComponent', but @BindParameter should only be used in mixins.");
    }

    @Test
    public void trigger_demo()
    {
        clickThru("Trigger Demo");

        assertTextPresent("Event 'provideAdditionalMarkup' handled.");
    }

    @Test
    public void xml_content() throws Exception
    {
        open(getBaseURL() + "xmlcontent");

        // Commented out ... Selenium can't seem to handle an XML response.

        // assertSourcePresent("<![CDATA[< & >]]>");
    }

    @Test
    public void secure_page_access()
    {
        clickThru("Secure Page Demo");

        assertText("secure", "secure");

        assertText("message", "Triggered from Index");

        clickAndWait("link=click");

        assertText("secure", "secure");

        assertText("message", "Link clicked");

        clickAndWait(SUBMIT);

        assertText("secure", "secure");
        assertText("message", "Form submitted");

        clickAndWait("link=Back to index");

        // Back to the insecure home page.

        assertText("//h1", "Tapestry Integration Test Application");
    }

    /** TAP5-815 */
    @Test
    public void test_asset_protection()
    {
        // Have to watch out for minor differences in error messages from one version of Jetty to
        // the next.

        // context resources should be available by default.
        clickThru("Asset Protection Demo");
        clickAndWait("link=Available File");
        assertTextPresent("This file should be available to clients.");

        clickThru("Asset Protection Demo");
        clickAndWait("link=Unavailable CSS");
        assertTextPresent("HTTP ERROR 404");

        clickThru("Asset Protection Demo");
        clickAndWait("link=WEB-INF");
        assertTextPresent("HTTP ERROR 404");

        clickThru("Asset Protection Demo");
        clickAndWait("link=WEB-INF/");
        assertTextPresent("HTTP ERROR 404");

        clickThru("Asset Protection Demo");
        clickAndWait("link=Available File2");
        assertTextPresent("This file should be available to clients.");
    }

    /** TAP5-964 */
    @Test
    public void failure_inside_default_object_renderer()
    {
        clickThru("RenderObject Exception Demo");

        assertText(
                "container",
                "Exception rendering description for object of type org.apache.tapestry5.integration.app1.data.NullToString: (java.lang.NullPointerException) NPE from NullToString");
    }

    /** TAP5-966 */

    @Test
    public void module_loading()
    {
        clickThru("Test Only Service Demo");

        assertText("message", "TestOnly service message");
    }

    /** TAP5-948 */
    @Test
    public void page_reset_annotation()
    {
        clickThru("PageReset Annotation Demo");

        assertText("current", "0");

        clickAndWait("link=increment");

        assertText("current", "1");

        clickAndWait("link=increment");

        assertText("current", "2");

        clickAndWait("link=refresh");

        assertText("current", "2");

        clickAndWait("link=Back to index");
        clickAndWait("link=PageReset Annotation Demo");

        assertText("current", "0");
    }

    /** TAP5-948 */
    @Test
    public void page_reset_annotation_on_bad_method()
    {
        clickThru("PageReset Annotation Failure");

        assertTextPresent(
                "Method org.apache.tapestry5.integration.app1.pages.PageResetFailure.reset(java.lang.String)",
                "is invalid: methods with the @PageReset annotation must return void, and have no parameters.");
    }

    /** TAP5-1056 */
    @Test
    public void injection_of_application_message_catalog_into_service()
    {
        clickThru("Inject Global Messages into Service Demo");

        assertText("status", "Application Catalog Working");
    }

    /** TAP5-1121 */
    @Test
    public void discard_after()
    {
        clickThru("@DiscardAfter Demo");

        type("stringValue", "foo bar baz");

        clickAndWait("//input[@id='keep']");

        assertTextPresent("Value is: 'foo bar baz'");

        clickAndWait("//input[@id='discard']");

        assertTextPresent("Value is: ''");

        // Once again

        type("stringValue", "barney quux");

        clickAndWait("//input[@id='keep']");

        assertTextPresent("Value is: 'barney quux'");

        clickAndWait("//input[@id='discardWithCheckedException']");

        assertTextPresent("Oops! Error occured");

        clickThru("@DiscardAfter Demo");

        assertTextPresent("Value is: 'barney quux'");
    }

    /** TAP5-1080 */
    @Test
    public void context_lost_on_secure_page_redirect()
    {
        open("/securepage/mycontext");

        assertText("context", "mycontext");
    }

    /** TAP5-424 */
    @Test
    public void multiple_resources_contributed_to_global_message_catalog()
    {
        clickThru("Library Messages Demo");

        assertText("id=no-override", "[pre-app]");
        assertText("id=override", "[app]");
    }
    
    /** TAP5-978 */
    @Test
    public void remote_pool_management()
    {
        clickThru("Remote Pool Management");

        assertTextPresent("SoftWait: 10");
    }
}
