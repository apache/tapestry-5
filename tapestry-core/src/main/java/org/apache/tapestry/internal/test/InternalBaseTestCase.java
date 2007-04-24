// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.test;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;

import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ComponentResourcesCommon;
import org.apache.tapestry.Link;
import org.apache.tapestry.events.InvalidationListener;
import org.apache.tapestry.internal.InternalComponentResources;
import org.apache.tapestry.internal.parser.ComponentTemplate;
import org.apache.tapestry.internal.parser.TemplateToken;
import org.apache.tapestry.internal.services.ComponentInstantiatorSource;
import org.apache.tapestry.internal.services.ComponentInvocationMap;
import org.apache.tapestry.internal.services.ComponentTemplateSource;
import org.apache.tapestry.internal.services.FormParameterLookup;
import org.apache.tapestry.internal.services.Instantiator;
import org.apache.tapestry.internal.services.LinkFactory;
import org.apache.tapestry.internal.services.LinkFactoryListener;
import org.apache.tapestry.internal.services.PageElementFactory;
import org.apache.tapestry.internal.services.PageLoader;
import org.apache.tapestry.internal.services.PagePool;
import org.apache.tapestry.internal.services.PageResponseRenderer;
import org.apache.tapestry.internal.services.PageTemplateLocator;
import org.apache.tapestry.internal.services.RequestPageCache;
import org.apache.tapestry.internal.services.ResourceCache;
import org.apache.tapestry.internal.services.ResourceStreamer;
import org.apache.tapestry.internal.services.TemplateParser;
import org.apache.tapestry.internal.structure.ComponentPageElement;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.internal.structure.PageElement;
import org.apache.tapestry.ioc.Location;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.RegistryBuilder;
import org.apache.tapestry.ioc.Resource;
import org.apache.tapestry.ioc.internal.InternalRegistry;
import org.apache.tapestry.ioc.internal.Module;
import org.apache.tapestry.ioc.internal.util.MessagesImpl;
import org.apache.tapestry.model.ComponentModel;
import org.apache.tapestry.model.EmbeddedComponentModel;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.runtime.RenderQueue;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.Infrastructure;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.TapestryModule;
import org.apache.tapestry.test.TapestryTestCase;
import org.easymock.EasyMock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Contains additional factory and training methods related to internal interfaces.
 */
public class InternalBaseTestCase extends TapestryTestCase implements Registry
{
    private static Registry _registry;

    private Messages _validationMessages;

    @BeforeSuite
    public final void setup_registry()
    {
        RegistryBuilder builder = new RegistryBuilder();

        builder.add(TapestryModule.class);

        _registry = builder.build();

        _registry.getService(Infrastructure.class).setMode("servlet");
    }

    @AfterSuite
    public final void shutdown_registry()
    {
        _registry.shutdown();

        _registry = null;
    }

    @AfterMethod
    public final void cleanupThread()
    {
        _registry.cleanupThread();
    }

    public final <T> T getObject(String reference, Class<T> objectType)
    {
        return _registry.getObject(reference, objectType);
    }

    public final <T> T getService(Class<T> serviceInterface)
    {
        return _registry.getService(serviceInterface);
    }

    public final <T> T getService(String serviceId, Class<T> serviceInterface)
    {
        return _registry.getService(serviceId, serviceInterface);
    }

    public final void shutdown()
    {
        throw new UnsupportedOperationException("No registry shutdown until @AfterSuite.");
    }

    protected final InternalComponentResources newInternalComponentResources()
    {
        return newMock(InternalComponentResources.class);
    }

    protected final ComponentTemplate newComponentTemplate()
    {
        return newMock(ComponentTemplate.class);
    }

    protected final <T> void train_getService(InternalRegistry registry, String serviceId,
            Class<T> serviceInterface, Module module, T service)
    {
        expect(registry.getService(serviceId, serviceInterface, module)).andReturn(service);

    }

    protected final ComponentInstantiatorSource newComponentInstantiatorSource()
    {
        return newMock(ComponentInstantiatorSource.class);
    }

    protected final Page newPage()
    {
        return newMock(Page.class);
    }

    protected final PageLoader newPageLoader()
    {
        return newMock(PageLoader.class);
    }

    protected final void train_loadPage(PageLoader loader, String pageName, Locale locale, Page page)
    {
        expect(loader.loadPage(pageName, locale)).andReturn(page);
    }

    protected final void train_getName(Page page, String name)
    {
        expect(page.getName()).andReturn(name).atLeastOnce();
    }

    protected final PagePool newPagePool()
    {
        return newMock(PagePool.class);
    }

    protected RenderQueue newRenderQueue()
    {
        return newMock(RenderQueue.class);
    }

