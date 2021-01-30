// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.UUID;

import org.apache.tapestry5.TapestryConstants;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.services.InvalidationListener;
import org.apache.tapestry5.internal.pageload.DefaultComponentRequestSelectorAnalyzer;
import org.apache.tapestry5.internal.parser.ComponentTemplate;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.internal.services.ClasspathURLConverterImpl;
import org.apache.tapestry5.ioc.internal.services.ThreadLocaleImpl;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.ioc.services.ThreadLocale;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.pageload.ComponentRequestSelectorAnalyzer;
import org.apache.tapestry5.services.pageload.ComponentResourceLocator;
import org.apache.tapestry5.services.pageload.ComponentResourceSelector;
import org.apache.tapestry5.services.templates.ComponentTemplateLocator;
import org.testng.annotations.Test;

public class ComponentTemplateSourceImplTest extends InternalBaseTestCase
{
    private static final String PACKAGE = "org.apache.tapestry5.internal.pageload";

    private final ClassLoader loader = Thread.currentThread().getContextClassLoader();

    private final ClasspathURLConverter converter = new ClasspathURLConverterImpl();

    private final ComponentResourceSelector english = new ComponentResourceSelector(Locale.ENGLISH);

    private final ComponentResourceSelector french = new ComponentResourceSelector(Locale.FRENCH);

    private final ThreadLocale threadLocale = new ThreadLocaleImpl();

    private final ComponentRequestSelectorAnalyzer componentRequestSelectorAnalyzer = 
        new DefaultComponentRequestSelectorAnalyzer(threadLocale);

    /**
     * Creates a new class loader, whose parent is the thread's context class loader, but adds a single classpath root
     * from the filesystem.
     * 
     * @see #createClasspathRoot()
     */
    protected final URLClassLoader newLoaderWithClasspathRoot(File rootDir) throws MalformedURLException
    {
        String urlPath = rootDir.toURL().toString();
        // URLs for folders must end with a slash to make URLClassLoader happy.
        URL url = new URL(urlPath + "/");

        return new URLClassLoader(new URL[]
        { url }, loader);
    }

    /**
     * Creates a new temporary directory which can act as a classpath root.
     * 
     * @see #newLoaderWithClasspathRoot(File)
     */
    protected final File createClasspathRoot()
    {
        String temp = System.getProperty("java.io.tmpdir");
        String rootDirPath = temp + "/" + UUID.randomUUID().toString();

        return new File(rootDirPath);
    }

    private ComponentResourceLocator mockLocator(ComponentModel model, ComponentResourceSelector selector,
            Resource templateResource)
    {
        ComponentResourceLocator locator = newMock(ComponentResourceLocator.class);

        expect(locator.locateTemplate(model, selector)).andReturn(templateResource).once();

        return locator;
    }

    @Test
    public void caching()
    {
        TemplateParser parser = mockTemplateParser();
        ComponentTemplate template = mockComponentTemplate();
        ComponentModel model = mockComponentModel();
        Resource resource = mockResource();
        ComponentResourceLocator locator = mockLocator(model, english, resource);

        train_getComponentClassName(model, PACKAGE + ".Fred");

        expect(resource.exists()).andReturn(true);
        expect(resource.toURL()).andReturn(null);

        train_parseTemplate(parser, resource, template);

        replay();

        ComponentTemplateSource source = new ComponentTemplateSourceImpl(true, parser, locator, converter, componentRequestSelectorAnalyzer, threadLocale);

        assertSame(source.getTemplate(model, english), template);

        // A second pass will test the caching (the
        // parser is not invoked).

        assertSame(source.getTemplate(model, english), template);

        verify();
    }

    protected final ComponentTemplateLocator mockComponentTemplateLocator()
    {
        return newMock(ComponentTemplateLocator.class);
    }

