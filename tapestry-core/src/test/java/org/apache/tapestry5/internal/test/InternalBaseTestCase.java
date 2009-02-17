// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.test;

import org.apache.tapestry5.*;
import org.apache.tapestry5.internal.*;
import org.apache.tapestry5.internal.parser.ComponentTemplate;
import org.apache.tapestry5.internal.parser.TemplateToken;
import org.apache.tapestry5.internal.services.*;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.internal.structure.ComponentPageElementResources;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.internal.InternalRegistry;
import org.apache.tapestry5.ioc.internal.util.MessagesImpl;
import org.apache.tapestry5.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.PropertyAdapter;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.EmbeddedComponentModel;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;
import org.apache.tapestry5.services.*;
import org.apache.tapestry5.test.TapestryTestCase;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.isA;
import org.slf4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.*;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Contains additional factory and training methods related to internal interfaces.
 */
public class InternalBaseTestCase extends TapestryTestCase implements Registry
{
    private static Registry registry;

    private Messages validationMessages;

    @BeforeSuite
    public final void setup_registry()
    {
        RegistryBuilder builder = new RegistryBuilder();

        builder.add(TapestryModule.class);

        // A synthetic module to ensure that the tapestry.alias-mode is set correctly.

        SymbolProvider provider = new SingleKeySymbolProvider(InternalSymbols.ALIAS_MODE, "servlet");
        ContributionDef contribution = new SyntheticSymbolSourceContributionDef("AliasMode", provider,
                                                                                "before:ApplicationDefaults");

        ModuleDef module = new SyntheticModuleDef(contribution);

        builder.add(module);

        registry = builder.build();

        // registry.getService(Alias.class).setMode("servlet");

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

    public final <T> T getService(String serviceId, Class<T> serviceInterface)
    {
        return registry.getService(serviceId, serviceInterface);
    }

    public final <T> T autobuild(Class<T> clazz)
    {
        return registry.autobuild(clazz);
    }

    public <T> T proxy(Class<T> interfaceClass, Class<? extends T> implementationClass)
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

    protected final <T> void train_getService(InternalRegistry registry, String serviceId, Class<T> serviceInterface,
                                              T service)
    {
        expect(registry.getService(serviceId, serviceInterface)).andReturn(service);
    }

    protected final ComponentInstantiatorSource mockComponentInstantiatorSource()
    {
        return newMock(ComponentInstantiatorSource.class);
    }

    protected final Page mockPage()
    {
        return newMock(Page.class);
    }

    protected final PageLoader mockPageLoader()
    {
        return newMock(PageLoader.class);
    }

    protected final void train_loadPage(PageLoader loader, String pageName, Locale locale, Page page)
    {
        expect(loader.loadPage(pageName, locale)).andReturn(page);
    }

    protected final PagePool mockPagePool()
    {
        return newMock(PagePool.class);
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

    protected final void train_resolvePageClassNameToPageName(ComponentClassResolver resolver, String pageClassName,
                                                              String pageName)
    {
        expect(resolver.resolvePageClassNameToPageName(pageClassName)).andReturn(pageName);
    }

    protected final void train_getContainingPage(ComponentPageElement element, Page page)
    {
        expect(element.getContainingPage()).andReturn(page).atLeastOnce();
    }

    protected final void train_getComponentResources(ComponentPageElement element, InternalComponentResources resources)
    {
        expect(element.getComponentResources()).andReturn(resources).atLeastOnce();
    }

    protected final void train_getComponentClassName(EmbeddedComponentModel model, String className)
    {
        expect(model.getComponentClassName()).andReturn(className).atLeastOnce();
    }

    protected final RenderCommand mockRenderCommand()
    {
        return newMock(RenderCommand.class);
    }

    protected final void train_getParameterNames(EmbeddedComponentModel model, String... names)
    {
        expect(model.getParameterNames()).andReturn(Arrays.asList(names));
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

    protected final EmbeddedComponentModel mockEmbeddedComponentModel()
    {
        return newMock(EmbeddedComponentModel.class);
    }

    protected final PageElementFactory mockPageElementFactory()
    {
        return newMock(PageElementFactory.class);
    }

    protected final ComponentTemplateSource mockComponentTemplateSource()
    {
        return newMock(ComponentTemplateSource.class);
    }

    protected final void train_getLogger(ComponentModel model, Logger logger)
    {
        expect(model.getLogger()).andReturn(logger).atLeastOnce();
    }

    protected final void train_getTokens(ComponentTemplate template, TemplateToken... tokens)
    {
        expect(template.getTokens()).andReturn(Arrays.asList(tokens));
    }

    protected final void train_getEmbeddedIds(ComponentModel model, String... ids)
    {
        expect(model.getEmbeddedComponentIds()).andReturn(Arrays.asList(ids));
    }

    protected void train_getTemplate(ComponentTemplateSource templateSource, ComponentModel model, Locale locale,
                                     ComponentTemplate template)
    {
        expect(templateSource.getTemplate(model, locale)).andReturn(template);
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

        expect(ins.newInstance(EasyMock.isA(InternalComponentResources.class)))
                .andReturn(component);

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

    protected final ResourceCache mockResourceCache()
    {
        return newMock(ResourceCache.class);
    }

    protected final void train_requiresDigest(ResourceCache cache, Resource resource, boolean requiresChecksum)
    {
        expect(cache.requiresDigest(resource)).andReturn(requiresChecksum);
    }

    protected final InvalidationListener mockInvalidationListener()
    {
        return newMock(InvalidationListener.class);
    }

    protected final void train_getTimeModified(ResourceCache cache, Resource resource, long timeModified)
    {
        expect(cache.getTimeModified(resource)).andReturn(timeModified).atLeastOnce();
    }

    protected final ResourceStreamer mockResourceStreamer()
    {
        return newMock(ResourceStreamer.class);
    }

    protected final void train_get(RequestPageCache cache, String pageName, Page page)
    {
        expect(cache.get(pageName)).andReturn(page).atLeastOnce();
    }

    protected final void train_findPageTemplateResource(PageTemplateLocator locator, ComponentModel model,
                                                        Locale locale, Resource resource)
    {
        expect(locator.findPageTemplateResource(model, locale)).andReturn(resource).atLeastOnce();
    }

    protected final PageTemplateLocator mockPageTemplateLocator()
    {
        return newMock(PageTemplateLocator.class);
    }

    /**
     * Returns the default validator messages.
     */
    protected final Messages validationMessages()
    {
        if (validationMessages == null)
        {
            ResourceBundle bundle = ResourceBundle
                    .getBundle("org.apache.tapestry5.internal.ValidationMessages");

            validationMessages = new MessagesImpl(Locale.ENGLISH, bundle);
        }

        return validationMessages;
    }

    protected final LinkCreationListener mockLinkCreationListener()
    {
        return newMock(LinkCreationListener.class);
    }

    protected final LinkSource mockLinkSource()
    {
        return newMock(LinkSource.class);
    }

    protected final void train_isLoaded(InternalComponentResources resources, boolean isLoaded)
    {
        expect(resources.isLoaded()).andReturn(isLoaded);
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
     * Reads the content of a file into a string. Each line is trimmed of line separators and leading/trailing
     * whitespace.
     *
     * @param trim trim each line of whitespace
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

            if (line == null) break;

            buffer.append(line);

            buffer.append("\n");
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

    protected final void train_resolvePageNameToClassName(ComponentClassResolver resolver, String pageName,
                                                          String pageClassName)
    {
        expect(resolver.resolvePageNameToClassName(pageName)).andReturn(pageClassName)
                .atLeastOnce();
    }

    protected final void train_getLocale(Page page, Locale locale)
    {
        expect(page.getLocale()).andReturn(locale).atLeastOnce();
    }

    protected final void train_detached(Page page, boolean dirty)
    {
        expect(page.detached()).andReturn(dirty);
    }

    protected void train_forName(ComponentClassCache cache, String className, Class cachedClass)
    {
        expect(cache.forName(className)).andReturn(cachedClass).atLeastOnce();
    }

    protected void train_forName(ComponentClassCache cache, Class cachedClass)
    {
        train_forName(cache, cachedClass.getName(), cachedClass);
    }

    protected final ComponentClassCache mockComponentClassCache()
    {
        return newMock(ComponentClassCache.class);
    }

    protected void train_findContentType(PageContentTypeAnalyzer analyzer, Page page, ContentType contentType)
    {
        expect(analyzer.findContentType(page)).andReturn(contentType).atLeastOnce();
    }

    protected final PageContentTypeAnalyzer mockPageContentTypeAnalyzer()
    {
        return newMock(PageContentTypeAnalyzer.class);
    }


    protected final RequestPathOptimizer mockRequestPathOptimizer()
    {
        return newMock(RequestPathOptimizer.class);
    }

    protected final void train_optimizePath(RequestPathOptimizer optimizer, String path, String optimizedPath)
    {
        expect(optimizer.optimizePath(path)).andReturn(optimizedPath);
    }

    protected final ActionRenderResponseGenerator mockActionRenderResponseGenerator()
    {
        return newMock(ActionRenderResponseGenerator.class);
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

    protected final <S, T> void train_coerce(ComponentPageElementResources componentPageElementResources, S input,
                                             Class<T> expectedType,
                                             T coercedValue)
    {
        expect(componentPageElementResources.coerce(input, expectedType)).andReturn(coercedValue);
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

    protected final void train_getPropertyAdapter(ClassPropertyAdapter classPropertyAdapter,
                                                  String propertyName, PropertyAdapter propertyAdapter)
    {
        expect(classPropertyAdapter.getPropertyAdapter(propertyName)).andReturn(propertyAdapter)
                .atLeastOnce();
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

    protected final MutableComponentModel mockMutableComponentModel(Logger logger)
    {
        MutableComponentModel model = mockMutableComponentModel();
        train_getLogger(model, logger);

        return model;
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
}
