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

package org.apache.tapestry5.modules;

import java.util.List;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.internal.pageload.DefaultComponentRequestSelectorAnalyzer;
import org.apache.tapestry5.internal.pageload.DefaultComponentResourceLocator;
import org.apache.tapestry5.internal.pageload.PagePreloaderImpl;
import org.apache.tapestry5.internal.services.ComponentDependencyRegistry;
import org.apache.tapestry5.internal.services.ComponentDependencyRegistryImpl;
import org.apache.tapestry5.internal.services.ComponentInstantiatorSource;
import org.apache.tapestry5.internal.services.ComponentTemplateSource;
import org.apache.tapestry5.internal.services.ComponentTemplateSourceImpl;
import org.apache.tapestry5.internal.services.InternalComponentInvalidationEventHub;
import org.apache.tapestry5.internal.services.TemplateParser;
import org.apache.tapestry5.internal.services.assets.ResourceChangeTracker;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Order;
import org.apache.tapestry5.ioc.annotations.Startup;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.ChainBuilder;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.Core;
import org.apache.tapestry5.services.pageload.ComponentRequestSelectorAnalyzer;
import org.apache.tapestry5.services.pageload.ComponentResourceLocator;
import org.apache.tapestry5.services.pageload.PageCachingReferenceTypeService;
import org.apache.tapestry5.services.pageload.PageClassLoaderContextManager;
import org.apache.tapestry5.services.pageload.PageClassLoaderContextManagerImpl;
import org.apache.tapestry5.services.pageload.PagePreloader;
import org.apache.tapestry5.services.pageload.PreloaderMode;
import org.apache.tapestry5.services.pageload.ReferenceType;
import org.apache.tapestry5.services.templates.ComponentTemplateLocator;
import org.slf4j.LoggerFactory;

/**
 * @since 5.3
 */
@SuppressWarnings("deprecation")
@Marker(Core.class)
public class PageLoadModule
{
    
    /**
     * Contributes factory defaults that may be overridden.
     */
    public static void contributeFactoryDefaults(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(SymbolConstants.MULTIPLE_CLASSLOADERS, false);
        configuration.add(SymbolConstants.COMPONENT_DEPENDENCY_FILE, ComponentDependencyRegistry.FILENAME);
    }
    
    public static void bind(ServiceBinder binder)
    {
        binder.bind(ComponentRequestSelectorAnalyzer.class, DefaultComponentRequestSelectorAnalyzer.class);
        binder.bind(ComponentResourceLocator.class, DefaultComponentResourceLocator.class);
        binder.bind(ComponentTemplateSource.class, ComponentTemplateSourceImpl.class);
        binder.bind(PagePreloader.class, PagePreloaderImpl.class);
        binder.bind(PageClassLoaderContextManager.class, PageClassLoaderContextManagerImpl.class);
    }

    @Startup
    public static void preloadPages(PagePreloader preloader,
                                    @Symbol(SymbolConstants.PRELOADER_MODE)
                                    PreloaderMode mode,
                                    @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
                                    boolean productionMode)
    {
        if (mode.isEnabledFor(productionMode))
        {
            preloader.preloadPages();
        }
    }
    
    @Startup
    @Order("before:*")
    public void preloadPageClassLoaderContexts(
            PageClassLoaderContextManager pageClassLoaderContextManager,
            ComponentDependencyRegistry componentDependencyRegistry,
            @Symbol(SymbolConstants.PRODUCTION_MODE) boolean productionMode,
            @Symbol(SymbolConstants.MULTIPLE_CLASSLOADERS) boolean multipleClassLoaders)
    {
        if (!productionMode && multipleClassLoaders)
        {
            // If we have component dependency information previously stored in 
            // a file, then we just preload the page classloader contexts.
            // Otherwise, we gather component dependency information then
            // preload the page classloader contexts.
            if (componentDependencyRegistry.isStoredDependencyInformationPresent())
            {
                pageClassLoaderContextManager.preloadContexts();
            }
            else 
            {
                LoggerFactory.getLogger(PageClassLoaderContextManager.class)
                    .warn("If the component dependency process is taking too long, "
                            + "consider writing its results to a file using the "
                            + " 'Store dependency information' button "
                            + "in the /t5dashboard/pages page.");
                pageClassLoaderContextManager.preload();
            }
        }
    }
    
    public static PageCachingReferenceTypeService buildPageCachingReferenceTypeService(
            List<PageCachingReferenceTypeService> configuration,
            ChainBuilder chainBuilder) 
    {
        return chainBuilder.build(PageCachingReferenceTypeService.class, configuration);
    }
    
    public static void contributePageCachingReferenceTypeService(
            OrderedConfiguration<PageCachingReferenceTypeService> configuration)
    {
        configuration.add("Fallback", p -> ReferenceType.SOFT, "after:*");
    }
    
    public static ComponentDependencyRegistry buildComponentDependencyRegistry(
            InternalComponentInvalidationEventHub internalComponentInvalidationEventHub,
            ResourceChangeTracker resourceChangeTracker,
            ComponentTemplateSource componentTemplateSource,
            PageClassLoaderContextManager pageClassLoaderContextManager,
            ComponentInstantiatorSource componentInstantiatorSource,
            ComponentClassResolver componentClassResolver,
            TemplateParser templateParser,
            ComponentTemplateLocator componentTemplateLocator,
            PerthreadManager perthreadManager,
            @Symbol(SymbolConstants.COMPONENT_DEPENDENCY_FILE) String componentDependencyFile,
            @Symbol(SymbolConstants.PRODUCTION_MODE) boolean productionMode)
    {
        ComponentDependencyRegistryImpl componentDependencyRegistry = 
                new ComponentDependencyRegistryImpl(
                        pageClassLoaderContextManager,
                        componentInstantiatorSource.getProxyFactory().getPlasticManager(),
                        componentClassResolver,
                        templateParser,
                        componentTemplateLocator,
                        componentDependencyFile,
                        productionMode);
        componentDependencyRegistry.listen(internalComponentInvalidationEventHub);
        componentDependencyRegistry.listen(resourceChangeTracker);
        componentDependencyRegistry.listen(componentTemplateSource.getInvalidationEventHub());
        // TODO: remove
        componentDependencyRegistry.setupThreadCleanup(perthreadManager);
        return componentDependencyRegistry;
    }

}
