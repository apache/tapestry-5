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

package org.apache.tapestry.internal.model;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.model.MutableEmbeddedComponentModel;
import org.apache.tapestry.model.ParameterModel;
import org.testng.annotations.Test;

/**
 * Tests {@link org.apache.tapestry.internal.model.MutableComponentModelImpl} and
 * {@link org.apache.tapestry.internal.model.MutableEmbeddedComponentModelImpl}.
 */
public class MutableComponentModelImplTest extends InternalBaseTestCase
{
    private static final String COMPONENT_CLASS_NAME = "org.example.components.Fred";

    private static final String CLASS_NAME = "org.example.components.Foo";

    @Test
    public void root_class_vs_sub_class()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        assertTrue(model.isRootClass());

        MutableComponentModel subModel = new MutableComponentModelImpl(CLASS_NAME, log, r, model);

        assertFalse(subModel.isRootClass());

        verify();
    }

    @Test
    public void add_new_parameter()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        assertTrue(model.getParameterNames().isEmpty());

        String parameterName = "value";

        model.addParameter(parameterName, true, TapestryConstants.PROP_BINDING_PREFIX);

        ParameterModel pm = model.getParameterModel(parameterName);

        assertEquals(pm.getName(), parameterName);
        assertEquals(true, pm.isRequired());
        assertEquals(pm.getDefaultBindingPrefix(), TapestryConstants.PROP_BINDING_PREFIX);

        assertEquals(model.getDeclaredParameterNames(), Arrays.asList(parameterName));

        // Verify that the binding prefix is actually stored:

        model.addParameter("fred", true, "flint");

        // Checks that parameter names are case insensitive
        
        assertEquals(model.getParameterModel("Fred").getDefaultBindingPrefix(), "flint");

        verify();
    }

    @Test
    public void parameter_names_are_sorted()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        model.addParameter("fred", true, TapestryConstants.PROP_BINDING_PREFIX);
        model.addParameter("wilma", true, TapestryConstants.PROP_BINDING_PREFIX);
        model.addParameter("barney", true, TapestryConstants.PROP_BINDING_PREFIX);

        assertEquals(model.getDeclaredParameterNames(), Arrays.asList("barney", "fred", "wilma"));

        verify();
    }

    @Test
    public void declared_parameter_names_does_not_include_superclass_parameters()
    {
        Resource r = mockResource();
        Log log = mockLog();
        ComponentModel parent = mockComponentModel();

        train_getPersistentFieldNames(parent);
        train_getParameterNames(parent, "betty");

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, parent);

        model.addParameter("fred", true, TapestryConstants.PROP_BINDING_PREFIX);
        model.addParameter("wilma", true, TapestryConstants.PROP_BINDING_PREFIX);
        model.addParameter("barney", true, TapestryConstants.PROP_BINDING_PREFIX);

        assertEquals(model.getDeclaredParameterNames(), Arrays.asList("barney", "fred", "wilma"));
        assertEquals(model.getParameterNames(), Arrays.asList("barney", "betty", "fred", "wilma"));

        verify();
    }

    @Test
    public void add_duplicate_parameter()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        model.addParameter("fred", true, TapestryConstants.PROP_BINDING_PREFIX);

        try
        {
            // This also helps check that the comparison is caseless!
            
            model.addParameter("Fred", true, TapestryConstants.PROP_BINDING_PREFIX);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Parameter 'Fred' of component org.example.components.Foo is already defined.");
        }

        verify();
    }

    @Test
    public void get_parameter_by_name_with_no_parameters_defined()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        assertNull(model.getParameterModel("foo"));

        verify();
    }

    @Test
    public void get_unknown_parameter()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);
        model.addParameter("fred", true, TapestryConstants.PROP_BINDING_PREFIX);

        assertNull(model.getParameterModel("barney"));

        verify();
    }

    @Test
    public void add_embedded()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        assertTrue(model.getEmbeddedComponentIds().isEmpty());

        MutableEmbeddedComponentModel fred = model.addEmbeddedComponent(
                "fred",
                "Fred",
                COMPONENT_CLASS_NAME);

        assertEquals(fred.getId(), "fred");
        assertEquals(fred.getComponentType(), "Fred");

        MutableEmbeddedComponentModel barney = model.addEmbeddedComponent(
                "barney",
                "Barney",
                COMPONENT_CLASS_NAME);

        assertEquals(model.getEmbeddedComponentIds(), Arrays.asList("barney", "fred"));

        assertSame(model.getEmbeddedComponentModel("fred"), fred);
        assertSame(model.getEmbeddedComponentModel("barney"), barney);

        // Access by id is case insensitive
        
        assertSame(model.getEmbeddedComponentModel("FRED"), fred);
        assertSame(model.getEmbeddedComponentModel("BARNEY"), barney);
        
        
        assertEquals(
                fred.toString(),
                "EmbeddedComponentModel[id=fred type=Fred class=org.example.components.Fred]");

        verify();
    }

    @Test
    public void add_embedded_component_with_duplicate_id()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        model.addEmbeddedComponent("fred", "Fred1", COMPONENT_CLASS_NAME);

        try
        {
            model.addEmbeddedComponent("fred", "Fred2", COMPONENT_CLASS_NAME);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Embedded component 'fred' has already been defined for component class org.example.components.Foo.");
        }

        verify();
    }
    
    @Test
    public void add_embedded_is_case_insensitive()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        model.addEmbeddedComponent("fred", "Fred1", COMPONENT_CLASS_NAME);

        try
        {
            model.addEmbeddedComponent("FRED", "Fred2", COMPONENT_CLASS_NAME);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Embedded component 'FRED' has already been defined for component class org.example.components.Foo.");
        }

        verify();      
    }

    @Test
    public void add_parameters_to_embedded()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        MutableEmbeddedComponentModel fred = model.addEmbeddedComponent(
                "fred",
                "Fred",
                COMPONENT_CLASS_NAME);

        assertTrue(fred.getParameterNames().isEmpty());

        fred.addParameter("city", "bedrock");
        fred.addParameter("job", "crane operator");

        assertEquals(fred.getParameterNames(), Arrays.asList("city", "job"));

        assertEquals(fred.getParameterValue("city"), "bedrock");

        verify();
    }

    @Test
    public void add_duplicate_parameters_to_embedded()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        MutableEmbeddedComponentModel fred = model.addEmbeddedComponent(
                "fred",
                "Fred",
                COMPONENT_CLASS_NAME);

        fred.addParameter("city", "bedrock");

        try
        {
            fred.addParameter("city", "slateville");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "A value for parameter 'city' of embedded component fred (of component class org.example.components.Foo) has already been provided.");
        }

        verify();
    }

    @Test
    public void mixin_names_is_initially_empty_list()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        MutableEmbeddedComponentModel fred = model.addEmbeddedComponent(
                "fred",
                "Fred",
                COMPONENT_CLASS_NAME);

        assertTrue(fred.getMixinClassNames().isEmpty());

        verify();
    }

    @Test
    public void mixin_class_names_remembered_in_order_added()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        MutableEmbeddedComponentModel fred = model.addEmbeddedComponent(
                "fred",
                "Fred",
                COMPONENT_CLASS_NAME);

        fred.addMixin("zip.zop.Zoom");
        fred.addMixin("foo.bar.Baz");

        assertEquals(fred.getMixinClassNames(), Arrays.asList("zip.zop.Zoom", "foo.bar.Baz"));

        verify();
    }

    @Test
    public void mixin_name_conflict()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        MutableEmbeddedComponentModel fred = model.addEmbeddedComponent(
                "fred",
                "Fred",
                COMPONENT_CLASS_NAME);

        fred.addMixin("zip.zop.Zoom");

        try
        {
            fred.addMixin("zip.zop.Zoom");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Mixin zip.zop.Zoom (for component fred) has already been defined.");
        }

        // Make sure it wasn't actually added.

        assertEquals(fred.getMixinClassNames(), Arrays.asList("zip.zop.Zoom"));

        verify();
    }

    @Test
    public void get_persistent_field_names_when_none_defined()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        assertTrue(model.getPersistentFieldNames().isEmpty());

        verify();
    }

    @Test
    public void get_persistent_field_names_are_sorted()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        assertEquals(model.setFieldPersistenceStrategy("fred", "session"), "fred");
        assertEquals(model.setFieldPersistenceStrategy("barney", "client"), "barney");

        assertEquals(model.getPersistentFieldNames(), Arrays.asList("barney", "fred"));

        verify();
    }

    @Test
    public void persistent_field_names_have_punctuation_stripped()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        assertEquals(model.setFieldPersistenceStrategy("_fred", "session"), "fred");
        assertEquals(model.setFieldPersistenceStrategy("_$barney", "client"), "barney");

        assertEquals(model.getPersistentFieldNames(), Arrays.asList("barney", "fred"));

        verify();
    }

    @Test
    public void get_persistent_field_names_reflects_parent_model()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel parent = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        assertEquals(parent.setFieldPersistenceStrategy("wilma", "session"), "wilma");

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, parent);

        assertEquals(model.setFieldPersistenceStrategy("fred", "session"), "fred");
        assertEquals(model.setFieldPersistenceStrategy("barney", "client"), "barney");

        assertEquals(model.getPersistentFieldNames(), Arrays.asList("barney", "fred", "wilma"));

        verify();
    }

    @Test
    public void persistent_field_names_allocated_in_subclasses_are_unique()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel parent = new MutableComponentModelImpl(CLASS_NAME, log, r, null);
        assertEquals(parent.setFieldPersistenceStrategy("wilma", "session"), "wilma");

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, parent);

        assertEquals(model.setFieldPersistenceStrategy("wilma", "session"), "wilma_0");

        assertEquals(model.getPersistentFieldNames(), Arrays.asList("wilma", "wilma_0"));

        verify();
    }

    @Test
    public void get_persistent_field_defined_in_model()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        model.setFieldPersistenceStrategy("fred", "session");
        model.setFieldPersistenceStrategy("barney", "client");

        assertEquals(model.getFieldPersistenceStrategy("fred"), "session");
        assertEquals(model.getFieldPersistenceStrategy("barney"), "client");

        verify();
    }

    @Test
    public void no_persistence_defined_for_field()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        try
        {
            model.getFieldPersistenceStrategy("someField");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "No field persistence strategy has been defined for field \'someField\'.");
        }

        verify();

    }

    @Test
    public void get_persistent_field_defined_in_parent()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel parent = new MutableComponentModelImpl(CLASS_NAME, log, r, null);
        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, parent);

        parent.setFieldPersistenceStrategy("wilma", "parent");

        model.setFieldPersistenceStrategy("fred", "session");

        assertEquals(model.getFieldPersistenceStrategy("wilma"), "parent");

        verify();
    }

    @Test
    public void default_for_supports_informal_parameters_is_false()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        assertFalse(model.getSupportsInformalParameters());

        model.enableSupportsInformalParameters();

        assertTrue(model.getSupportsInformalParameters());

        verify();
    }

    @Test
    public void get_mixin_class_names_with_no_mixins()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();
        ComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        assertTrue(model.getMixinClassNames().isEmpty());

        verify();
    }

    @Test
    public void get_mixin_class_names_mixes_with_parent_model()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel parent = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        parent.addMixinClassName("Wilma");

        MutableComponentModel child = new MutableComponentModelImpl(CLASS_NAME, log, r, parent);

        child.addMixinClassName("Fred");
        child.addMixinClassName("Barney");

        assertEquals(child.getMixinClassNames(), Arrays.asList("Barney", "Fred", "Wilma"));

        verify();
    }

    @Test
    public void get_parent_from_subclass()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel parent = new MutableComponentModelImpl(CLASS_NAME, log, r, null);
        MutableComponentModel child = new MutableComponentModelImpl(CLASS_NAME, log, r, parent);

        assertSame(child.getParentModel(), parent);
        assertNull(parent.getParentModel());

        verify();
    }

    @Test
    public void set_and_get_meta()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel model = new MutableComponentModelImpl(CLASS_NAME, log, r, null);

        model.setMeta("fred", "flintstone");
        model.setMeta("barney", "rubble");

        assertEquals(model.getMeta("fred"), "flintstone");
        assertEquals(model.getMeta("barney"), "rubble");

        // Ensure case insensitive:
        
        assertEquals(model.getMeta("FRED"), "flintstone");
        assertEquals(model.getMeta("BARNEY"), "rubble");


        verify();
    }

    @Test
    public void get_meta_from_parent()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel parent = new MutableComponentModelImpl(CLASS_NAME, log, r, null);
        MutableComponentModel child = new MutableComponentModelImpl(CLASS_NAME, log, r, parent);

        parent.setMeta("fred", "flintstone");

        assertEquals(child.getMeta("fred"), "flintstone");

        verify();
    }

    @Test
    public void parent_does_not_have_meta()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel parent = new MutableComponentModelImpl(CLASS_NAME, log, r, null);
        MutableComponentModel child = new MutableComponentModelImpl(CLASS_NAME, log, r, parent);

        parent.setMeta("fred", "flintstone");

        assertNull(child.getMeta("wilma"));

        verify();
    }

    @Test
    public void child_meta_overrides_parent_meta()
    {
        Resource r = mockResource();
        Log log = mockLog();

        replay();

        MutableComponentModel parent = new MutableComponentModelImpl(CLASS_NAME, log, r, null);
        MutableComponentModel child = new MutableComponentModelImpl(CLASS_NAME, log, r, parent);

        parent.setMeta("fred", "flintstone");
        child.setMeta("fred", "mcmurray");

        assertEquals(parent.getMeta("fred"), "flintstone");
        assertEquals(child.getMeta("fred"), "mcmurray");

        verify();
    }
}