    /**
     * Tests resource invalidation.
     */
    @Test
    public void invalidation() throws Exception
    {
        File rootDir = createClasspathRoot();
        URLClassLoader loader = newLoaderWithClasspathRoot(rootDir);
        ComponentModel model = mockComponentModel();

        File packageDir = new File(rootDir, "baz");
        packageDir.mkdirs();

        File f = new File(packageDir, "Biff.tml");

        f.createNewFile();

        Resource baseResource = new ClasspathResource(loader, "baz/Biff.class");
        Resource localized = baseResource.withExtension(TapestryConstants.TEMPLATE_EXTENSION);

        TemplateParser parser = mockTemplateParser();
        ComponentTemplate template = mockComponentTemplate();
        InvalidationListener listener = mockInvalidationListener();

        train_getComponentClassName(model, "baz.Biff");

        ComponentResourceLocator locator = mockLocator(model, english, localized);

        train_parseTemplate(parser, localized, template);

        replay();

        ComponentTemplateSourceImpl source = new ComponentTemplateSourceImpl(false, parser, locator, converter, componentRequestSelectorAnalyzer, threadLocale);
        source.addInvalidationListener(listener);

        assertSame(source.getTemplate(model, Locale.ENGLISH), template);

        // Check for updates (which won't be found).
        source.checkForUpdates();

        // A second pass will test the caching (the
        // parser is not invoked).

        assertSame(source.getTemplate(model, Locale.ENGLISH), template);

        verify();

        // Now, change the file and processInbound an UpdateEvent.

        touch(f);

        listener.objectWasInvalidated();

        replay();

        // Check for updates (which will be found).
        source.checkForUpdates();

        verify();

        // Check that the cache really is cleared.

        train_getComponentClassName(model, "baz.Biff");

        expect(locator.locateTemplate(model, english)).andReturn(localized);

        train_parseTemplate(parser, localized, template);

        replay();

        assertSame(source.getTemplate(model, Locale.ENGLISH), template);

        verify();
    }

    /**
     * Checks that localization to the same resource works (w/ caching).
     */
    @Test
    public void localization_to_same()
    {
        Resource resource = mockResource();
        TemplateParser parser = mockTemplateParser();
        ComponentTemplate template = mockComponentTemplate();
        ComponentModel model = mockComponentModel();
        ComponentResourceLocator locator = newMock(ComponentResourceLocator.class);

        train_getComponentClassName(model, PACKAGE + ".Fred");

        expect(locator.locateTemplate(model, english)).andReturn(resource).once();

        expect(resource.exists()).andReturn(true).anyTimes();
        expect(resource.toURL()).andReturn(null).anyTimes();

        expect(locator.locateTemplate(model, french)).andReturn(resource).once();

        train_parseTemplate(parser, resource, template);

        replay();

        ComponentTemplateSourceImpl source = new ComponentTemplateSourceImpl(true, parser, locator, converter, componentRequestSelectorAnalyzer, threadLocale);

        assertSame(source.getTemplate(model, Locale.ENGLISH), template);

        // A second pass finds the same resource, but using a different
        // path/locale combination.

        assertSame(source.getTemplate(model, Locale.FRENCH), template);

        // A third pass should further demonstrate the caching.

        assertSame(source.getTemplate(model, Locale.FRENCH), template);

        verify();
    }

    @Test
    public void no_template_found()
    {
        TemplateParser parser = mockTemplateParser();
        ComponentModel model = mockComponentModel();
        Resource baseResource = mockResource();
        Resource missingResource = mockResource();

        train_getComponentClassName(model, PACKAGE + ".Barney");

        ComponentResourceLocator locator = mockLocator(model, english, null);

        train_getParentModel(model, null);

        train_getBaseResource(model, baseResource);

        expect(baseResource.withExtension(TapestryConstants.TEMPLATE_EXTENSION)).andReturn(missingResource);

        expect(missingResource.exists()).andReturn(false);

        replay();

        ComponentTemplateSourceImpl source = new ComponentTemplateSourceImpl(true, parser, locator, converter, componentRequestSelectorAnalyzer, threadLocale);

        ComponentTemplate template = source.getTemplate(model, Locale.ENGLISH);

        assertTrue(template.isMissing());

        verify();
    }

    @Test
    public void child_component_inherits_parent_template()
    {
        TemplateParser parser = mockTemplateParser();
        ComponentTemplate template = mockComponentTemplate();
        ComponentModel model = mockComponentModel();
        ComponentModel parentModel = mockComponentModel();
        Resource resource = mockResource();
        ComponentResourceLocator locator = mockLocator(model, english, null);

        train_getComponentClassName(model, "foo.Bar");

        train_getParentModel(model, parentModel);

        expect(locator.locateTemplate(parentModel, english)).andReturn(resource).once();

        expect(resource.exists()).andReturn(true);
        expect(resource.toURL()).andReturn(null);

        train_parseTemplate(parser, resource, template);

        replay();

        ComponentTemplateSource source = new ComponentTemplateSourceImpl(true, parser, locator, converter, componentRequestSelectorAnalyzer, threadLocale);

        assertSame(source.getTemplate(model, english), template);

        verify();
    }
}
