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

import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.parser.ComponentTemplate;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.Resource;
import org.apache.tapestry5.ioc.internal.services.ClasspathURLConverterImpl;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.ioc.services.ClasspathURLConverter;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.InvalidationListener;
import org.testng.annotations.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.UUID;

public class ComponentTemplateSourceImplTest extends InternalBaseTestCase
{
    private static final String PACKAGE = "org.apache.tapestry5.internal.pageload";

    static public final String PATH = "org/apache/tapestry5/internal/pageload";

    private final ClassLoader loader = Thread.currentThread().getContextClassLoader();
    
    private final ClasspathURLConverter converter = new ClasspathURLConverterImpl();

    /**
     * Creates a new class loader, whose parent is the thread's context class loader, but adds a single classpath root
     * from the filesystem.
     *
     * @see #createClasspathRoot()
     */
    protected final URLClassLoader newLoaderWithClasspathRoot(File rootDir)
            throws MalformedURLException
    {
        String urlPath = rootDir.toURL().toString();
        // URLs for folders must end with a slash to make URLClassLoader happy.
        URL url = new URL(urlPath + "/");

        return new URLClassLoader(new URL[]
                {url}, loader);
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

    @Test
    public void caching()
    {
        Resource baseResource = newResource("Fred.class");

        TemplateParser parser = mockTemplateParser();
        ComponentTemplate template = mockComponentTemplate();
        ComponentModel model = mockComponentModel();

        train_getComponentClassName(model, PACKAGE + ".Fred");

        train_getBaseResource(model, baseResource);

        train_parseTemplate(parser, baseResource
                .withExtension(InternalConstants.TEMPLATE_EXTENSION), template);

        replay();

        ComponentTemplateSource source = new ComponentTemplateSourceImpl(parser, null, converter);

        assertSame(source.getTemplate(model, Locale.ENGLISH), template);

        // A second pass will test the caching (the
        // parser is not invoked).

        assertSame(source.getTemplate(model, Locale.ENGLISH), template);

        verify();
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
        Resource localized = baseResource.withExtension(InternalConstants.TEMPLATE_EXTENSION);

        TemplateParser parser = mockTemplateParser();
        ComponentTemplate template = mockComponentTemplate();
        InvalidationListener listener = mockInvalidationListener();

        train_getComponentClassName(model, "baz.Biff");

        train_getBaseResource(model, baseResource);

        train_parseTemplate(parser, localized, template);

        replay();

        ComponentTemplateSourceImpl source = new ComponentTemplateSourceImpl(parser, null, converter);
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

        train_getBaseResource(model, baseResource);

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
        Resource baseResource = newResource("Fred.class");

        TemplateParser parser = mockTemplateParser();
        ComponentTemplate template = mockComponentTemplate();
        ComponentModel model = mockComponentModel();

        train_getComponentClassName(model, PACKAGE + ".Fred");

        train_getBaseResource(model, baseResource);

        train_parseTemplate(parser, baseResource
                .withExtension(InternalConstants.TEMPLATE_EXTENSION), template);

        replay();

        ComponentTemplateSourceImpl source = new ComponentTemplateSourceImpl(parser, null, converter);

        assertSame(source.getTemplate(model, Locale.ENGLISH), template);

        // A second pass finds the same resource, but using a different
        // path/locale combination.

        assertSame(source.getTemplate(model, Locale.FRENCH), template);

        // A third pass should further demonstrate the caching.

        assertSame(source.getTemplate(model, Locale.FRENCH), template);

        verify();
    }

    @Test
    public void page_template_found_in_context()
    {
        Resource baseResource = newResource("NotInClasspath.class");

        // Simpler to do it this way ...

        Resource contextTemplateResource = newResource("Fred.tml");

        TemplateParser parser = mockTemplateParser();
        ComponentTemplate template = mockComponentTemplate();
        ComponentModel model = mockComponentModel();
        PageTemplateLocator locator = mockPageTemplateLocator();
        Locale locale = Locale.FRENCH;

        train_getComponentClassName(model, PACKAGE + ".NotInClasspath");

        train_getBaseResource(model, baseResource);

        train_findPageTemplateResource(locator, model, locale, contextTemplateResource);

        train_parseTemplate(parser, contextTemplateResource, template);

        replay();

        ComponentTemplateSourceImpl source = new ComponentTemplateSourceImpl(parser, locator, converter);

        assertSame(source.getTemplate(model, locale), template);

        verify();
    }

    @Test
    public void no_template_found()
    {
        TemplateParser parser = mockTemplateParser();
        ComponentModel model = mockComponentModel();
        PageTemplateLocator locator = mockPageTemplateLocator();

        Resource baseResource = newResource("Barney.class");

        train_getComponentClassName(model, PACKAGE + ".Barney");

        train_getBaseResource(model, baseResource);

        train_findPageTemplateResource(locator, model, Locale.ENGLISH, null);

        train_getParentModel(model, null);

        replay();

        ComponentTemplateSourceImpl source = new ComponentTemplateSourceImpl(parser, locator, converter);

        ComponentTemplate template = source.getTemplate(model, Locale.ENGLISH);

        assertTrue(template.isMissing());

        verify();
    }

    @Test
    public void child_component_inherits_parent_template()
    {
        Resource baseFred = newResource("Fred.class");
        Resource baseBarney = baseFred.forFile("Barney.class");
        PageTemplateLocator locator = mockPageTemplateLocator();

        TemplateParser parser = mockTemplateParser();
        ComponentTemplate template = mockComponentTemplate();
        ComponentModel model = mockComponentModel();
        ComponentModel parentModel = mockComponentModel();

        train_getComponentClassName(model, PACKAGE + ".Barney");

        train_getBaseResource(model, baseBarney);

        train_findPageTemplateResource(locator, model, Locale.ENGLISH, null);

        train_getParentModel(model, parentModel);

        train_getBaseResource(parentModel, baseFred);

        train_parseTemplate(
                parser,
                baseFred.withExtension(InternalConstants.TEMPLATE_EXTENSION),
                template);

        replay();

        ComponentTemplateSource source = new ComponentTemplateSourceImpl(parser, locator, converter);

        assertSame(source.getTemplate(model, Locale.ENGLISH), template);

        verify();
    }

    private Resource newResource(String name)
    {
        return new ClasspathResource(loader, PATH + "/" + name);
    }
}
