// Copyright 2006-2014 The Apache Software Foundation
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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.commons.services.TypeCoercer;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.bindings.InternalPropBinding;
import org.apache.tapestry5.internal.services.Instantiator;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.ParameterModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.PageLifecycleListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class InternalComponentResourcesImplTest extends InternalBaseTestCase
{
    private PerthreadManager perThreadManager;

    private ComponentPageElementResources elementResources;

    @BeforeClass
    public void setup()
    {
        perThreadManager = getService(PerthreadManager.class);
        TypeCoercer typeCoercer = getService(TypeCoercer.class);

        elementResources = new ComponentPageElementResourcesImpl(null, null, typeCoercer, null, null, null, null, null,
                null, null, perThreadManager, false, false, null);
    }

    @AfterMethod
    public void cleanup()
    {
        perThreadManager.cleanup();
    }

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

        InternalComponentResources resources = new InternalComponentResourcesImpl(null, element, null,
                elementResources, null, null, ins, false);

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

        InternalComponentResources resources = new InternalComponentResourcesImpl(null, element, null,
                elementResources, null, null, ins, false);

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
        Object rawValue = new Long(97);

        train_getModel(ins, model);

        train_getParameterModel(model, "fred", null);

        train_get(binding, rawValue);

        writer.attributes("fred", "97");

        replay();

        InternalComponentResources resources = new InternalComponentResourcesImpl(null, element, null,
                elementResources, "Foo.bar", null, ins, false);

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

        Object value = new Object();

        train_getModel(ins, model);

        replay();

        ComponentResources resources = new InternalComponentResourcesImpl(null, null, null, elementResources, "id",
                null, ins, false);

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

        train_getModel(ins, model);

        replay();

        ComponentResources resources = new InternalComponentResourcesImpl(null, null, null, elementResources,
                "Foo.bar", null, ins, false);

        resources.storeRenderVariable("fred", "FRED");
        resources.storeRenderVariable("barney", "BARNEY");

        try
        {
            resources.getRenderVariable("wilma");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
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

        train_getModel(ins, model);

        replay();

        InternalComponentResources resources = new InternalComponentResourcesImpl(null, null, null, elementResources,
                "Foo.bar", null, ins, false);

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
                null, ins, false);

        resources.addPageLifecycleListener(listener);

        verify();
    }

    @Test
    public void get_property_name()
    {
        Component component = mockComponent();
        Instantiator ins = mockInstantiator(component);
        ComponentModel model = mockComponentModel();
        ComponentPageElement element = mockComponentPageElement();
        Page page = mockPage();
        Binding binding = mockBinding();

        train_getModel(ins, model);

        replay();

        InternalComponentResources resources = new InternalComponentResourcesImpl(page, element, null, null, null,
                null, ins, false);

        resources.bindParameter("bar", binding);

        assertNull(resources.getPropertyName("bar"));

        verify();
    }

    @Test
    public void get_property_name_internal_prop_binding()
    {
        Component component = mockComponent();
        Instantiator ins = mockInstantiator(component);
        ComponentModel model = mockComponentModel();
        ComponentPageElement element = mockComponentPageElement();
        Page page = mockPage();
        InternalPropBinding binding = newMock(InternalPropBinding.class);

        train_getModel(ins, model);

        expect(binding.getPropertyName()).andReturn("foo");

        replay();

        InternalComponentResources resources = new InternalComponentResourcesImpl(page, element, null, null, null,
                null, ins, false);

        resources.bindParameter("bar", binding);

        assertEquals(resources.getPropertyName("bar"), "foo");

        verify();
    }
}