    protected final void train_parseTemplate(TemplateParser parser, Resource resource,
            ComponentTemplate template)
    {
        expect(parser.parseTemplate(resource)).andReturn(template);
    }

    protected final TemplateParser newTemplateParser()
    {
        return newMock(TemplateParser.class);
    }

    protected final ComponentPageElement newComponentPageElement()
    {
        return newMock(ComponentPageElement.class);
    }

    protected final void train_getComponent(ComponentPageElement element, Component component)
    {
        expect(element.getComponent()).andReturn(component).atLeastOnce();
    }

    protected final void train_getId(ComponentResourcesCommon resources, String id)
    {
        expect(resources.getId()).andReturn(id).atLeastOnce();
    }

    protected final void train_getNestedId(ComponentResourcesCommon resources, String nestedId)
    {
        expect(resources.getNestedId()).andReturn(nestedId).atLeastOnce();
    }

    protected final void train_getContextPath(Request request, String contextPath)
    {
        expect(request.getContextPath()).andReturn(contextPath).atLeastOnce();
    }

    protected final void train_resolvePageClassNameToPageName(ComponentClassResolver resolver,
            String pageClassName, String pageName)
    {
        expect(resolver.resolvePageClassNameToPageName(pageClassName)).andReturn(pageName);
    }

    protected final void train_getContainingPage(ComponentPageElement element, Page page)
    {
        expect(element.getContainingPage()).andReturn(page).atLeastOnce();
    }

    protected final void train_getComponentResources(ComponentPageElement element,
            InternalComponentResources resources)
    {
        expect(element.getComponentResources()).andReturn(resources).atLeastOnce();
    }

    protected final void train_getComponentClassName(EmbeddedComponentModel model, String className)
    {
        expect(model.getComponentClassName()).andReturn(className).atLeastOnce();
    }

    protected final void train_newRenderBodyElement(PageElementFactory elementFactory,
            ComponentPageElement component, PageElement body)
    {
        expect(elementFactory.newRenderBodyElement(component)).andReturn(body);
    }

    protected final PageElement newPageElement()
    {
        return newMock(PageElement.class);
    }

    protected final void train_getParameterNames(EmbeddedComponentModel model, String... names)
    {
        expect(model.getParameterNames()).andReturn(Arrays.asList(names));
    }

    protected final void train_newComponentElement(PageElementFactory elementFactory,
            ComponentPageElement container, String embeddedId, String embeddedType,
            String componentClassName, String elementName, Location location,
            ComponentPageElement embedded)
    {
        expect(
                elementFactory.newComponentElement(
                        isA(Page.class),
                        eq(container),
                        eq(embeddedId),
                        eq(embeddedType),
                        eq(componentClassName),
                        eq(elementName),
                        eq(location))).andReturn(embedded);
    }

    protected final void train_getComponentType(EmbeddedComponentModel emodel, String componentType)
    {
        expect(emodel.getComponentType()).andReturn(componentType).atLeastOnce();
    }

    protected final void train_getEmbeddedComponentModel(ComponentModel model, String embeddedId,
            EmbeddedComponentModel emodel)
    {
        expect(model.getEmbeddedComponentModel(embeddedId)).andReturn(emodel).atLeastOnce();
    }

    protected final EmbeddedComponentModel newEmbeddedComponentModel()
    {
        return newMock(EmbeddedComponentModel.class);
    }

    protected final PageElementFactory newPageElementFactory()
    {
        return newMock(PageElementFactory.class);
    }

    protected final ComponentTemplateSource newComponentTemplateSource()
    {
        return newMock(ComponentTemplateSource.class);
    }

    protected final void train_getLog(ComponentModel model, Log log)
    {
        expect(model.getLog()).andReturn(log).atLeastOnce();
    }

    protected final void train_getTokens(ComponentTemplate template, TemplateToken... tokens)
    {
        expect(template.getTokens()).andReturn(Arrays.asList(tokens));
    }

    protected final void train_getComponentIds(ComponentTemplate template, String... ids)
    {
        expect(template.getComponentIds()).andReturn(newSet(Arrays.asList(ids)));
    }

    protected final void train_getEmbeddedIds(ComponentModel model, String... ids)
    {
        expect(model.getEmbeddedComponentIds()).andReturn(Arrays.asList(ids));
    }

    protected void train_getTemplate(ComponentTemplateSource templateSource, ComponentModel model,
            Locale locale, ComponentTemplate template)
    {
        expect(templateSource.getTemplate(model, locale)).andReturn(template);
    }

    protected final void train_getComponentModel(ComponentResources resources, ComponentModel model)
    {
        expect(resources.getComponentModel()).andReturn(model).atLeastOnce();
    }

    protected final void train_newRootComponentElement(PageElementFactory elementFactory,
            String className, ComponentPageElement rootElement)
    {
        expect(elementFactory.newRootComponentElement(isA(Page.class), eq(className))).andReturn(
                rootElement);
    }

