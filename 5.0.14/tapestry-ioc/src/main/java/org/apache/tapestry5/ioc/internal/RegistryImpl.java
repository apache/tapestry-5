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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.*;
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.DecoratorDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.internal.services.PerthreadManagerImpl;
import org.apache.tapestry5.ioc.internal.services.RegistryShutdownHubImpl;
import org.apache.tapestry5.ioc.internal.util.*;
import org.apache.tapestry5.ioc.services.*;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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

    static
    {
        BUILTIN.add(Builtin.class);
    }

    /**
     * Used to obtain the {@link org.apache.tapestry5.ioc.services.ClassFactory} service, which is crucial when creating
     * runtime classes for proxies and the like.
     */
    static final String CLASS_FACTORY_SERVICE_ID = "ClassFactory";

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

    private final Map<String, ServiceLifecycle> lifecycles = CollectionFactory.newCaseInsensitiveMap();

    private final PerthreadManager perthreadManager;

    private final ClassFactory classFactory;

    private final ServiceActivityTracker tracker;

    private SymbolSource symbolSource;

    private final List<Module> modules = CollectionFactory.newList();

    /**
     * From marker type to a list of marked service instances.
     */
    private final Map<Class, List<ServiceDef>> markerToServiceDef = CollectionFactory.newMap();


    public static final class OrderedConfigurationToOrdererAdaptor<T> implements OrderedConfiguration<T>
    {
        private final Orderer<T> orderer;

        public OrderedConfigurationToOrdererAdaptor(Orderer<T> orderer)
        {
            this.orderer = orderer;
        }

        public void add(String id, T object, String... constraints)
        {
            orderer.add(id, object, constraints);
        }
    }

    /**
     * Constructs the registry from a set of module definitions and other resources.
     *
     * @param moduleDefs   defines the modules (and builders, decorators, etc., within)
     * @param classFactory TODO
     * @param loggerSource used to obtain Logger instances
     */
    public RegistryImpl(Collection<ModuleDef> moduleDefs, ClassFactory classFactory, LoggerSource loggerSource)
    {
        this.loggerSource = loggerSource;

        final ServiceActivityTrackerImpl scoreboardAndTracker = new ServiceActivityTrackerImpl();

        tracker = scoreboardAndTracker;

        addBuiltin(SERVICE_ACTIVITY_SCOREBOARD_SERVICE_ID, ServiceActivityScoreboard.class, scoreboardAndTracker);

        addBuiltin(LOGGER_SOURCE_SERVICE_ID, LoggerSource.class, this.loggerSource);

        this.classFactory = classFactory;

        addBuiltin(CLASS_FACTORY_SERVICE_ID, ClassFactory.class, this.classFactory);

        Logger logger = loggerForBuiltinService(PERTHREAD_MANAGER_SERVICE_ID);

        perthreadManager = new PerthreadManagerImpl(logger);

        addBuiltin(PERTHREAD_MANAGER_SERVICE_ID, PerthreadManager.class, perthreadManager);

        logger = loggerForBuiltinService(REGISTRY_SHUTDOWN_HUB_SERVICE_ID);

        registryShutdownHub = new RegistryShutdownHubImpl(logger);

        addBuiltin(REGISTRY_SHUTDOWN_HUB_SERVICE_ID, RegistryShutdownHub.class, registryShutdownHub);

        lifecycles.put("singleton", new SingletonServiceLifecycle());

        registryShutdownHub.addRegistryShutdownListener(new RegistryShutdownListener()
        {
            public void registryDidShutdown()
            {
                scoreboardAndTracker.shutdown();
            }
        });

        for (ModuleDef def : moduleDefs)
        {
            logger = this.loggerSource.getLogger(def.getLoggerName());

            Module module = new ModuleImpl(this, tracker, def, classFactory, logger);

            modules.add(module);

            for (String serviceId : def.getServiceIds())
            {
                ServiceDef serviceDef = module.getServiceDef(serviceId);

                Module existing = serviceIdToModule.get(serviceId);

                if (existing != null) throw new RuntimeException(IOCMessages.serviceIdConflict(serviceId, existing
                        .getServiceDef(serviceId), serviceDef));

                serviceIdToModule.put(serviceId, module);

                // The service is defined but will not have gone further than that.
                tracker.define(serviceDef, Status.DEFINED);

                for (Class marker : serviceDef.getMarkers())
                    InternalUtils.addToMapList(markerToServiceDef, marker, serviceDef);

            }
        }

        scoreboardAndTracker.startup();

        SerializationSupport.setProvider(this);
    }

    /**
     * It's not unreasonable for an eagerly-loaded service to decide to start a thread, at which point we raise issues
     * about improper publishing of the Registry instance from the RegistryImpl constructor. Moving eager loading of
     * services out to its own method should ensure thread safety.
     */
    public void performRegistryStartup()
    {
        eagerLoadLock.lock();

        List<EagerLoadServiceProxy> proxies = CollectionFactory.newList();

        for (Module m : modules)
            m.collectEagerLoadServices(proxies);

        // TAPESTRY-2267: Gather up all the proxies before instantiating any of them.

        for (EagerLoadServiceProxy proxy : proxies)
            proxy.eagerLoadService();

        getService("RegistryStartup", Runnable.class).run();

        cleanupThread();
    }

    public Logger getServiceLogger(String serviceId)
    {
        Module module = serviceIdToModule.get(serviceId);

        assert module != null;

        return loggerSource.getLogger(module.getLoggerName() + "." + serviceId);
    }

    private Logger loggerForBuiltinService(String serviceId)
    {
        return loggerSource.getLogger(TapestryIOCModule.class + "." + serviceId);
    }

    private <T> void addBuiltin(final String serviceId, final Class<T> serviceInterface, T service)
    {
        builtinTypes.put(serviceId, serviceInterface);
        builtinServices.put(serviceId, service);

        // Make sure each of the builtin services is also available via the Builtin annotation
        // marker.

        ServiceDef serviceDef = new ServiceDef()
        {
            public ObjectCreator createServiceCreator(ServiceBuilderResources resources)
            {
                return null;
            }

            public Set<Class> getMarkers()
            {
                return BUILTIN;
            }

            public String getServiceId()
            {
                return serviceId;
            }

            public Class getServiceInterface()
            {
                return serviceInterface;
            }

            public String getServiceScope()
            {
                return IOCConstants.DEFAULT_SCOPE;
            }

            public boolean isEagerLoad()
            {
                return false;
            }
        };

        for (Class marker : serviceDef.getMarkers())
            InternalUtils.addToMapList(markerToServiceDef, marker, serviceDef);

        tracker.define(serviceDef, Status.BUILTIN);
    }

    public synchronized void shutdown()
    {
        lock.lock();

        registryShutdownHub.fireRegistryDidShutdown();

        SerializationSupport.clearProvider(this);
    }

    public <T> T getService(String serviceId, Class<T> serviceInterface)
    {
        lock.check();

        T result = checkForBuiltinService(serviceId, serviceInterface);
        if (result != null) return result;

        // Checking serviceId and serviceInterface is overkill; they have been checked and rechecked
        // all the way to here.

        Module containingModule = locateModuleForService(serviceId);

        return containingModule.getService(serviceId, serviceInterface);
    }

    private <T> T checkForBuiltinService(String serviceId, Class<T> serviceInterface)
    {
        Object service = builtinServices.get(serviceId);

        if (service == null) return null;

        try
        {
            return serviceInterface.cast(service);
        }
        catch (ClassCastException ex)
        {
            throw new RuntimeException(IOCMessages.serviceWrongInterface(serviceId, builtinTypes
                    .get(serviceId), serviceInterface));
        }
    }

    public void cleanupThread()
    {
        lock.check();

        perthreadManager.cleanup();
    }

    private Module locateModuleForService(String serviceId)
    {
        Module module = serviceIdToModule.get(serviceId);

        if (module == null) throw new RuntimeException(IOCMessages.noSuchService(serviceId, serviceIdToModule
                .keySet()));

        return module;
    }

    public <T> Collection<T> getUnorderedConfiguration(ServiceDef serviceDef, Class<T> objectType)
    {
        lock.check();

        final Collection<T> result = CollectionFactory.newList();

        Configuration<T> configuration = new Configuration<T>()
        {
            public void add(T object)
            {
                result.add(object);
            }
        };

        Collection<Module> modules = this.modules;

        for (Module m : modules)
            addToUnorderedConfiguration(configuration, objectType, serviceDef, m);

        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getOrderedConfiguration(ServiceDef serviceDef, Class<T> objectType)
    {
        lock.check();

        String serviceId = serviceDef.getServiceId();
        Logger logger = getServiceLogger(serviceId);

        final Orderer<T> orderer = new Orderer<T>(logger);

        OrderedConfiguration<T> configuration = new OrderedConfigurationToOrdererAdaptor<T>(orderer);

        Collection<Module> modules = this.modules;

        for (Module m : modules)
            addToOrderedConfiguration(configuration, objectType, serviceDef, m);

        // An ugly hack ... perhaps we should introduce a new builtin service so that this can be
        // accomplished in the normal way?

        if (serviceId.equals("MasterObjectProvider"))
        {
            ObjectProvider contribution = new ObjectProvider()
            {
                public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator)
                {
                    return findServiceByMarkerAndType(objectType, annotationProvider);
                }
            };

            configuration.add("ServiceByMarker", (T) contribution);
        }

        return orderer.getOrdered();
    }

    public <K, V> Map<K, V> getMappedConfiguration(ServiceDef serviceDef, Class<K> keyType, Class<V> objectType)
    {
        lock.check();

        // When the key type is String, then a case insensitive map is used for both cases.

        final Map<K, V> result = newConfigurationMap(keyType);
        Map<K, ContributionDef> keyToContribution = newConfigurationMap(keyType);

        MappedConfiguration<K, V> configuration = new MappedConfiguration<K, V>()
        {
            public void add(K key, V value)
            {
                result.put(key, value);
            }
        };

        Collection<Module> modules = this.modules;

        for (Module m : modules)
            addToMappedConfiguration(configuration, keyToContribution, keyType, objectType, serviceDef, m);

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

    private <K, V> void addToMappedConfiguration(MappedConfiguration<K, V> configuration,
                                                 Map<K, ContributionDef> keyToContribution, Class<K> keyClass,
                                                 Class<V> valueType, ServiceDef serviceDef, Module module)
    {
        String serviceId = serviceDef.getServiceId();
        Set<ContributionDef> contributions = module.getContributorDefsForService(serviceId);

        if (contributions.isEmpty()) return;

        Logger logger = getServiceLogger(serviceId);

        boolean debug = logger.isDebugEnabled();

        ObjectLocator locator = new ServiceResourcesImpl(this, module, serviceDef, classFactory, logger);

        for (ContributionDef def : contributions)
        {
            MappedConfiguration<K, V> validating = new ValidatingMappedConfigurationWrapper<K, V>(serviceId, def,
                                                                                                  logger, keyClass,
                                                                                                  valueType,
                                                                                                  keyToContribution,
                                                                                                  configuration);

            if (debug) logger.debug(IOCMessages.invokingMethod(def));

            def.contribute(module, locator, validating);
        }

    }

    private <T> void addToUnorderedConfiguration(Configuration<T> configuration, Class<T> valueType,
                                                 ServiceDef serviceDef, Module module)
    {
        String serviceId = serviceDef.getServiceId();
        Set<ContributionDef> contributions = module.getContributorDefsForService(serviceId);

        if (contributions.isEmpty()) return;

        Logger logger = getServiceLogger(serviceId);

        boolean debug = logger.isDebugEnabled();

        ObjectLocator locator = new ServiceResourcesImpl(this, module, serviceDef, classFactory, logger);

        for (ContributionDef def : contributions)
        {
            Configuration<T> validating = new ValidatingConfigurationWrapper<T>(serviceId, logger, valueType, def,
                                                                                configuration);

            if (debug) logger.debug(IOCMessages.invokingMethod(def));

            def.contribute(module, locator, validating);
        }
    }

    private <T> void addToOrderedConfiguration(OrderedConfiguration<T> configuration, Class<T> valueType,
                                               ServiceDef serviceDef, Module module)
    {
        String serviceId = serviceDef.getServiceId();
        Set<ContributionDef> contributions = module.getContributorDefsForService(serviceId);

        if (contributions.isEmpty()) return;

        Logger logger = getServiceLogger(serviceId);
        boolean debug = logger.isDebugEnabled();

        ObjectLocator locator = new ServiceResourcesImpl(this, module, serviceDef, classFactory, logger);

        for (ContributionDef def : contributions)
        {
            OrderedConfiguration<T> validating = new ValidatingOrderedConfigurationWrapper<T>(serviceId, def, logger,
                                                                                              valueType, configuration);

            if (debug) logger.debug(IOCMessages.invokingMethod(def));

            def.contribute(module, locator, validating);
        }
    }

    public <T> T getService(Class<T> serviceInterface)
    {
        lock.check();

        List<String> serviceIds = findServiceIdsForInterface(serviceInterface);

        if (serviceIds == null) serviceIds = Collections.emptyList();

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

    private List<String> findServiceIdsForInterface(Class serviceInterface)
    {
        List<String> result = CollectionFactory.newList();

        for (Module module : modules)
            result.addAll(module.findServiceIdsForInterface(serviceInterface));

        for (Map.Entry<String, Object> entry : builtinServices.entrySet())
        {
            if (serviceInterface.isInstance(entry.getValue())) result.add(entry.getKey());
        }

        Collections.sort(result);

        return result;
    }

    public ServiceLifecycle getServiceLifecycle(String scope)
    {
        lock.check();

        ServiceLifecycle result = lifecycles.get(scope);

        if (result == null)
        {
            ServiceLifecycleSource source = getService("ServiceLifecycleSource", ServiceLifecycleSource.class);
            result = source.get(scope);
        }

        if (result == null) throw new RuntimeException(IOCMessages.unknownScope(scope));

        return result;
    }

    public List<ServiceDecorator> findDecoratorsForService(ServiceDef serviceDef)
    {
        lock.check();

        assert serviceDef != null;

        Logger logger = getServiceLogger(serviceDef.getServiceId());

        Orderer<ServiceDecorator> orderer = new Orderer<ServiceDecorator>(logger);

        for (Module module : modules)
        {
            Set<DecoratorDef> decorators = module.findMatchingDecoratorDefs(serviceDef);

            if (decorators.isEmpty()) continue;

            ServiceResources resources = new ServiceResourcesImpl(this, module, serviceDef, classFactory, logger);

            for (DecoratorDef dd : decorators)
            {
                ServiceDecorator sd = dd.createDecorator(module, resources);

                orderer.add(dd.getDecoratorId(), sd, dd.getConstraints());
            }
        }

        return orderer.getOrdered();
    }

    public ClassFab newClass(Class serviceInterface)
    {
        lock.check();

        return classFactory.newClass(serviceInterface);
    }

    private <T> T getObject(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator)
    {
        lock.check();

        AnnotationProvider effectiveProvider = annotationProvider != null ? annotationProvider : new NullAnnotationProvider();

        // We do a check here for known marker/type combinations, so that you can use a marker
        // annotation
        // to inject into a contribution method that contributes to MasterObjectProvider.
        // We also force a contribution into MasterObjectProvider to accomplish the same thing.

        T result = findServiceByMarkerAndType(objectType, annotationProvider);

        if (result != null) return result;

        MasterObjectProvider masterProvider = getService(IOCConstants.MASTER_OBJECT_PROVIDER_SERVICE_ID,
                                                         MasterObjectProvider.class);

        return masterProvider.provide(objectType, effectiveProvider, locator, true);
    }

    @SuppressWarnings("unchecked")
    private <T> T findServiceByMarkerAndType(Class<T> objectType, AnnotationProvider provider)
    {
        if (provider == null) return null;

        for (Class marker : markerToServiceDef.keySet())
        {
            if (provider.getAnnotation(marker) == null) continue;

            List<ServiceDef> matches = CollectionFactory.newList();

            for (ServiceDef def : markerToServiceDef.get(marker))
            {
                if (objectType.isAssignableFrom(def.getServiceInterface())) matches.add(def);
            }

            switch (matches.size())
            {

                case 1:

                    ServiceDef def = matches.get(0);

                    return getService(def.getServiceId(), objectType);

                case 0:

                    // It's no accident that the user put the marker annotation at the injection
                    // point, since it matches a known marker annotation, it better be there for
                    // a reason. So if we don't get a match, we have to assume the user expected
                    // one, and that is an error.

                    // This doesn't help when the user places an annotation they *think* is a marker
                    // but isn't really a marker (because no service is marked by the annotation).

                    throw new RuntimeException(IOCMessages
                            .noServicesMatchMarker(objectType, marker));

                default:
                    throw new RuntimeException(IOCMessages.manyServicesMatchMarker(objectType, marker, matches));
            }

        }

        return null;
    }

    public <T> T getObject(Class<T> objectType, AnnotationProvider annotationProvider)
    {
        lock.check();

        return getObject(objectType, annotationProvider, this);
    }

    public void addRegistryShutdownListener(RegistryShutdownListener listener)
    {
        lock.check();

        registryShutdownHub.addRegistryShutdownListener(listener);
    }

    public String expandSymbols(String input)
    {
        lock.check();

        // Again, a bit of work to avoid instantiating the SymbolSource until absolutely necessary.

        if (!InternalUtils.containsSymbols(input)) return input;

        return getSymbolSource().expandSymbols(input);
    }

    /**
     * Defers obtaining the symbol source until actually needed.
     */
    private synchronized SymbolSource getSymbolSource()
    {
        if (symbolSource == null) symbolSource = getService(SYMBOL_SOURCE_SERVICE_ID, SymbolSource.class);

        return symbolSource;
    }

    public <T> T autobuild(Class<T> clazz)
    {
        Defense.notNull(clazz, "clazz");

        Constructor constructor = InternalUtils.findAutobuildConstructor(clazz);

        if (constructor == null) throw new RuntimeException(IOCMessages.noAutobuildConstructor(clazz));

        Throwable failure;
        // An empty map, because when performing autobuilding outside the context of building a
        // service, we don't have defaults for Log, service id, etc.

        Map<Class, Object> empty = Collections.emptyMap();

        try
        {
            InternalUtils.validateConstructorForAutobuild(constructor);

            Object[] parameters = InternalUtils.calculateParametersForConstructor(constructor, this, empty);

            return clazz.cast(constructor.newInstance(parameters));
        }
        catch (InvocationTargetException ite)
        {
            failure = ite.getTargetException();
        }
        catch (Exception ex)
        {
            failure = ex;
        }

        String description = classFactory.getConstructorLocation(constructor).toString();

        throw new RuntimeException(IOCMessages.autobuildConstructorError(description, failure), failure);
    }

    public <T> T proxy(Class<T> interfaceClass, final Class<? extends T> implementationClass)
    {
        Defense.notNull(interfaceClass, "interfaceClass");
        Defense.notNull(implementationClass, "implementationClass");

        // TODO: Check really an interface
        // TODO: Check impl class extends interfaceClass and is concrete

        final ObjectCreator autobuildCreator = new ObjectCreator()
        {
            public Object createObject()
            {
                return autobuild(implementationClass);
            }
        };

        ObjectCreator justInTime = new ObjectCreator()
        {
            private Object delegate;

            public synchronized Object createObject()
            {
                if (delegate == null) delegate = autobuildCreator.createObject();

                return delegate;
            }
        };

        ClassFab cf = classFactory.newClass(interfaceClass);

        String description = String.format("<Autobuild proxy %s(%s)>", implementationClass
                .getName(), interfaceClass.getName());

        return ClassFabUtils.createObjectCreatorProxy(cf, interfaceClass, justInTime, description);
    }

    public Object provideServiceProxy(String serviceId)
    {
        return getService(serviceId, Object.class);
    }
}
