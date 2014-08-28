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

package org.apache.tapestry5.internal.services.templates

import org.apache.tapestry5.internal.test.InternalBaseTestCase
import org.testng.annotations.Test

class PageTemplateLocatorTest extends InternalBaseTestCase {

    void execute(root, resolver, closure) {

        replay()

        closure.call(new PageTemplateLocator(root, resolver, ""))

        verify()
    }

    @Test
    void not_a_page_class() {
        def model = mockComponentModel()
        def root = mockResource()
        def resolver = mockComponentClassResolver()

        expect(model.page).andReturn(false)

        execute(root, resolver) {

            assert it.locateTemplate(model, Locale.FRENCH) == null
        }
    }

    @Test
    void template_found() {
        def model = mockComponentModel()
        def root = mockResource()
        def withExtension = mockResource()
        def forLocale = mockResource()
        def resolver = mockComponentClassResolver()
        def locale = Locale.FRENCH
        def className = "myapp.pages.Foo"

        expect(model.page).andReturn(true)

        train_getComponentClassName model, className

        train_resolvePageClassNameToPageName(resolver, className, "Foo")

        train_forFile(root, "Foo.tml", withExtension)
        train_forLocale(withExtension, locale, forLocale)

        execute(root, resolver) {

            assertSame it.locateTemplate(model, locale), forLocale
        }
    }

    /**
     * Because of how Tapestry maps class names to logical page names, part of the name may be have been stripped off
     * and we want to make sure we get it back.
     */
    @Test
    void uses_simple_class_name_in_folders() {

        def model = mockComponentModel()
        def root = mockResource()
        def withExtension = mockResource()
        def forLocale = mockResource()
        def resolver = mockComponentClassResolver()

        def locale = Locale.FRENCH
        def className = "myapp.pages.foo.CreateFoo"

        expect(model.page).andReturn(true)

        train_getComponentClassName(model, className)
        train_resolvePageClassNameToPageName(resolver, className, "foo/Create")

        train_forFile(root, "foo/CreateFoo.tml", withExtension)
        train_forLocale(withExtension, locale, forLocale)

        execute(root, resolver) {

            assertSame it.locateTemplate(model, locale), forLocale
        }
    }

    @Test
    void uses_class_name_when_different_than_logical_name() {

        def model = mockComponentModel()
        def root = mockResource()
        def withExtension = mockResource()
        def forLocale = mockResource()
        def resolver = mockComponentClassResolver()

        def locale = Locale.FRENCH
        def className = "myapp.pages.foo.CreateFooPage"

        expect(model.page).andReturn(true)

        train_getComponentClassName(model, className)
        train_resolvePageClassNameToPageName(resolver, className, "foo/CreatePage")

        train_forFile(root, "foo/CreateFooPage.tml", withExtension)
        train_forLocale(withExtension, locale, forLocale)

        execute(root, resolver) {

            assertSame it.locateTemplate(model, locale), forLocale
        }
    }

    @Test
    void template_not_found() {
        def model = mockComponentModel()
        def root = mockResource()
        def withExtension = mockResource()
        def resolver = mockComponentClassResolver()
        def locale = Locale.GERMAN;
        def className = "myapp.pages.bar.Baz"

        expect(model.page).andReturn(true)

        train_getComponentClassName(model, className)
        train_resolvePageClassNameToPageName(resolver, className, "bar/Baz")

        train_forFile(root, "bar/Baz.tml", withExtension)
        train_forLocale(withExtension, locale, null)

        execute(root, resolver) {

            assertNull it.locateTemplate(model, locale)
        }
    }
}
