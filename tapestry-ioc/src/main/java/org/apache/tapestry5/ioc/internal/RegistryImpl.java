// Copyright 2006-2014 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.commons.*;
import org.apache.tapestry5.commons.internal.NullAnnotationProvider;
import org.apache.tapestry5.commons.internal.util.*;
import org.apache.tapestry5.commons.services.*;
import org.apache.tapestry5.commons.util.AvailableValues;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.ioc.AdvisorDef;
import org.apache.tapestry5.ioc.IOCConstants;
import org.apache.tapestry5.ioc.IOOperation;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.ServiceAdvisor;
import org.apache.tapestry5.ioc.ServiceBuilderResources;
import org.apache.tapestry5.ioc.ServiceDecorator;
import org.apache.tapestry5.ioc.ServiceLifecycle;
import org.apache.tapestry5.ioc.ServiceLifecycle2;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.annotations.Local;
import org.apache.tapestry5.ioc.def.*;
import org.apache.tapestry5.ioc.internal.services.PerthreadManagerImpl;
import org.apache.tapestry5.ioc.internal.services.RegistryShutdownHubImpl;
import org.apache.tapestry5.ioc.internal.util.InjectionResources;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.JDKUtils;
import org.apache.tapestry5.ioc.internal.util.MapInjectionResources;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.apache.tapestry5.ioc.internal.util.Orderer;
import org.apache.tapestry5.ioc.modules.TapestryIOCModule;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.MasterObjectProvider;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.RegistryShutdownListener;
import org.apache.tapestry5.ioc.services.ServiceActivityScoreboard;
import org.apache.tapestry5.ioc.services.ServiceConfigurationListener;
import org.apache.tapestry5.ioc.services.ServiceConfigurationListenerHub;
import org.apache.tapestry5.ioc.services.ServiceLifecycleSource;
import org.apache.tapestry5.ioc.services.Status;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.ioc.services.UpdateListenerHub;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.Map.Entry;

@SuppressWarnings("all")
public class RegistryImpl implements Registry, InternalRegistry, ServiceProxyProvider
{
    private static final String SYMBOL_SOURCE_SERVICE_ID = "SymbolSource";

    private static final String REGISTRY_SHUTDOWN_HUB_SERVICE_ID = "RegistryShutdownHub";

    static final String PERTHREAD_MANAGER_SERVICE_ID = "PerthreadManager";

    private static final String SERVICE_ACTIVITY_SCOREBOARD_SERVICE_ID = "ServiceActivityScoreboard";

    /**
     * The set of marker annotations for a builtin service.
     */
    private final static Set<Class> BUILTIN = CollectionFactory.newSet();

    // Split create/assign to appease generics gods
    static
    {
        BUILTIN.add(Builtin.class);
    }


    static final String PLASTIC_PROXY_FACTORY_SERVICE_ID = "PlasticProxyFactory";

    static final String LOGGER_SOURCE_SERVICE_ID = "LoggerSource";
    
    private final OneShotLock lock = new OneShotLock();

    private final OneShotLock eagerLoadLock = new OneShotLock();

    private final Map<String, Object> builtinServices = CollectionFactory.newCaseInsensitiveMap();

    private final Map<String, Class> builtinTypes = CollectionFactory.newCaseInsensitiveMap();

    private final RegistryShutdownHubImpl registryShutdownHub;

    private final LoggerSource loggerSource;

    /**
     * Map from service id to the Module that contains the service.
     */
    private final Map<String, Module> serviceIdToModule = CollectionFactory.newCaseInsensitiveMap();

    private final Map<String, ServiceLifecycle2> lifecycles = CollectionFactory.newCaseInsensitiveMap();

    private final PerthreadManager perthreadManager;

    private final PlasticProxyFactory proxyFactory;

    private final ServiceActivityTracker tracker;

    private SymbolSource symbolSource;

    private final Map<Module, Set<ServiceDef2>> moduleToServiceDefs = CollectionFactory.newMap();

    /**
     * From marker type to a list of marked service instances.
     */
    private final Map<Class, List<ServiceDef2>> markerToServiceDef = CollectionFactory.newMap();

    private final Set<ServiceDef2> allServiceDefs = CollectionFactory.newSet();

    private final OperationTracker operationTracker;

    private final TypeCoercerProxy typeCoercerProxy = new TypeCoercerProxyImpl(this);

    private final Map<Class<? extends Annotation>, Annotation> cachedAnnotationProxies = CollectionFactory.newConcurrentMap();

    private final Set<Runnable> startups = CollectionFactory.newSet();
    
    private DelegatingServiceConfigurationListener serviceConfigurationListener;
    
