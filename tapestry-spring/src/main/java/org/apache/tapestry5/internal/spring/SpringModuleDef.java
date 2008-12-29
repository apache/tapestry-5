// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.spring;

import org.apache.tapestry5.internal.AbstractContributionDef;
import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.DecoratorDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.Invokable;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.RegistryShutdownListener;
import org.springframework.context.ApplicationContext;
import org.springframework.core.SpringVersion;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A wrapper that converts a Spring {@link ApplicationContext} into a set of service definitions, compatible with
 * Tapestry 5 IoC, for the beans defined in the context, as well as the context itself.
 */
public class SpringModuleDef implements ModuleDef
{
    private static final String SERVICE_ID = "ApplicationContext";

    private final Map<String, ServiceDef> services = CollectionFactory.newMap();

    private final AtomicBoolean applicationContextCreated = new AtomicBoolean(false);

    public SpringModuleDef(final ServletContext servletContext, final ApplicationContextCustomizer customizer)
    {
        ServiceDef sd = new ServiceDef()
        {
            public ObjectCreator createServiceCreator(final ServiceBuilderResources resources)
            {
                final CustomizingContextLoader loader = new CustomizingContextLoader(
                        customizer);

                final RegistryShutdownListener shutdownListener = new RegistryShutdownListener()
                {
                    public void registryDidShutdown()
                    {
                        loader.closeWebApplicationContext(servletContext);
                    }
                };

                final RegistryShutdownHub shutdownHub = resources.getService(RegistryShutdownHub.class);


                return new ObjectCreator()
                {
                    public Object createObject()
                    {
                        return resources.getTracker().invoke(
                                "Creating Spring ApplicationContext via ContextLoader",
                                new Invokable<Object>()
                                {
                                    public Object invoke()
                                    {
                                        resources.getLogger().info(String.format(
                                                "Starting Spring (version %s)",
                                                SpringVersion.getVersion()));

                                        WebApplicationContext context = loader.initWebApplicationContext(
                                                servletContext);

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

            public String getServiceId()
            {
                return SERVICE_ID;
            }

            public Set<Class> getMarkers()
            {
                return Collections.emptySet();
            }

            public Class getServiceInterface()
            {
                return ConfigurableWebApplicationContext.class;
            }

            public String getServiceScope()
            {
                return ScopeConstants.DEFAULT;
            }

            public boolean isEagerLoad()
            {
                return false;
            }
        };

        services.put(SERVICE_ID, sd);
    }

    public Class getBuilderClass()
    {
        return null;
    }

    /**
     * Returns a contribution, "SpringBean", to the MasterObjectProvider service.  It is ordered after the built-in
     * contributions.
     */
    public Set<ContributionDef> getContributionDefs()
    {
        ContributionDef def = createContributionToMasterObjectProvider();

        return CollectionFactory.newSet(def);
    }

    private ContributionDef createContributionToMasterObjectProvider()
    {
        final ObjectProvider springBeanProvider = new ObjectProvider()
        {
            public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator)
            {
                ApplicationContext context = locator.getService(SERVICE_ID, ApplicationContext.class);

                Map beanMap = context.getBeansOfType(objectType);

                switch (beanMap.size())
                {
                    case 0:
                        return null;

                    case 1:

                        Object bean = beanMap.values().iterator().next();

                        return objectType.cast(bean);

                    default:

                        String message = String.format("Spring context contains %d beans assignable to type %s: %s.",
                                                       beanMap.size(),
                                                       ClassFabUtils.toJavaClassName(objectType),
                                                       InternalUtils.joinSorted(beanMap.keySet()));

                        throw new IllegalArgumentException(message);
                }
            }
        };

        ContributionDef def = new AbstractContributionDef()
        {
            public String getServiceId()
            {
                return "MasterObjectProvider";
            }

            @Override
            public void contribute(ModuleBuilderSource moduleBuilderSource, ServiceResources resources,
                                   OrderedConfiguration configuration)
            {
                final OperationTracker tracker = resources.getTracker();

                final ObjectProvider springBeanProviderInvoker = new ObjectProvider()
                {
                    public <T> T provide(final Class<T> objectType, final AnnotationProvider annotationProvider,
                                         final ObjectLocator locator)
                    {
                        return tracker.invoke(
                                "Resolving dependency by searching Spring ApplicationContext",
                                new Invokable<T>()
                                {
                                    public T invoke()
                                    {
                                        return springBeanProvider.provide(objectType, annotationProvider, locator);
                                    }
                                });
                    }
                };

                ObjectProvider outerCheck = new ObjectProvider()
                {
                    public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider,
                                         ObjectLocator locator)
                    {
                        if (!applicationContextCreated.get()) return null;

                        return springBeanProviderInvoker.provide(objectType, annotationProvider, locator);
                    }
                };


                configuration.add("SpringBean", outerCheck, "after:Service,Alias,Autobuild");
            }
        };

        return def;
    }

    /**
     * Returns an empty set.
     */
    public Set<DecoratorDef> getDecoratorDefs()
    {
        return Collections.emptySet();
    }

    public String getLoggerName()
    {
        return "Spring";
    }

    public ServiceDef getServiceDef(String serviceId)
    {
        return services.get(serviceId);
    }

    public Set<String> getServiceIds()
    {
        return services.keySet();
    }
}
