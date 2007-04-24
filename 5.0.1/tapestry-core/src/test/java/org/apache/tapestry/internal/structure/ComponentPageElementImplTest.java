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

package org.apache.tapestry.internal.structure;

import org.apache.tapestry.Binding;
import org.apache.tapestry.Block;
import org.apache.tapestry.BlockNotFoundException;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.internal.InternalComponentResources;
import org.apache.tapestry.internal.services.Instantiator;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.model.ParameterModel;
import org.apache.tapestry.runtime.Component;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

public class ComponentPageElementImplTest extends InternalBaseTestCase
{
    public static final String PAGE_NAME = "org.test.pages.Foo";

    private Page newPage(String pageName)
    {
        Page page = newPage();

        train_getName(page, pageName);

        return page;
    }

    @Test
    public void block_not_found()
    {
        Page page = newPage(PAGE_NAME);
        Component component = newComponent();
        ComponentModel model = newComponentModel();
        TypeCoercer coercer = newTypeCoercer();

        Instantiator ins = newInstantiator(component, model);

        replay();

        ComponentPageElement cpe = new ComponentPageElementImpl(page, ins, coercer, null);

        ComponentResources resources = cpe.getComponentResources();

        try
        {
            resources.getBlock("notFound");
            unreachable();
        }
        catch (BlockNotFoundException ex)
        {
            assertTrue(ex.getMessage().contains(
                    "does not contain a block with identifier 'notFound'."));
        }

        verify();
    }

    @Test
    public void block_found()
    {
        Page page = newPage(PAGE_NAME);
        Component component = newComponent();
        ComponentModel model = newComponentModel();
        TypeCoercer coercer = newTypeCoercer();
        Block block = newBlock();

        Instantiator ins = newInstantiator(component, model);

        replay();

        ComponentPageElement cpe = new ComponentPageElementImpl(page, ins, coercer, null);

        ComponentResources resources = cpe.getComponentResources();

        cpe.addBlock("known", block);

        assertSame(resources.getBlock("known"), block);
        // Caseless check
        assertSame(resources.getBlock("KNOWN"), block);

        verify();
    }

    @Test
    public void parameter_is_bound()
    {
        Page page = newPage(PAGE_NAME);
        Component component = newComponent();
        ComponentModel model = newComponentModel();
        Binding binding = newBinding();
        TypeCoercer coercer = newTypeCoercer();

        Instantiator ins = newInstantiator(component, model);

        train_getParameterModel(model, "barney", null);

        train_getSupportsInformalParameters(model, true);

        replay();

        ComponentPageElement cpe = new ComponentPageElementImpl(page, ins, coercer, null);

        ComponentResources resources = cpe.getComponentResources();
        assertFalse(resources.isBound("fred"));

        cpe.bindParameter("barney", binding);

        assertFalse(resources.isBound("fred"));
        assertTrue(resources.isBound("barney"));

        verify();
    }

    @Test
    public void duplicate_block_id()
    {
        Page page = newPage(PAGE_NAME);
        Component component = newComponent();
        ComponentModel model = newComponentModel();
        TypeCoercer coercer = newTypeCoercer();
        Block block1 = newBlock();
        Block block2 = newBlock();

        Instantiator ins = newInstantiator(component, model);

        replay();

        ComponentPageElement cpe = new ComponentPageElementImpl(page, ins, coercer, null);

        cpe.addBlock("myblock", block1);

        try
        {
            cpe.addBlock("MyBlock", block2);
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Component org.test.pages.Foo already contains a block with id \'MyBlock\'. Block ids must be unique (excluding case, which is ignored).");
        }

        verify();
    }

    @Test
    public void verify_required_parameters_all_are_bound()
    {
        Page page = newPage(PAGE_NAME);
        Component component = newComponent();
        ComponentModel model = newComponentModel();
        Binding binding = newBinding();
        TypeCoercer coercer = newTypeCoercer();
        ParameterModel pmodel = newParameterModel();

        Instantiator ins = newInstantiator(component, model);

        train_getParameterNames(model, "barney");
        train_getParameterModel(model, "barney", pmodel);

        component.containingPageDidLoad();

        replay();

        ComponentPageElementImpl cpe = new ComponentPageElementImpl(page, ins, coercer, null);

        cpe.bindParameter("barney", binding);

        cpe.containingPageDidLoad();

        verify();
    }

    @Test
    public void verify_required_parameters_unbound_but_not_required()
    {
        Page page = newPage(PAGE_NAME);
        Component component = newComponent();
        ComponentModel model = newComponentModel();
        ParameterModel pmodel = newParameterModel();
        TypeCoercer coercer = newTypeCoercer();

        Instantiator ins = newInstantiator(component, model);

        train_getParameterNames(model, "barney");
        train_getParameterModel(model, "barney", pmodel);
        train_isRequired(pmodel, false);

        component.containingPageDidLoad();

        replay();

        ComponentPageElementImpl cpe = new ComponentPageElementImpl(page, ins, coercer, null);

        cpe.containingPageDidLoad();

        verify();
    }

