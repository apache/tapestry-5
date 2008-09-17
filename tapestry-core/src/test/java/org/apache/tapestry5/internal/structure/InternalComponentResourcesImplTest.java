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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.services.Instantiator;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.ParameterModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.testng.annotations.Test;

public class InternalComponentResourcesImplTest extends InternalBaseTestCase
{
    @Test
    public void render_informal_parameters_no_bindings()
    {
        ComponentPageElement element = mockComponentPageElement();
        Component component = mockComponent();
        Instantiator ins = mockInstantiator(component);
        MarkupWriter writer = mockMarkupWriter();
        TypeCoercer coercer = mockTypeCoercer();
        ComponentModel model = mockComponentModel();

        train_getModel(ins, model);

        replay();

        InternalComponentResources resources = new InternalComponentResourcesImpl(null, element, null, null, null,
                                                                                  null, ins);

        resources.renderInformalParameters(writer);

        verify();
    }

    @Test
    public void render_informal_parameters_skips_formal_parameters()
    {
        ComponentPageElement element = mockComponentPageElement();
        Component component = mockComponent();
        Instantiator ins = mockInstantiator(component);
        MarkupWriter writer = mockMarkupWriter();
        ComponentModel model = mockComponentModel();
        ParameterModel pmodel = mockParameterModel();
        Binding binding = mockBinding();

        train_getModel(ins, model);

        train_getParameterModel(model, "fred", pmodel);

        replay();

        InternalComponentResources resources = new InternalComponentResourcesImpl(null, element, null, null, null,
                                                                                  null, ins);

        resources.bindParameter("fred", binding);

        resources.renderInformalParameters(writer);

        verify();
    }

    @Test
    public void render_an_informal_parameter()
    {
        ComponentPageElement element = mockComponentPageElement();
        Component component = mockComponent();
        Instantiator ins = mockInstantiator(component);
        MarkupWriter writer = mockMarkupWriter();
        ComponentModel model = mockComponentModel();
        Binding binding = mockBinding();
        Object rawValue = new Object();
        String convertedValue = "*converted*";
        ComponentPageElementResources componentPageElementResources = mockComponentPageElementResources();

        train_getModel(ins, model);

        train_getParameterModel(model, "fred", null);

        train_get(binding, rawValue);

        train_coerce(componentPageElementResources, rawValue, String.class, convertedValue);

        writer.attributes("fred", convertedValue);

        replay();

        InternalComponentResources resources = new InternalComponentResourcesImpl(null, element, null,
                                                                                  componentPageElementResources,
                                                                                  null, null, ins);

        resources.bindParameter("fred", binding);

        resources.renderInformalParameters(writer);

        verify();
    }

    @Test
    public void get_render_variable_exists()
    {
        Component component = mockComponent();
        Instantiator ins = mockInstantiator(component);
        ComponentModel model = mockComponentModel();
        ComponentPageElement element = mockComponentPageElement();

        Object value = new Object();

        train_getModel(ins, model);

        train_isRendering(element, true);

        replay();

        ComponentResources resources = new InternalComponentResourcesImpl(null, element, null, null, null, null, ins);

        resources.storeRenderVariable("myRenderVar", value);

        assertSame(resources.getRenderVariable("myrendervar"), value);

        verify();
    }

    protected final void train_isRendering(ComponentPageElement element, boolean isRendering)
    {
        expect(element.isRendering()).andReturn(isRendering);
    }

    @Test
    public void get_render_variable_missing()
    {
        Component component = mockComponent();
        Instantiator ins = mockInstantiator(component);
        ComponentModel model = mockComponentModel();
        ComponentPageElement element = mockComponentPageElement();

        train_getModel(ins, model);

        train_isRendering(element, true);
        train_isRendering(element, true);

        replay();

        ComponentResources resources = new InternalComponentResourcesImpl(null, element, null, null, "Foo.bar", null,
                                                                          ins);

        resources.storeRenderVariable("fred", "FRED");
        resources.storeRenderVariable("barney", "BARNEY");

        try
        {
            resources.getRenderVariable("wilma");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Component Foo.bar does not contain a stored render variable with name 'wilma'.  Stored render variables: barney, fred.");
        }

        verify();
    }

    @Test
    public void post_render_cleanup_removes_all_variables()
    {
        Component component = mockComponent();
        Instantiator ins = mockInstantiator(component);
        ComponentModel model = mockComponentModel();
        ComponentPageElement element = mockComponentPageElement();

        train_getModel(ins, model);

        train_isRendering(element, true);
        train_isRendering(element, true);

        replay();

        InternalComponentResources resources = new InternalComponentResourcesImpl(null, element, null, null, "Foo.bar",
                                                                                  null, ins);

        resources.storeRenderVariable("fred", "FRED");
        resources.storeRenderVariable("barney", "BARNEY");

        resources.postRenderCleanup();

        try
        {
            resources.getRenderVariable("fred");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Component Foo.bar does not contain a stored render variable with name 'fred'.  Stored render variables: (none).");
        }

        verify();
    }

    @Test
    public void store_render_variable_when_not_rendering()
    {
        Component component = mockComponent();
        Instantiator ins = mockInstantiator(component);
        ComponentModel model = mockComponentModel();
        ComponentPageElement element = mockComponentPageElement();

        train_getModel(ins, model);

        train_isRendering(element, false);

        replay();

        InternalComponentResources resources = new InternalComponentResourcesImpl(null, element, null, null, "Foo.bar",
                                                                                  null, ins);


        try
        {
            resources.storeRenderVariable("fred", "FRED");
            unreachable();
        }
        catch (IllegalStateException ex)
        {
            assertEquals(ex.getMessage(),
                         "Component Foo.bar is not rendering, so render variable 'fred' may not be updated.");
        }

        verify();
    }

    @Test
    public void add_page_lifecycle_listener()
    {
        Component component = mockComponent();
        Instantiator ins = mockInstantiator(component);
        ComponentModel model = mockComponentModel();
        ComponentPageElement element = mockComponentPageElement();
        Page page = mockPage();
        PageLifecycleListener listener = newMock(PageLifecycleListener.class);

        train_getModel(ins, model);

        page.addLifecycleListener(listener);

        replay();

        InternalComponentResources resources = new InternalComponentResourcesImpl(page, element, null, null, null,
                                                                                  null, ins);

        resources.addPageLifecycleListener(listener);

        verify();
    }
}
