// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.services.ClassNameLocator;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.LibraryMapping;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.isA;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ComponentClassResolverImplTest extends InternalBaseTestCase
{
    private static final String APP_ROOT_PACKAGE = "org.example.app";

    private static final String CORE_PREFIX = "core";

    private static final String CORE_ROOT_PACKAGE = "org.apache.tapestry5.corelib";

    private static final String LIB_PREFIX = "lib";

    private static final String LIB_ROOT_PACKAGE = "org.example.lib";

    private ComponentClassResolverImpl create(Logger logger, ComponentInstantiatorSource source,
                                              ClassNameLocator locator, LibraryMapping... mappings)
    {
        List<LibraryMapping> list = Arrays.asList(mappings);

        return new ComponentClassResolverImpl(logger, source, locator, APP_ROOT_PACKAGE, list);
    }

    private Logger compliantLogger()
    {
        Logger logger = mockLogger();

        logger.info(EasyMock.isA(String.class));

        EasyMock.expectLastCall().atLeastOnce();

        return logger;
    }

    @Test
    public void simple_page_name()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_app_packages(source);

        String className = APP_ROOT_PACKAGE + ".pages.SimplePage";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator);

        assertEquals(resolver.resolvePageNameToClassName("SimplePage"), className);

        verify();
    }

    /**
     * TAPESTRY-1923
     */
    @Test
    public void get_page_names()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_app_packages(source);


        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages",
                                        APP_ROOT_PACKAGE + ".pages.SimplePage",
                                        APP_ROOT_PACKAGE + ".pages.nested.Other",
                                        APP_ROOT_PACKAGE + ".pages.nested.NestedPage",
                                        APP_ROOT_PACKAGE + ".pages.nested.NestedIndex");

        replay();

        ComponentClassResolver resolver = create(logger, source, locator);

        List<String> pageNames = resolver.getPageNames();

        assertListsEquals(pageNames, "SimplePage", "nested/Index", "nested/Other", "nested/Page");

        verify();
    }

    /**
     * TAPESTRY-1541
     */
    @Test
    public void page_name_matches_containing_folder_name()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_app_packages(source);

        String className = APP_ROOT_PACKAGE + ".pages.admin.product.ProductAdmin";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator);

        assertEquals(resolver.resolvePageNameToClassName("admin/product/ProductAdmin"), className);

        verify();
    }


    @Test
    public void canonicalize_existing_page_name()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_app_packages(source);

        String className = APP_ROOT_PACKAGE + ".pages.SimplePage";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator);

        assertEquals(resolver.canonicalizePageName("simplepage"), "SimplePage");

        verify();
    }

    @Test
    public void page_name_in_subfolder()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_app_packages(source);

        String className = APP_ROOT_PACKAGE + ".pages.subfolder.NestedPage";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator);

        assertEquals(resolver.resolvePageNameToClassName("subfolder/NestedPage"), className);

        verify();
    }

    @Test
    public void lots_of_prefixes_and_suffixes_stripped()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_app_packages(source);

        String className = APP_ROOT_PACKAGE + ".pages.admin.edit.AdminUserEdit";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator);

        assertEquals(resolver.resolvePageNameToClassName("admin/edit/User"), className);
        assertEquals(resolver.resolvePageNameToClassName("admin/edit/AdminUserEdit"), className);

        verify();
    }

    @Test
    public void page_in_subfolder()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_app_packages(source);

        String className = APP_ROOT_PACKAGE + ".pages.subfolder.NestedPage";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator);

        assertEquals(resolver.resolvePageNameToClassName("subfolder/NestedPage"), className);

        verify();
    }

    @Test
    public void subfolder_name_as_classname_prefix_is_stripped()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_app_packages(source);

        String className = APP_ROOT_PACKAGE + ".pages.foo.FooBar";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator);

        assertEquals(resolver.resolvePageNameToClassName("foo/Bar"), className);

        verify();
    }

    @Test
    public void core_prefix_stripped_from_exception_message()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_packages(source, CORE_ROOT_PACKAGE);
        train_for_app_packages(source);

        train_locateComponentClassNames(locator, CORE_ROOT_PACKAGE + ".pages", CORE_ROOT_PACKAGE + ".pages.Fred",
                                        CORE_ROOT_PACKAGE + ".pages.Barney");
        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", APP_ROOT_PACKAGE + ".pages.Wilma",
                                        APP_ROOT_PACKAGE + ".pages.Betty");

        replay();

        ComponentClassResolver resolver = create(logger, source, locator,
                                                 new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        try
        {
            resolver.resolvePageNameToClassName("Unknown");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                         "Unable to resolve \'Unknown\' to a page class name.  Available page names: Barney, Betty, Fred, Wilma.");
        }

        verify();
    }

    @Test
    public void is_page_name()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_app_packages(source);

        String className = APP_ROOT_PACKAGE + ".pages.SimplePage";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator);

        assertTrue(resolver.isPageName("SimplePage"));
        assertTrue(resolver.isPageName("simplepage"));
        assertFalse(resolver.isPageName("UnknownPage"));

        verify();
    }

    @Test
    public void index_page_name_at_root()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_app_packages(source);

        String className = APP_ROOT_PACKAGE + ".pages.Index";

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator);

        assertTrue(resolver.isPageName("Index"));
        assertTrue(resolver.isPageName(""));

        verify();
    }

    @Test
    public void is_page_name_for_core_page()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_app_packages(source);
        train_for_packages(source, CORE_ROOT_PACKAGE);

        String className = CORE_ROOT_PACKAGE + ".pages.MyCorePage";

        train_locateComponentClassNames(locator, CORE_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator,
                                                 new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        // Can look like an application page, but still resolves to the core library class name.

        assertTrue(resolver.isPageName("MyCorePage"));

        // Or we can give it its true name

        assertTrue(resolver.isPageName("core/mycorepage"));

        assertFalse(resolver.isPageName("UnknownPage"));

        verify();
    }

    protected final ClassNameLocator newClassNameLocator()
    {
        ClassNameLocator locator = newMock(ClassNameLocator.class);

        stub_locateComponentClassNames(locator);

        return locator;
    }

    private void stub_locateComponentClassNames(ClassNameLocator locator)
    {
        Collection<String> noMatches = Collections.emptyList();

        expect(locator.locateClassNames(isA(String.class))).andStubReturn(noMatches);
    }

    protected final void train_locateComponentClassNames(ClassNameLocator locator, String packageName,
                                                         String... classNames)
    {
        expect(locator.locateClassNames(packageName)).andReturn(Arrays.asList(classNames));
    }

    @Test
    public void class_name_to_simple_page_name()
    {
        String className = APP_ROOT_PACKAGE + ".pages.SimplePage";

        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_app_packages(source);

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator);

        assertEquals(resolver.resolvePageClassNameToPageName(className), "SimplePage");

        verify();
    }

    /**
     * All of the caches are handled identically, so we just test the pages for caching.
     */
    @Test
    public void resolved_page_names_are_cached()
    {
        String pageClassName = APP_ROOT_PACKAGE + ".pages.SimplePage";

        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_app_packages(source);

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", pageClassName);

        replay();

        ComponentClassResolverImpl resolver = create(logger, source, locator);

        assertEquals(resolver.resolvePageNameToClassName("SimplePage"), pageClassName);

        verify();

        // No more training, because it's already cached.

        replay();

        assertEquals(resolver.resolvePageNameToClassName("SimplePage"), pageClassName);

        verify();

        // After clearing the cache, redoes the work.

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", pageClassName);
        stub_locateComponentClassNames(locator);

        replay();

        resolver.objectWasInvalidated();

        assertEquals(resolver.resolvePageNameToClassName("SimplePage"), pageClassName);

        verify();
    }

    @Test
    public void page_found_in_core_lib()
    {
        String className = CORE_ROOT_PACKAGE + ".pages.MyCorePage";

        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_packages(source, CORE_ROOT_PACKAGE);
        train_for_app_packages(source);

        train_locateComponentClassNames(locator, CORE_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator,
                                                 new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        assertEquals(resolver.resolvePageNameToClassName("MyCorePage"), className);

        verify();
    }

    @Test
    public void page_class_name_resolved_to_core_page()
    {
        String className = CORE_ROOT_PACKAGE + ".pages.MyCorePage";

        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_packages(source, CORE_ROOT_PACKAGE);
        train_for_app_packages(source);

        train_locateComponentClassNames(locator, CORE_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator,
                                                 new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        assertEquals(resolver.resolvePageClassNameToPageName(className), "core/MyCorePage");

        verify();
    }

    @Test
    public void page_found_in_library()
    {
        String className = LIB_ROOT_PACKAGE + ".pages.MyLibPage";

        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_packages(source, LIB_ROOT_PACKAGE);
        train_for_packages(source, CORE_ROOT_PACKAGE);
        train_for_app_packages(source);

        train_locateComponentClassNames(locator, LIB_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator,
                                                 new LibraryMapping(LIB_PREFIX, LIB_ROOT_PACKAGE),
                                                 new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        assertEquals(resolver.resolvePageNameToClassName("lib/MyLibPage"), className);

        verify();
    }

    @Test
    public void slashes_trimmed_from_library_prefix()
    {
        String className = LIB_ROOT_PACKAGE + ".pages.MyLibPage";

        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_packages(source, LIB_ROOT_PACKAGE);
        train_for_packages(source, CORE_ROOT_PACKAGE);
        train_for_app_packages(source);

        train_locateComponentClassNames(locator, LIB_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator,
                                                 new LibraryMapping("/" + LIB_PREFIX + "/", LIB_ROOT_PACKAGE),
                                                 new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        assertEquals(resolver.resolvePageNameToClassName("lib/MyLibPage"), className);

        verify();
    }

    @Test
    public void lookup_by_logical_name_is_case_insensitive()
    {
        String className = LIB_ROOT_PACKAGE + ".pages.MyLibPage";

        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_packages(source, LIB_ROOT_PACKAGE);
        train_for_packages(source, CORE_ROOT_PACKAGE);
        train_for_app_packages(source);

        train_locateComponentClassNames(locator, LIB_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator,
                                                 new LibraryMapping(LIB_PREFIX, LIB_ROOT_PACKAGE),
                                                 new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        assertEquals(resolver.resolvePageNameToClassName("lib/MyLibPage"), className);

        verify();
    }

    @Test
    public void name_stripping_includes_library_folder()
    {
        String className = LIB_ROOT_PACKAGE + ".pages.LibPage";

        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_packages(source, LIB_ROOT_PACKAGE);
        train_for_packages(source, CORE_ROOT_PACKAGE);
        train_for_app_packages(source);

        train_locateComponentClassNames(locator, LIB_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator,
                                                 new LibraryMapping(LIB_PREFIX, LIB_ROOT_PACKAGE),
                                                 new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        assertEquals(resolver.resolvePageNameToClassName("lib/Page"), className);

        verify();
    }

    @Test
    public void name_stripping_for_complex_library_folder_name()
    {
        String libPrefix = "lib/deep";

        String className = LIB_ROOT_PACKAGE + ".pages.LibDeepPage";

        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_packages(source, LIB_ROOT_PACKAGE);
        train_for_packages(source, CORE_ROOT_PACKAGE);
        train_for_app_packages(source);

        train_locateComponentClassNames(locator, LIB_ROOT_PACKAGE + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator,
                                                 new LibraryMapping(libPrefix, LIB_ROOT_PACKAGE),
                                                 new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        assertEquals(resolver.resolvePageNameToClassName("lib/deep/Page"), className);
        assertEquals(resolver.resolvePageNameToClassName("lib/deep/LibDeepPage"), className);

        verify();
    }


    @Test
    public void class_name_does_not_resolve_to_page_name()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = mockLogger();

        train_for_packages(source, CORE_ROOT_PACKAGE);
        train_for_app_packages(source);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator,
                                                 new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        String className = LIB_ROOT_PACKAGE + ".pages.LibPage";

        try
        {
            resolver.resolvePageClassNameToPageName(className);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), "Unable to resolve class name " + className + " to a logical page name.");
        }

        verify();
    }

    @Test
    public void page_name_to_canonicalize_does_not_exist()
    {

        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_packages(source, CORE_ROOT_PACKAGE);
        train_for_app_packages(source);

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".pages", APP_ROOT_PACKAGE + ".pages.Start");

        replay();

        ComponentClassResolver resolver = create(logger, source, locator,
                                                 new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        try
        {
            resolver.canonicalizePageName("MissingPage");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Unable to resolve \'MissingPage\' to a known page name. Available page names: Start.");
        }

        verify();
    }

    @Test
    public void class_name_not_in_a_pages_package()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = mockLogger();

        train_for_packages(source, CORE_ROOT_PACKAGE);
        train_for_app_packages(source);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator,
                                                 new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        String className = CORE_ROOT_PACKAGE + ".foo.CorePage";

        try
        {
            resolver.resolvePageClassNameToPageName(className);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(), "Unable to resolve class name " + className + " to a logical page name.");
        }

        verify();
    }

    @Test
    public void multiple_mappings_for_same_prefix()
    {
        String secondaryLibPackage = "org.examples.addon.lib";
        String className = secondaryLibPackage + ".pages.MyLibPage";

        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_packages(source, LIB_ROOT_PACKAGE);
        train_for_packages(source, secondaryLibPackage);
        train_for_packages(source, CORE_ROOT_PACKAGE);
        train_for_app_packages(source);

        train_locateComponentClassNames(locator, secondaryLibPackage + ".pages", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator,
                                                 new LibraryMapping(LIB_PREFIX, LIB_ROOT_PACKAGE),
                                                 new LibraryMapping(LIB_PREFIX, secondaryLibPackage),
                                                 new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        assertEquals(resolver.resolvePageNameToClassName("lib/MyLibPage"), className);

        verify();
    }

    @Test
    public void complex_prefix_search_fails()
    {
        String deepPackage = "org.deep";

        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = mockLogger();

        train_for_packages(source, deepPackage);
        train_for_packages(source, LIB_ROOT_PACKAGE);
        train_for_packages(source, CORE_ROOT_PACKAGE);
        train_for_app_packages(source);

        // Is this test even needed any more with the new algorithm?

        replay();

        ComponentClassResolver resolver = create(logger, source, locator, new LibraryMapping("lib/deep", deepPackage),
                                                 new LibraryMapping(LIB_PREFIX, LIB_ROOT_PACKAGE),
                                                 new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        try
        {
            resolver.resolvePageNameToClassName("lib/deep/DeepPage");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertMessageContains(ex, "Unable to resolve 'lib/deep/DeepPage' to a page class name.");
        }

        verify();
    }

    private void train_for_packages(ComponentInstantiatorSource source, String packageName)
    {
        source.addPackage(packageName + ".pages");
        source.addPackage(packageName + ".components");
        source.addPackage(packageName + ".mixins");
        source.addPackage(packageName + ".base");
    }

    /**
     * The logic for searching is pretty much identical for both components and pages, so even a cursory test of
     * component types should nail it.
     */
    @Test
    public void simple_component_type()
    {
        String className = APP_ROOT_PACKAGE + ".components.SimpleComponent";

        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_app_packages(source);

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".components", className);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator);

        assertEquals(resolver.resolveComponentTypeToClassName("SimpleComponent"), className);

        verify();
    }

    /**
     * Likewise for mixins; it's all just setup for a particular method.
     */

    @Test
    public void simple_mixin_type()
    {
        String expectedClassName = APP_ROOT_PACKAGE + ".mixins.SimpleMixin";

        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = compliantLogger();

        train_for_app_packages(source);

        train_locateComponentClassNames(locator, APP_ROOT_PACKAGE + ".mixins", expectedClassName);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator);

        assertEquals(resolver.resolveMixinTypeToClassName("SimpleMixin"), expectedClassName);

        verify();
    }

    @Test
    public void mixin_type_not_found()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = mockLogger();

        train_for_packages(source, CORE_ROOT_PACKAGE);
        train_for_app_packages(source);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator,
                                                 new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        try
        {
            resolver.resolveMixinTypeToClassName("SimpleMixin");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertMessageContains(ex, "Unable to resolve 'SimpleMixin' to a mixin class name.");
        }

        verify();
    }

    @Test
    public void component_type_not_found()
    {
        ComponentInstantiatorSource source = mockComponentInstantiatorSource();
        ClassNameLocator locator = newClassNameLocator();
        Logger logger = mockLogger();

        train_for_packages(source, CORE_ROOT_PACKAGE);
        train_for_app_packages(source);

        replay();

        ComponentClassResolver resolver = create(logger, source, locator,
                                                 new LibraryMapping(CORE_PREFIX, CORE_ROOT_PACKAGE));

        try
        {
            resolver.resolveComponentTypeToClassName("SimpleComponent");
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertTrue(ex.getMessage().contains("Unable to resolve 'SimpleComponent' to a component class name."));
        }

        verify();
    }

    private void train_for_app_packages(ComponentInstantiatorSource source)
    {
        train_for_packages(source, APP_ROOT_PACKAGE);
    }
}