    /**
     * Constructs the registry from a set of module definitions and other resources.
     *
     * @param moduleDefs
     *         defines the modules (and builders, decorators, etc., within)
     * @param proxyFactory
     *         used to create new proxy objects
     * @param loggerSource
     *         used to obtain Logger instances
     * @param operationTracker
     */
    public RegistryImpl(Collection<ModuleDef2> moduleDefs, PlasticProxyFactory proxyFactory,
                        LoggerSource loggerSource, OperationTracker operationTracker)
    {
        assert moduleDefs != null;
        assert proxyFactory != null;
        assert loggerSource != null;
        assert operationTracker != null;

        this.loggerSource = loggerSource;
        this.operationTracker = operationTracker;

        this.proxyFactory = proxyFactory;
        
        serviceConfigurationListener = new DelegatingServiceConfigurationListener(
                loggerForBuiltinService(ServiceConfigurationListener.class.getSimpleName()));
        
        Logger logger = loggerForBuiltinService(PERTHREAD_MANAGER_SERVICE_ID);

        PerthreadManagerImpl ptmImpl = new PerthreadManagerImpl(logger);

        perthreadManager = ptmImpl;

        final ServiceActivityTrackerImpl scoreboardAndTracker = new ServiceActivityTrackerImpl(perthreadManager);

        tracker = scoreboardAndTracker;

        logger = loggerForBuiltinService(REGISTRY_SHUTDOWN_HUB_SERVICE_ID);

        registryShutdownHub = new RegistryShutdownHubImpl(logger);
        ptmImpl.registerForShutdown(registryShutdownHub);

        lifecycles.put("singleton", new SingletonServiceLifecycle());

        registryShutdownHub.addRegistryShutdownListener(new Runnable()
        {
            @Override
            public void run()
            {
                scoreboardAndTracker.shutdown();
            }
        });

        for (ModuleDef2 def : moduleDefs)
        {
            logger = this.loggerSource.getLogger(def.getLoggerName());

            Module module = new ModuleImpl(this, tracker, def, proxyFactory, logger);

            Set<ServiceDef2> moduleServiceDefs = CollectionFactory.newSet();

            for (String serviceId : def.getServiceIds())
            {
                ServiceDef2 serviceDef = module.getServiceDef(serviceId);

                moduleServiceDefs.add(serviceDef);
                allServiceDefs.add(serviceDef);

                Module existing = serviceIdToModule.get(serviceId);

                if (existing != null)
                    throw new RuntimeException(IOCMessages.serviceIdConflict(serviceId,
                            existing.getServiceDef(serviceId), serviceDef));

                serviceIdToModule.put(serviceId, module);

                // The service is defined but will not have gone further than that.
                tracker.define(serviceDef, Status.DEFINED);

                for (Class marker : serviceDef.getMarkers())
                    InternalUtils.addToMapList(markerToServiceDef, marker, serviceDef);
            }

            moduleToServiceDefs.put(module, moduleServiceDefs);

            addStartupsInModule(def, module, logger);
        }

        addBuiltin(SERVICE_ACTIVITY_SCOREBOARD_SERVICE_ID, ServiceActivityScoreboard.class, scoreboardAndTracker);
        addBuiltin(LOGGER_SOURCE_SERVICE_ID, LoggerSource.class, this.loggerSource);
        addBuiltin(PERTHREAD_MANAGER_SERVICE_ID, PerthreadManager.class, perthreadManager);
        addBuiltin(REGISTRY_SHUTDOWN_HUB_SERVICE_ID, RegistryShutdownHub.class, registryShutdownHub);
        addBuiltin(PLASTIC_PROXY_FACTORY_SERVICE_ID, PlasticProxyFactory.class, proxyFactory);

        validateContributeDefs(moduleDefs);
        
        serviceConfigurationListener.setDelegates(getService(ServiceConfigurationListenerHub.class).getListeners());

        scoreboardAndTracker.startup();

        SerializationSupport.setProvider(this);
        
    }

    private void addStartupsInModule(ModuleDef2 def, final Module module, final Logger logger)
    {
        for (final StartupDef startup : def.getStartups())
        {

            startups.add(new Runnable()
            {
                @Override
                public void run()
                {
                    startup.invoke(module, RegistryImpl.this, RegistryImpl.this, logger);
                }
            });
        }
    }

    /**
     * Validate that each module's ContributeDefs correspond to an actual service.
     */
    private void validateContributeDefs(Collection<ModuleDef2> moduleDefs)
    {
        for (ModuleDef2 module : moduleDefs)
        {
            Set<ContributionDef> contributionDefs = module.getContributionDefs();

            for (ContributionDef cd : contributionDefs)
            {
                String serviceId = cd.getServiceId();

                ContributionDef3 cd3 = InternalUtils.toContributionDef3(cd);

                // Ignore any optional contribution methods; there's no way to validate that
                // they contribute to a known service ... that's the point of @Optional

                if (cd3.isOptional())
                {
                    continue;
                }

                // Otherwise, check that the service being contributed to exists ...

                if (cd3.getServiceId() != null)
                {
                    if (!serviceIdToModule.containsKey(serviceId))
                    {
                        throw new IllegalArgumentException(
                                IOCMessages.contributionForNonexistentService(cd));
                    }
                } else if (!isContributionForExistentService(module, cd3))
                {
                    throw new IllegalArgumentException(
                            IOCMessages.contributionForUnqualifiedService(cd3));
                }
            }
        }

    }