    protected final void train_getModel(Instantiator ins, ComponentModel model)
    {
        expect(ins.getModel()).andReturn(model).atLeastOnce();
    }

    protected final Instantiator newInstantiator(Component component)
    {
        Instantiator ins = newMock(Instantiator.class);

        expect(ins.newInstance(EasyMock.isA(InternalComponentResources.class)))
                .andReturn(component);

        return ins;
    }

    protected final void train_getByClassName(RequestPageCache cache, String pageClassName,
            Page page)
    {
        expect(cache.getByClassName(pageClassName)).andReturn(page).atLeastOnce();
    }

    protected final RequestPageCache newRequestPageCache()
    {
        return newMock(RequestPageCache.class);
    }

    protected final void train_getComponentElementByNestedId(Page page, String nestedId,
            ComponentPageElement element)
    {
        expect(page.getComponentElementByNestedId(nestedId)).andReturn(element).atLeastOnce();
    }

    protected final void train_getRootElement(Page page, ComponentPageElement element)
    {
        expect(page.getRootElement()).andReturn(element).atLeastOnce();
    }

    protected final void train_isMissing(ComponentTemplate template, boolean isMissing)
    {
        expect(template.isMissing()).andReturn(isMissing).atLeastOnce();
    }

    protected final void train_getMixinClassNames(EmbeddedComponentModel model, String... names)
    {
        expect(model.getMixinClassNames()).andReturn(Arrays.asList(names));
    }

    protected final void train_getRootComponent(Page page, Component component)
    {
        expect(page.getRootComponent()).andReturn(component).atLeastOnce();
    }

    protected final ResourceCache newResourceCache()
    {
        return newMock(ResourceCache.class);
    }

    protected final void train_requiresDigest(ResourceCache cache, Resource resource,
            boolean requiresChecksum)
    {
        expect(cache.requiresDigest(resource)).andReturn(requiresChecksum);
    }

    protected InvalidationListener newInvalidationListener()
    {
        return newMock(InvalidationListener.class);
    }

    protected void train_getTimeModified(ResourceCache cache, Resource resource, long timeModified)
    {
        expect(cache.getTimeModified(resource)).andReturn(timeModified).atLeastOnce();
    }

    protected final ResourceStreamer newResourceStreamer()
    {
        return newMock(ResourceStreamer.class);
    }

    protected final void train_get(RequestPageCache cache, String pageName, Page page)
    {
        expect(cache.get(pageName)).andReturn(page).atLeastOnce();
    }

    protected final void train_findPageTemplateResource(PageTemplateLocator locator,
            ComponentModel model, Locale locale, Resource resource)
    {
        expect(locator.findPageTemplateResource(model, locale)).andReturn(resource).atLeastOnce();
    }

    protected final PageTemplateLocator newPageTemplateLocator()
    {
        return newMock(PageTemplateLocator.class);
    }

    /** Returns the default validator messages. */
    protected final Messages validationMessages()
    {
        if (_validationMessages == null)
        {
            ResourceBundle bundle = ResourceBundle
                    .getBundle("org.apache.tapestry.internal.ValidationMessages");

            _validationMessages = new MessagesImpl(bundle);
        }

        return _validationMessages;
    }

    protected final LinkFactoryListener newLinkFactoryListener()
    {
        return newMock(LinkFactoryListener.class);
    }

    protected ComponentInvocationMap newComponentInvocationMap()
    {
        return newMock(ComponentInvocationMap.class);
    }

    protected final LinkFactory newLinkFactory()
    {
        return newMock(LinkFactory.class);
    }

    protected final void train_createPageLink(LinkFactory factory, Page page, Link link)
    {
        expect(factory.createPageLink(page)).andReturn(link);
    }

    protected final FormParameterLookup newFormParameterLookup()
    {
        return newMock(FormParameterLookup.class);
    }

    protected final void train_getParameter(FormParameterLookup lookup, String elementName,
            String value)
    {
        expect(lookup.getParameter(elementName)).andReturn(value).atLeastOnce();
    }

    protected final void train_isLoaded(InternalComponentResources resources, boolean isLoaded)
    {
        expect(resources.isLoaded()).andReturn(isLoaded);
    }

    protected final void stub_isPageName(ComponentClassResolver resolver, boolean result)
    {
        expect(resolver.isPageName(isA(String.class))).andStubReturn(result);
    }

    protected final void train_isPageName(ComponentClassResolver resolver, String pageName,
            boolean result)
    {
        expect(resolver.isPageName(pageName)).andReturn(result);
    }

    protected final PageResponseRenderer newPageResponseRenderer()
    {
        return newMock(PageResponseRenderer.class);
    }
}
