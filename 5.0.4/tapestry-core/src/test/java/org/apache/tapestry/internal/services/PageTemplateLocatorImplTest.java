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

package org.apache.tapestry.internal.services;

import java.util.Locale;

import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.services.ComponentClassResolver;
import org.testng.annotations.Test;

public class PageTemplateLocatorImplTest extends InternalBaseTestCase
{
    @Test
    public void not_a_page_class()
    {
        ComponentModel model = mockComponentModel();
        Resource root = mockResource();
        ComponentClassResolver resolver = mockComponentClassResolver();

        train_getComponentClassName(model, "foo.bar.Baz");

        replay();

        PageTemplateLocator locator = new PageTemplateLocatorImpl(root, resolver);

        assertNull(locator.findPageTemplateResource(model, Locale.FRENCH));

        verify();
    }

    @Test
    public void template_found()
    {
        ComponentModel model = mockComponentModel();
        Resource root = mockResource();
        Resource withExtension = mockResource();
        Resource forLocale = mockResource();
        Locale locale = Locale.FRENCH;
        String className = "myapp.pages.Foo";

        ComponentClassResolver resolver = mockComponentClassResolver();

        train_getComponentClassName(model, className);

        train_resolvePageClassNameToPageName(resolver, className, "Foo");

        train_forFile(root, "WEB-INF/Foo.html", withExtension);
        train_forLocale(withExtension, locale, forLocale);

        replay();

        PageTemplateLocator locator = new PageTemplateLocatorImpl(root, resolver);

        assertSame(locator.findPageTemplateResource(model, locale), forLocale);

        verify();
    }

    @Test
    public void template_not_found()
    {
        ComponentModel model = mockComponentModel();
        Resource root = mockResource();
        Resource withExtension = mockResource();
        Locale locale = Locale.GERMAN;
        String className = "myapp.pages.bar.Baz";

        ComponentClassResolver resolver = mockComponentClassResolver();

        train_getComponentClassName(model, className);

        train_resolvePageClassNameToPageName(resolver, className, "bar/Baz");

        train_forFile(root, "WEB-INF/bar/Baz.html", withExtension);
        train_forLocale(withExtension, locale, null);

        replay();

        PageTemplateLocator locator = new PageTemplateLocatorImpl(root, resolver);

        assertNull(locator.findPageTemplateResource(model, locale));

        verify();
    }

}
