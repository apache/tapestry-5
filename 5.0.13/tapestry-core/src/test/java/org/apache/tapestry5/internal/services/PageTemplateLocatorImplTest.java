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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.testng.annotations.Test;

import java.util.Locale;

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

        train_forFile(root, "Foo.tml", withExtension);
        train_forLocale(withExtension, locale, forLocale);

        replay();

        PageTemplateLocator locator = new PageTemplateLocatorImpl(root, resolver);

        assertSame(locator.findPageTemplateResource(model, locale), forLocale);

        verify();
    }

    /**
     * Because of how Tapestry maps class names to logical page names, part of the name may be have been stripped off
     * and we want to make sure we get it back.
     */
    @Test
    public void uses_simple_class_name_in_folders()
    {
        ComponentModel model = mockComponentModel();
        Resource root = mockResource();
        Resource withExtension = mockResource();
        Resource forLocale = mockResource();
        Locale locale = Locale.FRENCH;
        String className = "myapp.pages.foo.CreateFoo";

        ComponentClassResolver resolver = mockComponentClassResolver();

        train_getComponentClassName(model, className);

        // Notice: foo/Create not foo/CreateFoo; we're simulating how the redundancy gets stripped
        // out of the class name.
        train_resolvePageClassNameToPageName(resolver, className, "foo/Create");

        // Abnd here's where we're showing that PTLI stitches it back together.
        train_forFile(root, "foo/CreateFoo.tml", withExtension);
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

        train_forFile(root, "bar/Baz.tml", withExtension);
        train_forLocale(withExtension, locale, null);

        replay();

        PageTemplateLocator locator = new PageTemplateLocatorImpl(root, resolver);

        assertNull(locator.findPageTemplateResource(model, locale));

        verify();
    }

}