    /**
     * Invoked when the contribution method didn't follow the naming convention and so doesn't identify
     * a service by id; instead there was an @Contribute to identify the service interface.
     */
    @SuppressWarnings("all")
    private boolean isContributionForExistentService(ModuleDef moduleDef, final ContributionDef2 cd)
    {
        final Set<Class> contributionMarkers = new HashSet(cd.getMarkers());

        boolean localOnly = contributionMarkers.contains(Local.class);

        Flow<ServiceDef2> serviceDefs = localOnly ? getLocalServiceDefs(moduleDef) : F.flow(allServiceDefs);

        contributionMarkers.retainAll(getMarkerAnnotations());
        contributionMarkers.remove(Local.class);

        // Match services with the correct interface AND having as markers *all* the marker annotations

        Flow<ServiceDef2> filtered = serviceDefs.filter(F.and(new Predicate<ServiceDef2>()
                                                              {
                                                                  @Override
                                                                  public boolean accept(ServiceDef2 object)
                                                                  {
                                                                      return object.getServiceInterface().equals(cd.getServiceInterface());
                                                                  }
                                                              }, new Predicate<ServiceDef2>()
                                                              {
                                                                  @Override
                                                                  public boolean accept(ServiceDef2 serviceDef)
                                                                  {
                                                                      return serviceDef.getMarkers().containsAll(contributionMarkers);
                                                                  }
                                                              }
        ));

        // That's a lot of logic; the good news is it will short-circuit as soon as it finds a single match,
        // thanks to the laziness inside Flow.

        return !filtered.isEmpty();
    }

    private Flow<ServiceDef2> getLocalServiceDefs(final ModuleDef moduleDef)
    {
        return F.flow(moduleDef.getServiceIds()).map(new Mapper<String, ServiceDef2>()
        {
            @Override
            public ServiceDef2 map(String value)
            {
                return InternalUtils.toServiceDef2(moduleDef.getServiceDef(value));
            }
        });
    }

    /**
     * It's not unreasonable for an eagerly-loaded service to decide to start a thread, at which
     * point we raise issues
     * about improper publishing of the Registry instance from the RegistryImpl constructor. Moving
     * eager loading of
     * services out to its own method should ensure thread safety.
     */
    @Override
    public void performRegistryStartup()
    {
        if (JDKUtils.JDK_1_5)
        {
            throw new RuntimeException("Your JDK version is too old."
                    + " Tapestry requires Java 1.6 or newer since version 5.4.");
        }
        eagerLoadLock.lock();

        List<EagerLoadServiceProxy> proxies = CollectionFactory.newList();

        for (Module m : moduleToServiceDefs.keySet())
            m.collectEagerLoadServices(proxies);

        // TAPESTRY-2267: Gather up all the proxies before instantiating any of them.

        for (EagerLoadServiceProxy proxy : proxies)
        {
            proxy.eagerLoadService();
        }

        for (Runnable startup : startups) {
            startup.run();
        }

        startups.clear();

        getService("RegistryStartup", Runnable.class).run();

        cleanupThread();
    }

    @Override
    public Logger getServiceLogger(String serviceId)
    {
        Module module = serviceIdToModule.get(serviceId);

        assert module != null;

        return loggerSource.getLogger(module.getLoggerName() + "." + serviceId);
    }

    private Logger loggerForBuiltinService(String serviceId)
    {
        return loggerSource.getLogger(TapestryIOCModule.class.getName() + "." + serviceId);
    }

