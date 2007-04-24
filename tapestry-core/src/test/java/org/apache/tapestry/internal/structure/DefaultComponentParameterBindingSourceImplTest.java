// Copyright 2007 The Apache Software Foundation
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
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry.ioc.services.PropertyAccess;
import org.apache.tapestry.ioc.services.PropertyAdapter;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.BindingSource;
import org.apache.tapestry.services.DefaultComponentParameterBindingSource;
import org.apache.tapestry.test.TapestryTestCase;
import org.testng.annotations.Test;

public class DefaultComponentParameterBindingSourceImplTest extends TapestryTestCase
{
    @Test
    public void no_matching_property()
    {
        String parameterName = "myparam";

        String id = "mycomponentid";

        ComponentResources resources = mockComponentResources();
        Component container = mockComponent();
        PropertyAccess access = newPropertyAccess();
        ClassPropertyAdapter classPropertyAdapter = newClassPropertyAdapter();
        BindingSource bindingSource = mockBindingSource();

        train_getId(resources, id);
        train_getContainer(resources, container);

        train_getAdapter(access, container, classPropertyAdapter);
        train_getPropertyAdapter(classPropertyAdapter, id, null);

        replay();

        DefaultComponentParameterBindingSource source = new DefaultComponentParameterBindingSourceImpl(
                access, bindingSource);

        assertNull(source.createDefaultBinding(parameterName, resources));

        verify();
    }

    @Test
    public void property_exists()
    {
        String parameterName = "myparam";

        String id = "mycomponentid";

        ComponentResources resources = mockComponentResources();
        Component container = mockComponent();
        PropertyAccess access = newPropertyAccess();
        ClassPropertyAdapter classPropertyAdapter = newClassPropertyAdapter();
        PropertyAdapter propertyAdapter = newPropertyAdapter();
        BindingSource bindingSource = mockBindingSource();
        Binding binding = mockBinding();
        ComponentResources containerResources = mockComponentResources();

        train_getId(resources, id);
        train_getContainer(resources, container);

        train_getAdapter(access, container, classPropertyAdapter);
        train_getPropertyAdapter(classPropertyAdapter, id, propertyAdapter);

        train_getContainerResources(resources, containerResources);

        train_newBinding(
                bindingSource,
                "default myparam",
                containerResources,
                TapestryConstants.PROP_BINDING_PREFIX,
                id,
                binding);

        replay();

        DefaultComponentParameterBindingSource source = new DefaultComponentParameterBindingSourceImpl(
                access, bindingSource);

        assertSame(source.createDefaultBinding(parameterName, resources), binding);

        verify();
    }

    protected final PropertyAdapter newPropertyAdapter()
    {
        return newMock(PropertyAdapter.class);
    }

    protected final ClassPropertyAdapter newClassPropertyAdapter()
    {
        return newMock(ClassPropertyAdapter.class);
    }

    protected final PropertyAccess newPropertyAccess()
    {
        return newMock(PropertyAccess.class);
    }

    protected final void train_getPropertyAdapter(ClassPropertyAdapter classPropertyAdapter,
            String propertyName, PropertyAdapter propertyAdapter)
    {
        expect(classPropertyAdapter.getPropertyAdapter(propertyName)).andReturn(propertyAdapter)
                .atLeastOnce();
    }

    protected final void train_getAdapter(PropertyAccess access, Object object,
            ClassPropertyAdapter classPropertyAdapter)
    {
        expect(access.getAdapter(object)).andReturn(classPropertyAdapter);
    }
}
