// Copyright 2007, 2008, 2009, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.spring;

import org.apache.tapestry5.commons.*;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.http.internal.AbstractContributionDef;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.ModuleBuilderSource;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.DecoratorDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.plastic.PlasticUtils;
import org.apache.tapestry5.spring.ApplicationContextCustomizer;
import org.apache.tapestry5.spring.SpringConstants;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.SpringVersion;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A wrapper that converts a Spring {@link ApplicationContext} into a set of service definitions,
 * compatible with
 * Tapestry 5 IoC, for the beans defined in the context, as well as the context itself.
 */
public class SpringModuleDef implements ModuleDef
{
    static final String SERVICE_ID = "ApplicationContext";

    private final Map<String, ServiceDef> services = CollectionFactory.newMap();

    private final boolean compatibilityMode;

    private final AtomicBoolean applicationContextCreated = new AtomicBoolean(false);

    private final ServletContext servletContext;

    private ApplicationContext locateExternalContext()
    {
        ApplicationContext context = locateApplicationContext(servletContext);

        applicationContextCreated.set(true);

        return context;
    }

    /**
     * Invoked to obtain the Spring ApplicationContext, presumably stored in the ServletContext.
     * This method is only used in Tapestry 5.0 compatibility mode (in Tapestry 5.1 and above,
     * the default is for Tapestry to <em>create</em> the ApplicationContext).
     *
     * @param servletContext used to locate the ApplicationContext
     * @return the ApplicationContext itself
     * @throws RuntimeException if the ApplicationContext could not be located or is otherwise invalid
     * @since 5.2.0
     */
    protected ApplicationContext locateApplicationContext(ServletContext servletContext)
    {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);

        if (context == null)
        {
            throw new NullPointerException(
                    String
                            .format(
                                    "No Spring ApplicationContext stored in the ServletContext as attribute '%s'. "
                                            + "You should either re-enable Tapestry as the creator of the ApplicationContext, or "
                                            + "add a Spring ContextLoaderListener to web.xml.",
                                    WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE));
        }

