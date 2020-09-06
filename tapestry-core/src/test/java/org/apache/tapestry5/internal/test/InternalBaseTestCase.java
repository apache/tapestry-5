// Copyright 2006-2012 The Apache Software Foundation
//
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

package org.apache.tapestry5.internal.test;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ComponentResourcesCommon;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.beanmodel.internal.services.*;
import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.services.ClassPropertyAdapter;
import org.apache.tapestry5.commons.services.InvalidationListener;
import org.apache.tapestry5.commons.services.PropertyAccess;
import org.apache.tapestry5.commons.services.PropertyAdapter;
import org.apache.tapestry5.http.ContentType;
import org.apache.tapestry5.http.services.BaseURLSource;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.InternalComponentResources;
import org.apache.tapestry5.internal.parser.ComponentTemplate;
import org.apache.tapestry5.internal.services.ComponentModelSource;
import org.apache.tapestry5.internal.services.DocumentLinker;
import org.apache.tapestry5.internal.services.ForceDevelopmentModeModule;
import org.apache.tapestry5.internal.services.Instantiator;
import org.apache.tapestry5.internal.services.LinkSource;
import org.apache.tapestry5.internal.services.PageContentTypeAnalyzer;
import org.apache.tapestry5.internal.services.PageRenderQueue;
import org.apache.tapestry5.internal.services.PageResponseRenderer;
import org.apache.tapestry5.internal.services.RequestPageCache;
import org.apache.tapestry5.internal.services.RequestSecurityManager;
import org.apache.tapestry5.internal.services.TemplateParser;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.ComponentPageElementResources;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.RegistryBuilder;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.modules.TapestryModule;
import org.apache.tapestry5.root.FieldComponent;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.ClientBehaviorSupport;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.LinkCreationListener2;
import org.apache.tapestry5.services.LocalizationSetter;
import org.apache.tapestry5.services.MetaDataLocator;
import org.apache.tapestry5.test.TapestryTestCase;
import org.easymock.EasyMock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.*;
import java.lang.annotation.Annotation;

import static org.easymock.EasyMock.isA;

/**
 * Contains additional factory and training methods related to internal interfaces.
 */
public class InternalBaseTestCase extends TapestryTestCase implements Registry
{
    private static Registry registry;

    @BeforeSuite
    public final void setup_registry()
    {
        RegistryBuilder builder = new RegistryBuilder();

        builder.add(TapestryModule.class, ForceDevelopmentModeModule.class);

        registry = builder.build();

        registry.performRegistryStartup();
    }

    @AfterSuite
    public final void shutdown_registry()
    {
        registry.shutdown();

        registry = null;
    }

    @AfterMethod
    public final void cleanupThread()
    {
        registry.cleanupThread();
    }

    public void performRegistryStartup()
    {
        registry.performRegistryStartup();
    }

    public final <T> T getObject(Class<T> objectType, AnnotationProvider annotationProvider)
    {
        return registry.getObject(objectType, annotationProvider);
    }

    public final <T> T getService(Class<T> serviceInterface)
    {
        return registry.getService(serviceInterface);
    }


    public final <T> T getService(Class<T> serviceInterface, Class<? extends Annotation>... markerTypes)
    {
        return registry.getService(serviceInterface);
    }

    public final <T> T getService(String serviceId, Class<T> serviceInterface)
    {
        return registry.getService(serviceId, serviceInterface);
    }

    public final <T> T autobuild(Class<T> clazz)
    {
        return registry.autobuild(clazz);
    }

    public final <T> T autobuild(String description, Class<T> clazz)
    {
        return registry.autobuild(description, clazz);
    }

    public final <T> T proxy(Class<T> interfaceClass, Class<? extends T> implementationClass)
    {
        return registry.proxy(interfaceClass, implementationClass);
    }

    public final void shutdown()
    {
        throw new UnsupportedOperationException("No registry shutdown until @AfterSuite.");
    }

    protected final InternalComponentResources mockInternalComponentResources()
    {
        return newMock(InternalComponentResources.class);
    }