    private <T> void addBuiltin(final String serviceId, final Class<T> serviceInterface, T service)
    {
        builtinTypes.put(serviceId, serviceInterface);
        builtinServices.put(serviceId, service);

        // Make sure each of the builtin services is also available via the Builtin annotation
        // marker.

        ServiceDef2 serviceDef = new ServiceDef2()
        {
            @Override
            public ObjectCreator createServiceCreator(ServiceBuilderResources resources)
            {
                return null;
            }

            @Override
            public Set<Class> getMarkers()
            {
                return BUILTIN;
            }

            @Override
            public String getServiceId()
            {
                return serviceId;
            }

            @Override
            public Class getServiceInterface()
            {
                return serviceInterface;
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

            @Override
            public boolean isPreventDecoration()
            {
                return true;
            }
            
            @Override
            public int hashCode()
            {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((serviceId == null) ? 0 : serviceId.hashCode());
                return result;
            }

            @Override
            public boolean equals(Object obj)
            {
                if (this == obj) { return true; }
                if (obj == null) { return false; }
                if (!(obj instanceof ServiceDefImpl)) { return false; }
                ServiceDef other = (ServiceDef) obj;
                if (serviceId == null)
                {
                    if (other.getServiceId() != null) { return false; }
                }
                else if (!serviceId.equals(other.getServiceId())) { return false; }
                return true;
            }
            
        };

        for (Class marker : serviceDef.getMarkers())
        {
            InternalUtils.addToMapList(markerToServiceDef, marker, serviceDef);
            allServiceDefs.add(serviceDef);
        }

        tracker.define(serviceDef, Status.BUILTIN);
    }

    @Override
    public synchronized void shutdown()
    {
        lock.lock();

        registryShutdownHub.fireRegistryDidShutdown();

        SerializationSupport.clearProvider(this);
    }

    @Override
    public <T> T getService(String serviceId, Class<T> serviceInterface)
    {
        lock.check();

        T result = checkForBuiltinService(serviceId, serviceInterface);
        if (result != null)
            return result;

        // Checking serviceId and serviceInterface is overkill; they have been checked and rechecked
        // all the way to here.

        Module containingModule = locateModuleForService(serviceId);

        return containingModule.getService(serviceId, serviceInterface);
    }

    private <T> T checkForBuiltinService(String serviceId, Class<T> serviceInterface)
    {
        Object service = builtinServices.get(serviceId);

        if (service == null)
            return null;

        try
        {
            return serviceInterface.cast(service);
        } catch (ClassCastException ex)
        {
            throw new RuntimeException(IOCMessages.serviceWrongInterface(serviceId, builtinTypes.get(serviceId),
                    serviceInterface));
        }
    }

    @Override
    public void cleanupThread()
    {
        lock.check();

        perthreadManager.cleanup();
    }

    private Module locateModuleForService(String serviceId)
    {
        Module module = serviceIdToModule.get(serviceId);

        if (module == null)
            throw new UnknownValueException(String.format("Service id '%s' is not defined by any module.", serviceId),
                    new AvailableValues("Defined service ids", serviceIdToModule));

        return module;
    }

    @Override
    public <T> Collection<T> getUnorderedConfiguration(ServiceDef3 serviceDef, Class<T> objectType)
    {
        lock.check();

        final Collection<T> result = CollectionFactory.newList();

        // TAP5-2649. NOTICE: if someday an ordering between modules is added, this should be reverted
        // or a notice added to the documentation.
        List<Module> modules = new ArrayList<Module>(moduleToServiceDefs.keySet());
        Collections.sort(modules, new ModuleComparator());

        for (Module m : modules)
            addToUnorderedConfiguration(result, objectType, serviceDef, m);
        
        if (!isServiceConfigurationListenerServiceDef(serviceDef))
        {
            serviceConfigurationListener.onUnorderedConfiguration(serviceDef, result);
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getOrderedConfiguration(ServiceDef3 serviceDef, Class<T> objectType)
    {
        lock.check();

        String serviceId = serviceDef.getServiceId();
        Logger logger = getServiceLogger(serviceId);

        Orderer<T> orderer = new Orderer<T>(logger);
        Map<String, OrderedConfigurationOverride<T>> overrides = CollectionFactory.newCaseInsensitiveMap();

        // TAP5-2129. NOTICE: if someday an ordering between modules is added, this should be reverted
        // or a notice added to the documentation.
        List<Module> modules = new ArrayList<Module>(moduleToServiceDefs.keySet());
        Collections.sort(modules, new ModuleComparator());
        
        for (Module m : modules)
            addToOrderedConfiguration(orderer, overrides, objectType, serviceDef, m);

        // An ugly hack ... perhaps we should introduce a new builtin service so that this can be
        // accomplished in the normal way?

        if (serviceId.equals("MasterObjectProvider"))
        {
            ObjectProvider contribution = new ObjectProvider()
            {
                @Override
                public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator)
                {
                    return findServiceByMarkerAndType(objectType, annotationProvider, null);
                }
            };

            orderer.add("ServiceByMarker", (T) contribution);
        }

        for (OrderedConfigurationOverride<T> override : overrides.values())
            override.apply();

        final List<T> result = orderer.getOrdered();
        
        if (!isServiceConfigurationListenerServiceDef(serviceDef))
        {
            serviceConfigurationListener.onOrderedConfiguration(serviceDef, result);
        }
        
        return result;
    }
    
    private boolean isServiceConfigurationListenerServiceDef(ServiceDef serviceDef)
    {
        return serviceDef.getServiceId().equalsIgnoreCase(ServiceConfigurationListener.class.getSimpleName());
    }

    @Override
    public <K, V> Map<K, V> getMappedConfiguration(ServiceDef3 serviceDef, Class<K> keyType, Class<V> objectType)
    {
        lock.check();

        // When the key type is String, then a case insensitive map is used.

        Map<K, V> result = newConfigurationMap(keyType);
        Map<K, ContributionDef> keyToContribution = newConfigurationMap(keyType);
        Map<K, MappedConfigurationOverride<K, V>> overrides = newConfigurationMap(keyType);

        for (Module m : moduleToServiceDefs.keySet())
            addToMappedConfiguration(result, overrides, keyToContribution, keyType, objectType, serviceDef, m);

        for (MappedConfigurationOverride<K, V> override : overrides.values())
        {
            override.apply();
        }

        if (!isServiceConfigurationListenerServiceDef(serviceDef))
        {
            serviceConfigurationListener.onMappedConfiguration(serviceDef, result);
        }
        
        return result;
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> newConfigurationMap(Class<K> keyType)
    {
        if (keyType.equals(String.class))
        {
            Map<String, K> result = CollectionFactory.newCaseInsensitiveMap();

            return (Map<K, V>) result;
        }

        return CollectionFactory.newMap();
    }

    private <K, V> void addToMappedConfiguration(Map<K, V> map, Map<K, MappedConfigurationOverride<K, V>> overrides,
                                                 Map<K, ContributionDef> keyToContribution, Class<K> keyClass, Class<V> valueType, ServiceDef3 serviceDef,
                                                 final Module module)
    {
        String serviceId = serviceDef.getServiceId();
        Set<ContributionDef2> contributions = module.getContributorDefsForService(serviceDef);

        if (contributions.isEmpty())
            return;

        Logger logger = getServiceLogger(serviceId);

        final ServiceResources resources = new ServiceResourcesImpl(this, module, serviceDef, proxyFactory, logger);

        for (final ContributionDef def : contributions)
        {
            final MappedConfiguration<K, V> validating = new ValidatingMappedConfigurationWrapper<K, V>(valueType,
                    resources, typeCoercerProxy, map, overrides, serviceId, def, keyClass, keyToContribution);

            String description = "Invoking " + def;

            logger.debug(description);

            operationTracker.run(description, new Runnable()
            {
                @Override
                public void run()
                {
                    def.contribute(module, resources, validating);
                }
            });
        }
    }

    private <T> void addToUnorderedConfiguration(Collection<T> collection, Class<T> valueType, ServiceDef3 serviceDef,
                                                 final Module module)
    {
        String serviceId = serviceDef.getServiceId();
        Set<ContributionDef2> contributions = module.getContributorDefsForService(serviceDef);

        if (contributions.isEmpty())
            return;

        Logger logger = getServiceLogger(serviceId);

        final ServiceResources resources = new ServiceResourcesImpl(this, module, serviceDef, proxyFactory, logger);

        for (final ContributionDef def : contributions)
        {
            final Configuration<T> validating = new ValidatingConfigurationWrapper<T>(valueType, resources,
                    typeCoercerProxy, collection, serviceId);

            String description = "Invoking " + def;

            logger.debug(description);

            operationTracker.run(description, new Runnable()
            {
                @Override
                public void run()
                {
                    def.contribute(module, resources, validating);
                }
            });
        }
    }

    private <T> void addToOrderedConfiguration(Orderer<T> orderer,
                                               Map<String, OrderedConfigurationOverride<T>> overrides, Class<T> valueType, ServiceDef3 serviceDef,
                                               final Module module)
    {
        String serviceId = serviceDef.getServiceId();
        Set<ContributionDef2> contributions = module.getContributorDefsForService(serviceDef);

        if (contributions.isEmpty())
            return;

        Logger logger = getServiceLogger(serviceId);

        final ServiceResources resources = new ServiceResourcesImpl(this, module, serviceDef, proxyFactory, logger);

        for (final ContributionDef def : contributions)
        {
            final OrderedConfiguration<T> validating = new ValidatingOrderedConfigurationWrapper<T>(valueType,
                    resources, typeCoercerProxy, orderer, overrides, def);

            String description = "Invoking " + def;

            logger.debug(description);

            operationTracker.run(description, new Runnable()
            {
                @Override
                public void run()
                {
                    def.contribute(module, resources, validating);
                }
            });
        }
    }

    @Override
    public <T> T getService(Class<T> serviceInterface)
    {
        lock.check();

        return getServiceByTypeAndMarkers(serviceInterface);
    }

    @Override
    public <T> T getService(Class<T> serviceInterface, Class<? extends Annotation>... markerTypes)
    {
        lock.check();

        return getServiceByTypeAndMarkers(serviceInterface, markerTypes);
    }

    private <T> T getServiceByTypeAlone(Class<T> serviceInterface)
    {
        List<String> serviceIds = findServiceIdsForInterface(serviceInterface);

        if (serviceIds == null)
            serviceIds = Collections.emptyList();

        switch (serviceIds.size())
        {
            case 0:

                throw new RuntimeException(IOCMessages.noServiceMatchesType(serviceInterface));

            case 1:

                String serviceId = serviceIds.get(0);

                return getService(serviceId, serviceInterface);

            default:

                Collections.sort(serviceIds);

                throw new RuntimeException(IOCMessages.manyServiceMatches(serviceInterface, serviceIds));
        }
    }

    private <T> T getServiceByTypeAndMarkers(Class<T> serviceInterface, Class<? extends Annotation>... markerTypes)
    {
        if (markerTypes.length == 0)
        {
            return getServiceByTypeAlone(serviceInterface);
        }

        AnnotationProvider provider = createAnnotationProvider(markerTypes);

        Set<ServiceDef2> matches = CollectionFactory.newSet();
        List<Class> markers = CollectionFactory.newList();

        findServiceDefsMatchingMarkerAndType(serviceInterface, provider, null, markers, matches);

        return extractServiceFromMatches(serviceInterface, markers, matches);
    }

    private AnnotationProvider createAnnotationProvider(Class<? extends Annotation>... markerTypes)
    {
        final Map<Class<? extends Annotation>, Annotation> map = CollectionFactory.newMap();

        for (Class<? extends Annotation> markerType : markerTypes)
        {
            map.put(markerType, createAnnotationProxy(markerType));
        }

        return new AnnotationProvider()
        {
            @Override
            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                return annotationClass.cast(map.get(annotationClass));
            }
        };
    }

    private <A extends Annotation> Annotation createAnnotationProxy(final Class<A> annotationType)
    {
        Annotation result = cachedAnnotationProxies.get(annotationType);

        if (result == null)
        {
            // We create a JDK proxy because its pretty quick and easy.

            InvocationHandler handler = new InvocationHandler()
            {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
                {
                    if (method.getName().equals("annotationType"))
                    {
                        return annotationType;
                    }

                    return method.invoke(proxy, args);
                }
            };

            result = (Annotation) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{annotationType},
                    handler);

            cachedAnnotationProxies.put(annotationType, result);
        }

        return result;
    }

    private List<String> findServiceIdsForInterface(Class serviceInterface)
    {
        List<String> result = CollectionFactory.newList();

        for (Module module : moduleToServiceDefs.keySet())
            result.addAll(module.findServiceIdsForInterface(serviceInterface));

        for (Map.Entry<String, Object> entry : builtinServices.entrySet())
        {
            if (serviceInterface.isInstance(entry.getValue()))
                result.add(entry.getKey());
        }

        Collections.sort(result);

        return result;
    }

    @Override
    public ServiceLifecycle2 getServiceLifecycle(String scope)
    {
        lock.check();

        ServiceLifecycle result = lifecycles.get(scope);

        if (result == null)
        {
            ServiceLifecycleSource source = getService("ServiceLifecycleSource", ServiceLifecycleSource.class);

            result = source.get(scope);
        }

        if (result == null)
            throw new RuntimeException(IOCMessages.unknownScope(scope));

        return InternalUtils.toServiceLifecycle2(result);
    }

    @Override
    public List<ServiceDecorator> findDecoratorsForService(ServiceDef3 serviceDef)
    {
        lock.check();

        assert serviceDef != null;
        
        Logger logger = getServiceLogger(serviceDef.getServiceId());

        Orderer<ServiceDecorator> orderer = new Orderer<ServiceDecorator>(logger, true);

        for (Module module : moduleToServiceDefs.keySet())
        {
            Set<DecoratorDef> decoratorDefs = module.findMatchingDecoratorDefs(serviceDef);

            if (decoratorDefs.isEmpty())
                continue;

            ServiceResources resources = new ServiceResourcesImpl(this, module, serviceDef, proxyFactory, logger);

            for (DecoratorDef decoratorDef : decoratorDefs)
            {
                ServiceDecorator decorator = decoratorDef.createDecorator(module, resources);
                try
                {
                    orderer.add(decoratorDef.getDecoratorId(), decorator, decoratorDef.getConstraints());
                }
                catch (IllegalArgumentException e) {
                    throw new RuntimeException(String.format(
                            "Service %s has two different decorators methods named decorate%s in different module classes. "
                            + "You can solve this by renaming one of them and annotating it with @Match(\"%2$s\").", 
                            serviceDef.getServiceId(), decoratorDef.getDecoratorId()));
                }
            }
        }

        return orderer.getOrdered();
    }

    @Override
    public List<ServiceAdvisor> findAdvisorsForService(ServiceDef3 serviceDef)
    {
        lock.check();

        assert serviceDef != null;

        Logger logger = getServiceLogger(serviceDef.getServiceId());

        Orderer<ServiceAdvisor> orderer = new Orderer<ServiceAdvisor>(logger, true);

        for (Module module : moduleToServiceDefs.keySet())
        {
            Set<AdvisorDef> advisorDefs = module.findMatchingServiceAdvisors(serviceDef);

            if (advisorDefs.isEmpty())
                continue;

            ServiceResources resources = new ServiceResourcesImpl(this, module, serviceDef, proxyFactory, logger);

            for (AdvisorDef advisorDef : advisorDefs)
            {
                ServiceAdvisor advisor = advisorDef.createAdvisor(module, resources);

                orderer.add(advisorDef.getAdvisorId(), advisor, advisorDef.getConstraints());
            }
        }

        return orderer.getOrdered();
    }

    @Override
    public <T> T getObject(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator,
                           Module localModule)
    {
        lock.check();

        AnnotationProvider effectiveProvider = annotationProvider != null ? annotationProvider
                : new NullAnnotationProvider();

        // We do a check here for known marker/type combinations, so that you can use a marker
        // annotation
        // to inject into a contribution method that contributes to MasterObjectProvider.
        // We also force a contribution into MasterObjectProvider to accomplish the same thing.

        T result = findServiceByMarkerAndType(objectType, annotationProvider, localModule);

        if (result != null)
            return result;

        MasterObjectProvider masterProvider = getService(IOCConstants.MASTER_OBJECT_PROVIDER_SERVICE_ID,
                MasterObjectProvider.class);

        return masterProvider.provide(objectType, effectiveProvider, locator, true);
    }

    private Collection<ServiceDef2> filterByType(Class<?> objectType, Collection<ServiceDef2> serviceDefs)
    {
        Collection<ServiceDef2> result = CollectionFactory.newSet();

        for (ServiceDef2 sd : serviceDefs)
        {
            if (objectType.isAssignableFrom(sd.getServiceInterface()))
            {
                result.add(sd);
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T findServiceByMarkerAndType(Class<T> objectType, AnnotationProvider provider, Module localModule)
    {
        if (provider == null)
            return null;

        Set<ServiceDef2> matches = CollectionFactory.newSet();
        List<Class> markers = CollectionFactory.newList();

        findServiceDefsMatchingMarkerAndType(objectType, provider, localModule, markers, matches);


        // If didn't see @Local or any recognized marker annotation, then don't try to filter that
        // way. Continue on, eventually to the MasterObjectProvider service.

        if (markers.isEmpty())
        {
            return null;
        }

        return extractServiceFromMatches(objectType, markers, matches);
    }

    /**
     * Given markers and matches processed by {@link #findServiceDefsMatchingMarkerAndType(Class, org.apache.tapestry5.commons.AnnotationProvider, Module, java.util.List, java.util.Set)}, this
     * finds the singular match, or reports an error for 0 or 2+ matches.
     */
    private <T> T extractServiceFromMatches(Class<T> objectType, List<Class> markers, Set<ServiceDef2> matches)
    {
        switch (matches.size())
        {

            case 1:

                ServiceDef def = matches.iterator().next();

                return getService(def.getServiceId(), objectType);

            case 0:

                // It's no accident that the user put the marker annotation at the injection
                // point, since it matches a known marker annotation, it better be there for
                // a reason. So if we don't get a match, we have to assume the user expected
                // one, and that is an error.

                // This doesn't help when the user places an annotation they *think* is a marker
                // but isn't really a marker (because no service is marked by the annotation).

                throw new RuntimeException(IOCMessages.noServicesMatchMarker(objectType, markers));

            default:
                throw new RuntimeException(IOCMessages.manyServicesMatchMarker(objectType, markers, matches));
        }
    }

    private <T> void findServiceDefsMatchingMarkerAndType(Class<T> objectType, AnnotationProvider provider, Module localModule, List<Class> markers,
                                                          Set<ServiceDef2> matches)
    {
        assert provider != null;

        boolean localOnly = localModule != null && provider.getAnnotation(Local.class) != null;

        matches.addAll(filterByType(objectType, localOnly ? moduleToServiceDefs.get(localModule) : allServiceDefs));

        if (localOnly)
        {
            markers.add(Local.class);
        }

        for (Entry<Class, List<ServiceDef2>> entry : markerToServiceDef.entrySet())
        {
            Class marker = entry.getKey();
            if (provider.getAnnotation(marker) == null)
            {
                continue;
            }

            markers.add(marker);

            matches.retainAll(entry.getValue());

            if (matches.isEmpty())
            {
                return;
            }
        }
    }

    @Override
    public <T> T getObject(Class<T> objectType, AnnotationProvider annotationProvider)
    {
        return getObject(objectType, annotationProvider, this, null);
    }

    @Override
    public void addRegistryShutdownListener(RegistryShutdownListener listener)
    {
        lock.check();

        registryShutdownHub.addRegistryShutdownListener(listener);
    }

    @Override
    public void addRegistryShutdownListener(Runnable listener)
    {
        lock.check();

        registryShutdownHub.addRegistryShutdownListener(listener);
    }

    @Override
    public void addRegistryWillShutdownListener(Runnable listener)
    {
        lock.check();

        registryShutdownHub.addRegistryWillShutdownListener(listener);
    }

    @Override
    public String expandSymbols(String input)
    {
        lock.check();

        // Again, a bit of work to avoid instantiating the SymbolSource until absolutely necessary.

        if (!InternalUtils.containsSymbols(input))
            return input;

        return getSymbolSource().expandSymbols(input);
    }

    /**
     * Defers obtaining the symbol source until actually needed.
     */
    private SymbolSource getSymbolSource()
    {
        if (symbolSource == null)
            symbolSource = getService(SYMBOL_SOURCE_SERVICE_ID, SymbolSource.class);

        return symbolSource;
    }

    @Override
    public <T> T autobuild(String description, final Class<T> clazz)
    {
        return invoke(description, new Invokable<T>()
        {
            @Override
            public T invoke()
            {
                return autobuild(clazz);
            }
        });
    }

    @Override
    public <T> T autobuild(final Class<T> clazz)
    {
        assert clazz != null;
        final Constructor constructor = InternalUtils.findAutobuildConstructor(clazz);

        if (constructor == null)
        {
            throw new RuntimeException(IOCMessages.noAutobuildConstructor(clazz));
        }

        Map<Class, Object> resourcesMap = CollectionFactory.newMap();
        resourcesMap.put(OperationTracker.class, RegistryImpl.this);

        InjectionResources resources = new MapInjectionResources(resourcesMap);

        ObjectCreator<T> plan = InternalUtils.createConstructorConstructionPlan(this, this, resources, null, "Invoking " + proxyFactory.getConstructorLocation(constructor).toString(), constructor);

        return plan.createObject();
    }

    @Override
    public <T> T proxy(Class<T> interfaceClass, Class<? extends T> implementationClass)
    {
        return proxy(interfaceClass, implementationClass, this);
    }

    @Override
    public <T> T proxy(Class<T> interfaceClass, Class<? extends T> implementationClass, ObjectLocator locator)
    {
        assert interfaceClass != null;
        assert implementationClass != null;

        if (InternalUtils.SERVICE_CLASS_RELOADING_ENABLED && InternalUtils.isLocalFile(implementationClass))
            return createReloadingProxy(interfaceClass, implementationClass, locator);

        return createNonReloadingProxy(interfaceClass, implementationClass, locator);
    }

    private <T> T createNonReloadingProxy(Class<T> interfaceClass, final Class<? extends T> implementationClass,
                                          final ObjectLocator locator)
    {
        final ObjectCreator<T> autobuildCreator = new ObjectCreator<T>()
        {
            @Override
            public T createObject()
            {
                return locator.autobuild(implementationClass);
            }
        };

        ObjectCreator<T> justInTime = new ObjectCreator<T>()
        {
            private T delegate;

            @Override
            public synchronized T createObject()
            {
                if (delegate == null)
                    delegate = autobuildCreator.createObject();

                return delegate;
            }
        };

        return proxyFactory.createProxy(interfaceClass, justInTime,
                String.format("<Autobuild proxy %s(%s)>", implementationClass.getName(), interfaceClass.getName()));
    }

    private <T> T createReloadingProxy(Class<T> interfaceClass, final Class<? extends T> implementationClass,
                                       ObjectLocator locator)
    {
        ReloadableObjectCreator creator = new ReloadableObjectCreator(proxyFactory, implementationClass.getClassLoader(),
                implementationClass.getName(), loggerSource.getLogger(implementationClass), this, locator);

        getService(UpdateListenerHub.class).addUpdateListener(creator);

        return proxyFactory.createProxy(interfaceClass, implementationClass, (ObjectCreator<T>) creator,
                String.format("<Autoreload proxy %s(%s)>", implementationClass.getName(), interfaceClass.getName()));
    }

    @Override
    public Object provideServiceProxy(String serviceId)
    {
        return getService(serviceId, Object.class);
    }

    @Override
    public void run(String description, Runnable operation)
    {
        operationTracker.run(description, operation);
    }

    @Override
    public <T> T invoke(String description, Invokable<T> operation)
    {
        return operationTracker.invoke(description, operation);
    }

    @Override
    public <T> T perform(String description, IOOperation<T> operation) throws IOException
    {
        return operationTracker.perform(description, operation);
    }

    @Override
    public Set<Class> getMarkerAnnotations()
    {
        return markerToServiceDef.keySet();
    }
    
    final private static class ModuleComparator implements Comparator<Module> {
        @Override
        public int compare(Module m1, Module m2)
        {
            return m1.getLoggerName().compareTo(m2.getLoggerName());
        }
    }
    
    final static private class DelegatingServiceConfigurationListener implements ServiceConfigurationListener {
        
        final private Logger logger;
        
        private List<ServiceConfigurationListener> delegates;
        private Map<ServiceDef, Map> mapped = CollectionFactory.newMap();
        private Map<ServiceDef, Collection> unordered = CollectionFactory.newMap();
        private Map<ServiceDef, List> ordered = CollectionFactory.newMap();
        
        public DelegatingServiceConfigurationListener(Logger logger)
        {
            this.logger = logger;
        }

        public void setDelegates(List<ServiceConfigurationListener> delegates)
        {
            
            this.delegates = delegates;
            
            for (Entry<ServiceDef, Map> entry : mapped.entrySet())
            {
                for (ServiceConfigurationListener delegate : delegates)
                {
                    delegate.onMappedConfiguration(entry.getKey(), Collections.unmodifiableMap(entry.getValue()));
                }
            }

            for (Entry<ServiceDef, Collection> entry : unordered.entrySet())
            {
                for (ServiceConfigurationListener delegate : delegates)
                {
                    delegate.onUnorderedConfiguration(entry.getKey(), Collections.unmodifiableCollection(entry.getValue()));
                }
            }

            for (Entry<ServiceDef, List> entry : ordered.entrySet())
            {
                for (ServiceConfigurationListener delegate : delegates)
                {
                    delegate.onOrderedConfiguration(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
                }
            }
            
            mapped.clear();
            mapped = null;
            unordered.clear();
            unordered = null;
            ordered.clear();
            ordered = null;

        }
        
        @Override
        public void onOrderedConfiguration(ServiceDef serviceDef, List configuration)
        {
            log("ordered", serviceDef, configuration);
            if (delegates == null)
            {
                ordered.put(serviceDef, configuration);
            }
            else
            {
                for (ServiceConfigurationListener delegate : delegates)
                {
                    delegate.onOrderedConfiguration(serviceDef, Collections.unmodifiableList(configuration));
                }
            }
        }

        @Override
        public void onUnorderedConfiguration(ServiceDef serviceDef, Collection configuration)
        {
            log("unordered", serviceDef, configuration);
            if (delegates == null)
            {
                unordered.put(serviceDef, configuration);
            }
            else
            {
                for (ServiceConfigurationListener delegate : delegates)
                {
                    delegate.onUnorderedConfiguration(serviceDef, Collections.unmodifiableCollection(configuration));
                }
            }
        }

        @Override
        public void onMappedConfiguration(ServiceDef serviceDef, Map configuration)
        {
            log("mapped", serviceDef, configuration);
            if (delegates == null)
            {
                mapped.put(serviceDef, configuration);
            }
            else
            {
                for (ServiceConfigurationListener delegate : delegates)
                {
                    delegate.onMappedConfiguration(serviceDef, Collections.unmodifiableMap(configuration));
                }
            }
            
        }
        
        private void log(String type, ServiceDef serviceDef, Object configuration)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Service {} {} configuration: {}", 
                        serviceDef.getServiceId(), type, configuration.toString());
            }
        }
        
    }
    
}
