// Copyright 2006, 2007, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.internal.util.URLChangeTracker;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.services.ClasspathURLConverterImpl;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.model.ComponentModel;
import org.testng.annotations.Test;

import java.util.Locale;

/**
 * Tests {@link ComponentMessagesSourceImpl} as well as {@link MessagesSourceImpl} (which contains code refactored out
 * of CMSI).
 */
@Test(sequential = true)
public class ComponentMessagesSourceImplTest extends InternalBaseTestCase
{
    // With control of the tracker, we can force changes as if underlying files were changed.

    private static final String SIMPLE_COMPONENT_CLASS_NAME = "org.apache.tapestry5.internal.services.SimpleComponent";

    private final ClasspathURLConverter converter = new ClasspathURLConverterImpl();

    private final URLChangeTracker tracker = new URLChangeTracker(converter);

    private final Resource simpleComponentResource = new ClasspathResource(
            "org/apache/tapestry5/internal/services/SimpleComponent.class");

    private final ComponentMessagesSourceImpl source = new ComponentMessagesSourceImpl(
            simpleComponentResource.forFile("AppCatalog.properties"), tracker);

    @Test
    public void simple_component()
    {
        ComponentModel model = mockComponentModel();

        train_getComponentClassName(model, SIMPLE_COMPONENT_CLASS_NAME);

        train_getBaseResource(model, simpleComponentResource);

        train_getParentModel(model, null);

        replay();

        forceCacheClear();

        Messages messages = source.getMessages(model, Locale.ENGLISH);

        assertEquals(messages.get("color"), "color");
        assertEquals(messages.get("framework"), "Tapestry");

        // Check normal caching

        assertSame(source.getMessages(model, Locale.ENGLISH), messages);

        // Now, force a cache clear and retry.

        forceCacheClear();

        Messages messages2 = source.getMessages(model, Locale.ENGLISH);

        // Check that a new Messages was created

        assertNotSame(messages2, messages);

        assertEquals(messages2.get("color"), "color");
        assertEquals(messages2.get("framework"), "Tapestry");

        verify();
    }

    private void forceCacheClear()
    {
        tracker.forceChange();
        source.checkForUpdates();
    }

    @Test
    public void per_language_messages_override()
    {
        ComponentModel model = mockComponentModel();

        train_getComponentClassName(model, SIMPLE_COMPONENT_CLASS_NAME);

        train_getBaseResource(model, simpleComponentResource);

        train_getParentModel(model, null);

        replay();

        forceCacheClear();

        Messages messages = source.getMessages(model, Locale.UK);

        assertEquals(messages.get("color"), "colour");
        assertEquals(messages.get("framework"), "Tapestry");

        verify();
    }

    @Test
    public void messages_keys_are_case_insensitive()
    {
        ComponentModel model = mockComponentModel();

        train_getComponentClassName(model, SIMPLE_COMPONENT_CLASS_NAME);

        train_getBaseResource(model, simpleComponentResource);

        train_getParentModel(model, null);

        replay();

        forceCacheClear();

        Messages messages = source.getMessages(model, Locale.UK);

        assertEquals(messages.get("COlor"), "colour");
        assertEquals(messages.get("Framework"), "Tapestry");

        verify();
    }

    @Test
    public void subclass_inherits_base_class_messages()
    {
        ComponentModel model = mockComponentModel();
        ComponentModel parent = mockComponentModel();

        train_getComponentClassName(
                model,
                "org.apache.tapestry5.internal.services.SubclassComponent");

        train_getBaseResource(model, new ClasspathResource(
                "org/apache/tapestry5/internal/services/SubclassComponent.class"));

        train_getParentModel(model, parent);

        train_getComponentClassName(parent, SIMPLE_COMPONENT_CLASS_NAME);

        train_getBaseResource(parent, simpleComponentResource);

        train_getParentModel(parent, null);

        replay();

        forceCacheClear();

        Messages messages = source.getMessages(model, Locale.ENGLISH);

        assertEquals(messages.get("color"), "color");
        assertEquals(messages.get("framework"), "Tapestry");
        assertEquals(messages.get("source"), "SubclassComponent");
        assertEquals(messages.get("metal"), "steel");
        assertEquals(messages.get("app-catalog-source"), "AppCatalog");
        assertEquals(messages.get("app-catalog-overridden"), "Overridden by Component");

        messages = source.getMessages(model, Locale.UK);

        assertEquals(messages.get("color"), "colour");
        assertEquals(messages.get("framework"), "Tapestry");
        assertEquals(messages.get("source"), "SubclassComponent");
        assertEquals(messages.get("metal"), "aluminium");

        verify();
    }

    @Test
    public void no_app_catalog()
    {
        ComponentModel model = mockComponentModel();
        ComponentModel parent = mockComponentModel();

        train_getComponentClassName(
                model,
                "org.apache.tapestry5.internal.services.SubclassComponent");

        train_getBaseResource(model, new ClasspathResource(
                "org/apache/tapestry5/internal/services/SubclassComponent.class"));

        train_getParentModel(model, parent);

        train_getComponentClassName(parent, SIMPLE_COMPONENT_CLASS_NAME);

        train_getBaseResource(parent, simpleComponentResource);

        train_getParentModel(parent, null);

        replay();

        forceCacheClear();

        ComponentMessagesSource source = new ComponentMessagesSourceImpl(
                simpleComponentResource.forFile("NoSuchAppCatalog.properties"), converter);

        Messages messages = source.getMessages(model, Locale.ENGLISH);

        assertEquals(messages.get("color"), "color");
        assertEquals(messages.get("app-catalog-source"), "[[missing key: app-catalog-source]]");
        assertEquals(messages.get("app-catalog-overridden"), "Overridden by Component");

        verify();
    }
}