        return context;
    }

    public SpringModuleDef(ServletContext servletContext)
    {
        this.servletContext = servletContext;

        compatibilityMode = Boolean.parseBoolean(servletContext
                .getInitParameter(SpringConstants.USE_EXTERNAL_SPRING_CONTEXT));

        final ApplicationContext externalContext = compatibilityMode ? locateExternalContext()
                : null;

        if (compatibilityMode)
            addServiceDefsForSpringBeans(externalContext);

        ServiceDef applicationContextServiceDef = new ServiceDef()
        {
            @Override
            public ObjectCreator createServiceCreator(final ServiceBuilderResources resources)
            {
                if (compatibilityMode)
                    return new StaticObjectCreator(externalContext,
                            "externally configured Spring ApplicationContext");

                ApplicationContextCustomizer customizer = resources.getService(
                        "ApplicationContextCustomizer", ApplicationContextCustomizer.class);

                return constructObjectCreatorForApplicationContext(resources, customizer);
            }

            @Override
            public String getServiceId()
            {
                return SERVICE_ID;
            }

            @Override
            public Set<Class> getMarkers()
            {
                return Collections.emptySet();
            }

            @Override
            public Class getServiceInterface()
            {
                return compatibilityMode ? externalContext.getClass()
                        : ConfigurableWebApplicationContext.class;
            }

            @Override
            public String getServiceScope()
            {
                return ScopeConstants.DEFAULT;
            }

            @Override
            public boolean isEagerLoad()
            {
                return false;
            }
        };

        services.put(SERVICE_ID, applicationContextServiceDef);
    }

    private void addServiceDefsForSpringBeans(ApplicationContext context)
    {
        ConfigurableListableBeanFactory beanFactory = null;
        if (context instanceof ConfigurableApplicationContext)
        {
            beanFactory = ((ConfigurableApplicationContext) context).getBeanFactory();
        }

        for (final String beanName : context.getBeanDefinitionNames())
        {
            boolean isAbstract = false;
            if (beanFactory != null)
            {
                isAbstract = beanFactory.getBeanDefinition(beanName).isAbstract();
            }

            if (!isAbstract)
            {
                String trueName = beanName.startsWith("&") ? beanName.substring(1) : beanName;

                services.put(trueName, new SpringBeanServiceDef(trueName, context));
            }
        }
    }

    private ObjectCreator constructObjectCreatorForApplicationContext(
            final ServiceBuilderResources resources, @Primary
    ApplicationContextCustomizer customizer)
    {
        final CustomizingContextLoader loader = new CustomizingContextLoader(customizer);

        final Runnable shutdownListener = new Runnable()
        {
            @Override
            public void run()
            {
                loader.closeWebApplicationContext(servletContext);
            }
        };

        final RegistryShutdownHub shutdownHub = resources.getService(RegistryShutdownHub.class);

        return new ObjectCreator()
        {
            @Override
            public Object createObject()
            {
                return resources.getTracker().invoke(
                        "Creating Spring ApplicationContext via ContextLoader",
                        new Invokable<Object>()
                        {
                            @Override
                            public Object invoke()
                            {
                                resources.getLogger().info(
                                        String.format("Starting Spring (version %s)", SpringVersion
                                                .getVersion()));

                                WebApplicationContext context = loader
                                        .initWebApplicationContext(servletContext);

                                shutdownHub.addRegistryShutdownListener(shutdownListener);

                                applicationContextCreated.set(true);

                                return context;
                            }
                        });
            }

            @Override
            public String toString()
            {
                return "ObjectCreator for Spring ApplicationContext";
            }
        };
    }

    @Override
    public Class getBuilderClass()
    {
        return null;
    }

    /**
     * Returns a contribution, "SpringBean", to the MasterObjectProvider service. It is ordered
     * after the built-in
     * contributions.
     */
    @Override
    public Set<ContributionDef> getContributionDefs()
    {
        ContributionDef def = createContributionToMasterObjectProvider();

        return CollectionFactory.newSet(def);
    }

    private ContributionDef createContributionToMasterObjectProvider()
    {

        ContributionDef def = new AbstractContributionDef()
        {
            @Override
            public String getServiceId()
            {
                return "MasterObjectProvider";
            }

            @Override
            public void contribute(ModuleBuilderSource moduleSource, ServiceResources resources,
                                   OrderedConfiguration configuration)
            {
                final OperationTracker tracker = resources.getTracker();

                final ApplicationContext context = resources.getService(SERVICE_ID,
                        ApplicationContext.class);

                final ObjectProvider springBeanProvider = new ObjectProvider()
                {
                    @Override
                    public <T> T provide(Class<T> objectType,
                                         AnnotationProvider annotationProvider, ObjectLocator locator)
                    {

                        Map beanMap = context.getBeansOfType(objectType);

                        switch (beanMap.size())
                        {
                            case 0:
                                return null;

                            case 1:

                                Object bean = beanMap.values().iterator().next();

                                return objectType.cast(bean);

                            default:

                                String message = String
                                        .format(
                                                "Spring context contains %d beans assignable to type %s: %s.",
                                                beanMap.size(), PlasticUtils.toTypeName(objectType), InternalUtils
                                                .joinSorted(beanMap.keySet()));

                                throw new IllegalArgumentException(message);
                        }
                    }
                };

                final ObjectProvider springBeanProviderInvoker = new ObjectProvider()
                {
                    @Override
                    public <T> T provide(final Class<T> objectType,
                                         final AnnotationProvider annotationProvider, final ObjectLocator locator)
                    {
                        return tracker.invoke(
                                "Resolving dependency by searching Spring ApplicationContext",
                                new Invokable<T>()
                                {
                                    @Override
                                    public T invoke()
                                    {
                                        return springBeanProvider.provide(objectType,
                                                annotationProvider, locator);
                                    }
                                });
                    }
                };

                ObjectProvider outerCheck = new ObjectProvider()
                {
                    @Override
                    public <T> T provide(Class<T> objectType,
                                         AnnotationProvider annotationProvider, ObjectLocator locator)
                    {
                        // I think the following line is the only reason we put the
                        // SpringBeanProvider here,
                        // rather than in SpringModule.

                        if (!applicationContextCreated.get())
                            return null;

                        return springBeanProviderInvoker.provide(objectType, annotationProvider,
                                locator);
                    }
                };


                configuration.add("SpringBean", outerCheck, "after:AnnotationBasedContributions", "after:ServiceOverride");
            }
        };

        return def;
    }

    /**
     * Returns an empty set.
     */
    @Override
    public Set<DecoratorDef> getDecoratorDefs()
    {
        return Collections.emptySet();
    }

    @Override
    public String getLoggerName()
    {
        return SpringModuleDef.class.getName();
    }

    @Override
    public ServiceDef getServiceDef(String serviceId)
    {
        return services.get(serviceId);
    }

    @Override
    public Set<String> getServiceIds()
    {
        return services.keySet();
    }
}
