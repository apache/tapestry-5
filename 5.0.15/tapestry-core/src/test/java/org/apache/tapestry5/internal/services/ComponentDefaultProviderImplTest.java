// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.FieldTranslator;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.PropertyAdapter;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.BindingSource;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.FieldTranslatorSource;
import org.testng.annotations.Test;

public class ComponentDefaultProviderImplTest extends InternalBaseTestCase
{
    @Test
    public void default_label_key_exists()
    {
        ComponentResources resources = mockComponentResources();
        ComponentResources container = mockComponentResources();
        Messages messages = mockMessages();

        String componentId = "myfield";
        String key = componentId + "-label";
        String message = "My Lovely Field";

        train_getId(resources, componentId);
        train_getContainerResources(resources, container);
        train_getMessages(container, messages);
        train_contains(messages, key, true);
        train_get(messages, key, message);

        replay();

        ComponentDefaultProvider provider = new ComponentDefaultProviderImpl(null, null, null, null, null);

        assertSame(provider.defaultLabel(resources), message);

        verify();
    }

    @Test
    public void default_label_key_missing()
    {
        ComponentResources resources = mockComponentResources();
        ComponentResources container = mockComponentResources();
        Messages messages = mockMessages();

        String componentId = "myField";
        String key = componentId + "-label";

        train_getId(resources, componentId);
        train_getContainerResources(resources, container);
        train_getMessages(container, messages);
        train_contains(messages, key, false);

        replay();

        ComponentDefaultProvider provider = new ComponentDefaultProviderImpl(null, null, null, null, null);

        assertEquals(provider.defaultLabel(resources), "My Field");

        verify();
    }

    @Test
    public void no_matching_property_for_default()
    {
        String parameterName = "myparam";

        String id = "mycomponentid";

        ComponentResources resources = mockComponentResources();
        Component container = mockComponent();
        PropertyAccess access = mockPropertyAccess();
        ClassPropertyAdapter classPropertyAdapter = mockClassPropertyAdapter();
        BindingSource bindingSource = mockBindingSource();

        train_getId(resources, id);
        train_getContainer(resources, container);

        train_getAdapter(access, container, classPropertyAdapter);
        train_getPropertyAdapter(classPropertyAdapter, id, null);

        replay();

        ComponentDefaultProvider source = new ComponentDefaultProviderImpl(access, bindingSource, null,
                                                                           null, null);

        assertNull(source.defaultBinding(parameterName, resources));

        verify();
    }

    @Test
    public void default_property_exists()
    {
        String parameterName = "myparam";

        String id = "mycomponentid";

        ComponentResources resources = mockComponentResources();
        Component container = mockComponent();
        PropertyAccess access = mockPropertyAccess();
        ClassPropertyAdapter classPropertyAdapter = mockClassPropertyAdapter();
        PropertyAdapter propertyAdapter = mockPropertyAdapter();
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
                BindingConstants.PROP,
                id,
                binding);

        replay();

        ComponentDefaultProvider source = new ComponentDefaultProviderImpl(access, bindingSource, null,
                                                                           null, null);

        assertSame(source.defaultBinding(parameterName, resources), binding);

        verify();
    }

    @Test
    public void default_translator_property_type_is_null()
    {
        ComponentResources resources = mockComponentResources();
        FieldTranslatorSource source = newMock(FieldTranslatorSource.class);

        train_createDefaultTranslator(source, resources, "object", null);

        replay();

        ComponentDefaultProvider provider = new ComponentDefaultProviderImpl(null, null, null, source, null);

        assertNull(provider.defaultTranslator("object", resources));

        verify();
    }

    @Test
    public void default_translator()
    {
        ComponentResources resources = mockComponentResources();
        FieldTranslator translator = mockFieldTranslator();
        FieldTranslatorSource source = newMock(FieldTranslatorSource.class);

        train_createDefaultTranslator(source, resources, "object", translator);

        replay();

        ComponentDefaultProvider provider = new ComponentDefaultProviderImpl(null, null, null, source, null);

        assertSame(provider.defaultTranslator("object", resources), translator);

        verify();
    }
}