    protected final ComponentTemplate mockComponentTemplate()
    {
        return newMock(ComponentTemplate.class);
    }

    protected final Page mockPage()
    {
        return newMock(Page.class);
    }

    protected RenderQueue mockRenderQueue()
    {
        return newMock(RenderQueue.class);
    }

    protected final void train_parseTemplate(TemplateParser parser, Resource resource, ComponentTemplate template)
    {
        expect(parser.parseTemplate(resource)).andReturn(template);
    }

    protected final TemplateParser mockTemplateParser()
    {
        return newMock(TemplateParser.class);
    }

    protected final ComponentPageElement mockComponentPageElement()
    {
        return newMock(ComponentPageElement.class);
    }

    protected final void train_getComponent(ComponentPageElement element, Component component)
    {
        expect(element.getComponent()).andReturn(component).atLeastOnce();
    }

    protected final void train_getNestedId(ComponentResourcesCommon resources, String nestedId)
    {
        expect(resources.getNestedId()).andReturn(nestedId).atLeastOnce();
    }

    protected final void train_getBaseSource(BaseURLSource baseURLSource, Request request)
    {
        expect(request.isSecure()).andReturn(false);
        expect(baseURLSource.getBaseURL(false)).andReturn("");
    }

    protected final void train_resolvePageClassNameToPageName(ComponentClassResolver resolver, String pageClassName,
                                                              String pageName)
    {
        expect(resolver.resolvePageClassNameToPageName(pageClassName)).andReturn(pageName);
    }

    protected final void train_getComponentResources(ComponentPageElement element, InternalComponentResources resources)
    {
        expect(element.getComponentResources()).andReturn(resources).atLeastOnce();
    }

    protected final RenderCommand mockRenderCommand()
    {
        return newMock(RenderCommand.class);
    }

    protected final void train_getComponentModel(ComponentResources resources, ComponentModel model)
    {
        expect(resources.getComponentModel()).andReturn(model).atLeastOnce();
    }

    protected final void train_getModel(Instantiator ins, ComponentModel model)
    {
        expect(ins.getModel()).andReturn(model).atLeastOnce();
    }

    protected final Instantiator mockInstantiator(Component component)
    {
        Instantiator ins = newMock(Instantiator.class);

        expect(ins.newInstance(EasyMock.isA(InternalComponentResources.class))).andReturn(component);

        return ins;
    }

    protected final RequestPageCache mockRequestPageCache()
    {
        return newMock(RequestPageCache.class);
    }

    protected final void train_getComponentElementByNestedId(Page page, String nestedId, ComponentPageElement element)
    {
        expect(page.getComponentElementByNestedId(nestedId)).andReturn(element).atLeastOnce();
    }

    protected final void train_getRootElement(Page page, ComponentPageElement element)
    {
        expect(page.getRootElement()).andReturn(element).atLeastOnce();
    }

    protected final void train_getRootComponent(Page page, Component component)
    {
        expect(page.getRootComponent()).andReturn(component).atLeastOnce();
    }

    protected final InvalidationListener mockInvalidationListener()
    {
        return newMock(InvalidationListener.class);
    }

    protected final void train_get(RequestPageCache cache, String pageName, Page page)
    {
        expect(cache.get(pageName)).andReturn(page).atLeastOnce();
    }

    protected final LinkCreationListener2 mockLinkCreationListener2()
    {
        return newMock(LinkCreationListener2.class);
    }

    protected final LinkSource mockLinkSource()
    {
        return newMock(LinkSource.class);
    }

    protected final void stub_isPageName(ComponentClassResolver resolver, boolean result)
    {
        expect(resolver.isPageName(isA(String.class))).andStubReturn(result);
    }

    protected final void train_isPageName(ComponentClassResolver resolver, String pageName, boolean result)
    {
        expect(resolver.isPageName(pageName)).andReturn(result);
    }

    protected final PageResponseRenderer mockPageResponseRenderer()
    {
        return newMock(PageResponseRenderer.class);
    }