    @Test
    public void verify_required_parameters_unbound_and_required()
    {
        Page page = newPage();
        ComponentPageElement container = newComponentPageElement();
        InternalComponentResources containerResources = newInternalComponentResources();
        Component component = newComponent();
        ComponentModel model = newComponentModel();
        ParameterModel pmodel = newParameterModel();
        Location l = newLocation();
        TypeCoercer coercer = newTypeCoercer();

        Instantiator ins = newInstantiator(component, model);

        train_getComponentResources(container, containerResources);

        train_getNestedId(container, null);
        train_getName(page, "foo.pages.MyPage");

        train_getParameterNames(model, "wilma", "barney", "fred");
        train_getParameterModel(model, "wilma", pmodel);
        train_isRequired(pmodel, true);

        train_getParameterModel(model, "barney", pmodel);
        train_isRequired(pmodel, false);

        train_getParameterModel(model, "fred", pmodel);
        train_isRequired(pmodel, true);

        // Now called *before* the check for unbound parametesr

        component.containingPageDidLoad();

        train_getComponentClassName(model, "foo.components.MyComponent");

        replay();

        ComponentPageElementImpl cpe = new ComponentPageElementImpl(page, container, "myid", null,
                ins, coercer, null, l);

        try
        {
            cpe.containingPageDidLoad();
        }
        catch (TapestryException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Parameter(s) fred, wilma are required for foo.components.MyComponent, but have not been bound.");
            assertSame(ex.getLocation(), l);
        }

        verify();
    }

    @Test
    public void is_invariant()
    {
        Page page = newPage(PAGE_NAME);
        Component component = newComponent();
        ComponentModel model = newComponentModel();
        Binding binding = newBinding();
        TypeCoercer coercer = newTypeCoercer();
        ParameterModel pmodel = newParameterModel();

        Instantiator ins = newInstantiator(component, model);

        train_getParameterModel(model, "barney", pmodel);

        train_isInvariant(binding, true);

        replay();

        ComponentPageElement cpe = new ComponentPageElementImpl(page, ins, coercer, null);

        assertFalse(cpe.getComponentResources().isInvariant("fred"));

        cpe.bindParameter("barney", binding);

        assertFalse(cpe.getComponentResources().isInvariant("fred"));
        assertTrue(cpe.getComponentResources().isInvariant("barney"));

        verify();
    }

    @Test
    public void read_binding()
    {
        Page page = newPage(PAGE_NAME);
        Component component = newComponent();
        ComponentModel model = newComponentModel();
        TypeCoercer coercer = newTypeCoercer();
        Binding binding = newBinding();

        train_getSupportsInformalParameters(model, true);

        Long boundValue = new Long(23);

        Instantiator ins = newInstantiator(component, model);

        train_getParameterModel(model, "barney", null);

        train_get(binding, boundValue);

        train_coerce(coercer, boundValue, Long.class, boundValue);

        replay();

        ComponentPageElement cpe = new ComponentPageElementImpl(page, ins, coercer, null);

        cpe.bindParameter("barney", binding);

        assertSame(cpe.getComponentResources().readParameter("barney", Long.class), boundValue);

        verify();
    }

    @Test
    public void write_binding()
    {
        Page page = newPage(PAGE_NAME);
        Component component = newComponent();
        ComponentModel model = newComponentModel();
        TypeCoercer coercer = newTypeCoercer();
        Binding binding = newBinding();

        Instantiator ins = newInstantiator(component, model);

        train_getParameterModel(model, "barney", null);

        train_getSupportsInformalParameters(model, true);

        expect(binding.getBindingType()).andReturn(Integer.class);

        train_coerce(coercer, 23, Integer.class, 23);

        binding.set(23);

        replay();

        ComponentPageElement cpe = new ComponentPageElementImpl(page, ins, coercer, null);

        cpe.bindParameter("barney", binding);

        cpe.getComponentResources().writeParameter("barney", 23);

        verify();
    }

    @Test
    public void get_embedded_does_not_exist()
    {
        Page page = newPage(PAGE_NAME);
        Component component = newComponent();
        ComponentModel model = newComponentModel();
        TypeCoercer coercer = newTypeCoercer();

        Instantiator ins = newInstantiator(component, model);

        replay();

        ComponentPageElement cpe = new ComponentPageElementImpl(page, ins, coercer, null);

        try
        {
            cpe.getEmbeddedElement("unknown");
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertEquals(ex.getMessage(), "Component " + PAGE_NAME
                    + " does not contain an embedded component with id 'unknown'.");
        }

        verify();
    }

