// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.integration.core;

import org.apache.tapestry5.corelib.mixins.RenderDisabled;
import org.apache.tapestry5.integration.TapestryCoreTestCase;
import org.testng.annotations.Test;

public class CoreBehaviorsTest extends TapestryCoreTestCase
{

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

        assertTextPresent(
                "org.apache.tapestry5.ioc.internal.util.TapestryException",
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

        assertTextPresent(
                "Unable to inject component 'form' into field form of component InjectComponentMismatch. Class org.apache.tapestry5.corelib.components.BeanEditForm is not assignable to a field of type org.apache.tapestry5.corelib.components.Form.",
                "ClassCastException");
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
}