    /**
     * Reads the content of a file into a string. Each line is trimmed of line separators and
     * leading/trailing
     * whitespace.
     *
     * @param file trim each line of whitespace
     */
    protected final String readFile(String file) throws Exception
    {
        InputStream is = getClass().getResourceAsStream(file);
        is = new BufferedInputStream(is);
        Reader reader = new BufferedReader(new InputStreamReader(is));
        LineNumberReader in = new LineNumberReader(reader);

        StringBuilder buffer = new StringBuilder();

        while (true)
        {
            String line = in.readLine();

            if (line == null)
                break;

            buffer.append(line);

            buffer.append('\n');
        }

        in.close();

        return buffer.toString().trim();
    }

    protected final DocumentLinker mockDocumentLinker()
    {
        return newMock(DocumentLinker.class);
    }

    protected final void train_canonicalizePageName(ComponentClassResolver resolver, String pageName,
                                                    String canonicalized)
    {
        expect(resolver.canonicalizePageName(pageName)).andReturn(canonicalized);
    }

    protected final void train_getName(Page page, String pageName)
    {
        expect(page.getName()).andReturn(pageName).atLeastOnce();
    }

    protected void train_findContentType(PageContentTypeAnalyzer analyzer, Page page, ContentType contentType)
    {
        expect(analyzer.findContentType(page)).andReturn(contentType).atLeastOnce();
    }

    protected final PageContentTypeAnalyzer mockPageContentTypeAnalyzer()
    {
        return newMock(PageContentTypeAnalyzer.class);
    }

    protected final PageRenderQueue mockPageRenderQueue()
    {
        return newMock(PageRenderQueue.class);
    }

    protected final void train_getRenderingPage(PageRenderQueue queue, Page page)
    {
        expect(queue.getRenderingPage()).andReturn(page);
    }

    protected final ComponentPageElementResources mockComponentPageElementResources()
    {
        return newMock(ComponentPageElementResources.class);
    }

    protected final void train_toClass(ComponentPageElementResources resources, String className, Class toClass)
    {
        expect(resources.toClass(className)).andReturn(toClass).atLeastOnce();
    }

    protected final EventContext mockEventContext()
    {
        return newMock(EventContext.class);
    }

    protected final <T> void train_get(EventContext context, Class<T> type, int index, T value)
    {
        expect(context.get(type, index)).andReturn(value);
    }

    protected final void train_getCount(EventContext context, int count)
    {
        expect(context.getCount()).andReturn(count).atLeastOnce();
    }

    protected final void train_getPropertyAdapter(ClassPropertyAdapter classPropertyAdapter, String propertyName,
                                                  PropertyAdapter propertyAdapter)
    {
        expect(classPropertyAdapter.getPropertyAdapter(propertyName)).andReturn(propertyAdapter).atLeastOnce();
    }

    protected final void train_getAdapter(PropertyAccess access, Object object,
                                          ClassPropertyAdapter classPropertyAdapter)
    {
        expect(access.getAdapter(object)).andReturn(classPropertyAdapter);
    }

    protected final RequestSecurityManager mockRequestSecurityManager()
    {
        return newMock(RequestSecurityManager.class);
    }

    protected final ClientBehaviorSupport mockClientBehaviorSupport()
    {
        return newMock(ClientBehaviorSupport.class);
    }

    protected final FieldComponent mockFieldComponent()
    {
        return newMock(FieldComponent.class);
    }

    protected final void train_setLocaleFromLocaleName(LocalizationSetter localizationSetter, String localeName,
                                                       boolean recognized)
    {
        expect(localizationSetter.setLocaleFromLocaleName(localeName)).andReturn(recognized);
    }

    protected final LocalizationSetter mockLocalizationSetter()
    {
        return newMock(LocalizationSetter.class);
    }

    protected final ComponentModelSource mockComponentModelSource()
    {
        return newMock(ComponentModelSource.class);
    }

    protected final MetaDataLocator neverWhitelistProtected()
    {
        MetaDataLocator l = mockMetaDataLocator();

        expect(l.findMeta(EasyMock.anyObject(String.class), EasyMock.anyObject(String.class), EasyMock.eq(boolean.class))).andReturn(false);

        return l;
    }
}
