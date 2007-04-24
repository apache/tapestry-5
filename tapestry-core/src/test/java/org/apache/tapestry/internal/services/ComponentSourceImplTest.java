// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.internal.bindings.DefaultComponent;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ComponentSource;
import org.testng.annotations.Test;

public class ComponentSourceImplTest extends InternalBaseTestCase
{
    private static final String PAGE_NAME = "foo.pages.Bar";

    private static final String NESTED_ELEMENT_ID = "zip.zoom";

    @Test
    public void root_element_of_page()
    {
        RequestPageCache cache = newRequestPageCache();
        Page page = newPage();
        Component component = newComponent();

        train_getByClassName(cache, PAGE_NAME, page);

        train_getRootComponent(page, component);

        replay();

        ComponentSource source = new ComponentSourceImpl(cache);

        assertSame(source.getComponent(PAGE_NAME), component);

        verify();
    }

    @Test
    public void nested_element_within_page()
    {
        RequestPageCache cache = newRequestPageCache();
        Page page = newPage();
        ComponentPageElement element = newComponentPageElement();
        Component component = newComponent();

        train_getByClassName(cache, PAGE_NAME, page);

        train_getComponentElementByNestedId(page, NESTED_ELEMENT_ID, element);

        train_getComponent(element, component);

        replay();

        ComponentSource source = new ComponentSourceImpl(cache);

        assertSame(source.getComponent(PAGE_NAME + ":" + NESTED_ELEMENT_ID), component);

        verify();
    }

    @Test
    public void get_page_by_logical_name()
    {
        RequestPageCache cache = newRequestPageCache();
        Page page = newPage();
        Component component = newComponent();

        train_get(cache, PAGE_NAME, page);
        train_getRootComponent(page, component);

        replay();

        ComponentSource source = new ComponentSourceImpl(cache);

        assertSame(source.getPage(PAGE_NAME), component);

        verify();
    }

    @Test
    public void get_page_by_class()
    {
        RequestPageCache cache = newRequestPageCache();
        Page page = newPage();
        Component component = new DefaultComponent();

        train_getByClassName(cache, DefaultComponent.class.getName(), page);
        train_getRootComponent(page, component);

        replay();

        ComponentSource source = new ComponentSourceImpl(cache);

        assertSame(source.getPage(DefaultComponent.class), component);

        verify();
    }

}