    @Test
    public void get_existing_embedded_component()
    {
        Page page = newPage(PAGE_NAME);
        Component component = newComponent();
        ComponentModel model = newComponentModel();
        ComponentPageElement childElement = newComponentPageElement();
        Component childComponent = newComponent();
        TypeCoercer coercer = newTypeCoercer();

        Instantiator ins = newInstantiator(component, model);

        train_getId(childElement, "child");
        train_getComponent(childElement, childComponent);

        replay();

        ComponentPageElement cpe = new ComponentPageElementImpl(page, ins, coercer, null);

        cpe.addEmbeddedElement(childElement);

        assertSame(cpe.getComponentResources().getEmbeddedComponent("child"), childComponent);

        // Now check that the search is caseless.

        assertSame(cpe.getComponentResources().getEmbeddedComponent("CHILD"), childComponent);

        verify();
    }

    @Test
    public void component_ids_must_be_unique_within_container()
    {
        Page page = newPage(PAGE_NAME);
        Component pageComponent = newComponent();
        ComponentModel model = newComponentModel();
        ComponentPageElement child1 = newComponentPageElement();
        ComponentPageElement child2 = newComponentPageElement();
        TypeCoercer coercer = newTypeCoercer();

        Instantiator ins = newInstantiator(pageComponent, model);

        train_getId(child1, "Child");
        train_getId(child2, "CHILD");

        replay();

        ComponentPageElement cpe = new ComponentPageElementImpl(page, ins, coercer, null);

        cpe.addEmbeddedElement(child1);

        try
        {
            cpe.addEmbeddedElement(child2);
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertTrue(ex.getMessage().contains(
                    "already contains a child component with id 'child'."));
        }

        verify();
    }

    @Test
    public void get_mixin_by_class_name()
    {
        Page page = newPage(PAGE_NAME);
        Component component = newComponent();
        ComponentModel model = newComponentModel();
        TypeCoercer coercer = newTypeCoercer();
        final String mixinClassName = "foo.Bar";
        Component mixin = newComponent();
        ComponentModel mixinModel = newComponentModel();

        Instantiator ins = newInstantiator(component, model);
        Instantiator mixinIns = newInstantiator(mixin, mixinModel);

        train_getComponentClassName(mixinModel, mixinClassName);

        replay();

        ComponentPageElement cpe = new ComponentPageElementImpl(page, ins, coercer, null);

        cpe.addMixin(mixinIns);

        assertSame(cpe.getMixinByClassName(mixinClassName), mixin);

        verify();
    }

    @Test
    public void get_mixin_by_unknown_class_name()
    {
        Page page = newPage(PAGE_NAME);
        Component component = newComponent();
        ComponentModel model = newComponentModel();
        TypeCoercer coercer = newTypeCoercer();
        Component mixin = newComponent();
        ComponentModel mixinModel = newComponentModel();

        Instantiator ins = newInstantiator(component, model);
        Instantiator mixinIns = newInstantiator(mixin, mixinModel);

        train_getComponentClassName(mixinModel, "foo.Bar");

        replay();

        ComponentPageElement cpe = new ComponentPageElementImpl(page, ins, coercer, null);

        cpe.addMixin(mixinIns);

        try
        {
            cpe.getMixinByClassName("foo.Baz");
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertTrue(ex.getMessage().endsWith(" does not contain a mixin of type foo.Baz."));
        }

        verify();

    }

    @Test
    public void set_explicit_parameter_of_unknown_mixin()
    {
        Page page = newPage(PAGE_NAME);
        Component component = newComponent();
        ComponentModel model = newComponentModel();
        ComponentModel mixinModel = newComponentModel();
        Component mixin = newComponent();
        TypeCoercer coercer = newTypeCoercer();
        Binding binding = newBinding();

        Instantiator ins = newInstantiator(component, model);
        Instantiator mixinInstantiator = newInstantiator(mixin, mixinModel);

        train_getComponentClassName(mixinModel, "foo.Fred");

        replay();

        ComponentPageElement cpe = new ComponentPageElementImpl(page, ins, coercer, null);

        cpe.addMixin(mixinInstantiator);

        try
        {
            cpe.bindParameter("Wilma.barney", binding);
            unreachable();
        }
        catch (TapestryException ex)
        {
            assertTrue(ex.getMessage().contains(
                    "does not contain a mixin named 'Wilma' (setting parameter 'Wilma.barney')"));
        }

        verify();
    }

    private Instantiator newInstantiator(Component component, ComponentModel model)
    {
        Instantiator ins = newMock(Instantiator.class);

        expect(ins.getModel()).andReturn(model).anyTimes();

        expect(ins.newInstance(EasyMock.isA(InternalComponentResources.class)))
                .andReturn(component);

        return ins;
    }
}
