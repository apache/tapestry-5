// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.Configuration;
import org.apache.tapestry.ioc.IOCConstants;
import org.apache.tapestry.ioc.IOCUtilities;
import org.apache.tapestry.ioc.LogSource;
import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.OrderedConfiguration;
import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.RegistryBuilder;
import org.apache.tapestry.ioc.ServiceDecorator;
import org.apache.tapestry.ioc.ServiceLifecycle;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.ioc.ServiceResources;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.apache.tapestry.ioc.def.DecoratorDef;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.internal.services.ClassFactoryImpl;
import org.apache.tapestry.ioc.internal.services.RegistryShutdownHubImpl;
import org.apache.tapestry.ioc.internal.services.ThreadCleanupHubImpl;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.internal.util.OneShotLock;
import org.apache.tapestry.ioc.internal.util.Orderer;
import org.apache.tapestry.ioc.services.ClassFab;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.RegistryShutdownHub;
import org.apache.tapestry.ioc.services.RegistryShutdownListener;
import org.apache.tapestry.ioc.services.ServiceLifecycleSource;
import org.apache.tapestry.ioc.services.SymbolSource;
import org.apache.tapestry.ioc.services.ThreadCleanupHub;

public class RegistryImpl implements Registry, InternalRegistry
{
    private static final String SYMBOL_SOURCE_SERVICE_ID = "tapestry.ioc.SymbolSource";

    private static final String REGISTRY_SHUTDOWN_HUB_SERVICE_ID = "tapestry.ioc.RegistryShutdownHub";

    static final String THREAD_CLEANUP_HUB_SERVICE_ID = "tapestry.ioc.ThreadCleanupHub";

    /**
     * Used to obtain the {@link org.apache.tapestry.ioc.services.ClassFactory} service, which is
     * crucial when creating runtime classes for proxies and the like.
     */
    static final String CLASS_FACTORY_SERVICE_ID = "tapestry.ioc.ClassFactory";

    static final String LOG_SOURCE_SERVICE_ID = "tapestry.ioc.LogSource";

    private final OneShotLock _lock = new OneShotLock();

    private final Map<String, Object> _builtinServices = newMap();

    private final Map<String, Class> _builtinTypes = newMap();

    private final RegistryShutdownHubImpl _registryShutdownHub;

    private final LogSource _logSource;

    /** Keyed on module id. */
    private final Map<String, Module> _modules = newMap();

    private final Map<String, ServiceLifecycle> _lifecycles = newMap();

    /**
     * Service implementation overrides, keyed on service id. Service implementations are most
     * useful when perfroming integration tests on services. As one service can bring in another, we
     * have to stop at a certain "bounary" services by provide stub/ mock objects as their
     * implementations.
     */
    private Map<String, Object> _serviceOverrides;

    private final ThreadCleanupHubImpl _cleanupHub;

    private final ClassFactory _classFactory;

    private SymbolSource _symbolSource;

    public static final class OrderedConfigurationToOrdererAdaptor<T> implements
            OrderedConfiguration<T>
    {
        private final Orderer<T> _orderer;

        public OrderedConfigurationToOrdererAdaptor(Orderer<T> orderer)
        {
            _orderer = orderer;
        }

        public void add(String id, T object, String... constraints)
        {
            _orderer.add(id, object, constraints);
        }
    }

    /**
     * Constructs the registry from a set of module definitions and other resources.
     * 
     * @param moduleDefs
     *            defines the modules (and builders, decorators, etc., within)
     * @param contextClassLoader
     *            the class loader used to load classes
     * @param logSource
     *            used to obtain Log instances
     * @param serviceOverrides
     *            overrides for service implementation (used in testing, see
     *            {@link RegistryBuilder#addServiceOverride(String, Object)})
     */
    public RegistryImpl(Collection<ModuleDef> moduleDefs, ClassLoader contextClassLoader,
            LogSource logSource, Map<String, Object> serviceOverrides)
    {
        _logSource = logSource;

        _serviceOverrides = serviceOverrides;

        for (ModuleDef def : moduleDefs)
        {
            Log log = _logSource.getLog(def.getModuleId());

            Module module = new ModuleImpl(this, def, log);

            _modules.put(def.getModuleId(), module);
        }

        addBuiltin(LOG_SOURCE_SERVICE_ID, LogSource.class, _logSource);

        Log log = _logSource.getLog(RegistryImpl.CLASS_FACTORY_SERVICE_ID);

        _classFactory = new ClassFactoryImpl(contextClassLoader, log);

        addBuiltin(RegistryImpl.CLASS_FACTORY_SERVICE_ID, ClassFactory.class, _classFactory);

        log = _logSource.getLog(THREAD_CLEANUP_HUB_SERVICE_ID);

        _cleanupHub = new ThreadCleanupHubImpl(log);

        addBuiltin(THREAD_CLEANUP_HUB_SERVICE_ID, ThreadCleanupHub.class, _cleanupHub);

        log = _logSource.getLog(REGISTRY_SHUTDOWN_HUB_SERVICE_ID);

        _registryShutdownHub = new RegistryShutdownHubImpl(log);

        addBuiltin(
                REGISTRY_SHUTDOWN_HUB_SERVICE_ID,
                RegistryShutdownHub.class,
                _registryShutdownHub);

        _lifecycles.put("singleton", new SingletonServiceLifecycle());

        // Ask all modules to eager-load any services marked with @EagerLoad

        for (Module m : _modules.values())
            m.eagerLoadServices();
    }

    private <T> void addBuiltin(String serviceId, Class<T> serviceInterface, T service)
    {
        _builtinTypes.put(serviceId, serviceInterface);
        _builtinServices.put(serviceId, service);
    }

    public synchronized void shutdown()
    {
        _lock.lock();

        _registryShutdownHub.fireRegistryDidShutdown();
    }

    /** Internal access, usualy from another module. */
    public <T> T getService(String serviceId, Class<T> serviceInterface, Module module)
    {
        _lock.check();

        T result = checkForBuiltinService(serviceId, serviceInterface);
        if (result != null) return result;

        result = checkForServiceOverrides(serviceId, serviceInterface);
        if (result != null) return result;

        // Checking serviceId and serviceInterface is overkill; they have been checked and rechecked
        // all the way to here.

        Module containingModule = locateModuleForService(serviceId);

        return containingModule.getService(serviceId, serviceInterface, module);
    }

    private <T> T checkForBuiltinService(String serviceId, Class<T> serviceInterface)
    {
        Object service = _builtinServices.get(serviceId);

        if (service == null) return null;

        try
        {
            return serviceInterface.cast(service);
        }
        catch (ClassCastException ex)
        {
            throw new RuntimeException(IOCMessages.serviceWrongInterface(serviceId, _builtinTypes
                    .get(serviceId), serviceInterface));
        }
    }

    private <T> T checkForServiceOverrides(String serviceId, Class<T> serviceInterface)
    {
        Object service = _serviceOverrides.get(serviceId);

        if (service == null) return null;

        try
        {
            return serviceInterface.cast(service);
        }
        catch (ClassCastException ex)
        {
            throw new RuntimeException(IOCMessages.serviceWrongInterface(serviceId, service
                    .getClass(), serviceInterface));
        }
    }

    /**
     * Access for a service from the outside world (limited to publically visible services).
     */
    public <T> T getService(String serviceId, Class<T> serviceInterface)
    {
        _lock.check();

        return getService(expandSymbols(serviceId), serviceInterface, null);
    }

    public void cleanupThread()
    {
        _lock.check();

        _cleanupHub.cleanup();
    }

    private Module locateModuleForService(String serviceId)
    {
        String moduleId = IOCUtilities.extractModuleId(serviceId);

        Module module = _modules.get(moduleId);

        if (module == null) throw new RuntimeException(IOCMessages.noSuchModule(moduleId));

        return module;
    }

    public <T> Collection<T> getUnorderedConfiguration(ServiceDef serviceDef, Class<T> objectType)
    {
        _lock.check();

        final Collection<T> result = newList();

        Configuration<T> configuration = new Configuration<T>()
        {
            public void add(T object)
            {
                result.add(object);
            }
        };

        Collection<Module> modules = modulesThatContributeToService(serviceDef);

        for (Module m : modules)
            addToUnorderedConfiguration(configuration, objectType, serviceDef, m);

        return result;
    }

    public <T> List<T> getOrderedConfiguration(ServiceDef serviceDef, Class<T> objectType)
    {
        _lock.check();

        Log log = getLog(serviceDef.getServiceId());

        final Orderer<T> orderer = new Orderer<T>(log);

        OrderedConfiguration<T> configuration = new OrderedConfigurationToOrdererAdaptor<T>(orderer);

        Collection<Module> modules = modulesThatContributeToService(serviceDef);

        for (Module m : modules)
            addToOrderedConfiguration(configuration, objectType, serviceDef, m);

        return orderer.getOrdered();
    }

    public <K, V> Map<K, V> getMappedConfiguration(ServiceDef serviceDef, Class<K> keyType,
            Class<V> objectType)
    {
        _lock.check();

        final Map<K, V> result = newMap();

        MappedConfiguration<K, V> configuration = new MappedConfiguration<K, V>()
        {
            public void add(K key, V value)
            {
                result.put(key, value);
            }
        };

        Map<K, ContributionDef> keyToContribution = newMap();

        Collection<Module> modules = modulesThatContributeToService(serviceDef);

        for (Module m : modules)
            addToMappedConfiguration(
                    configuration,
                    keyToContribution,
                    keyType,
                    objectType,
                    serviceDef,
                    m);

        return result;
    }

    private Collection<Module> modulesThatContributeToService(ServiceDef serviceDef)
    {
        if (serviceDef.isPrivate())
        {
            String moduleId = IOCUtilities.extractModuleId(serviceDef.getServiceId());
            Module module = _modules.get(moduleId);

            return Arrays.asList(module);
        }

        return _modules.values();
    }

    private <K, V> void addToMappedConfiguration(MappedConfiguration<K, V> configuration,
            Map<K, ContributionDef> keyToContribution, Class<K> keyClass, Class<V> valueType,
            ServiceDef serviceDef, Module module)
    {
        String serviceId = serviceDef.getServiceId();
        Set<ContributionDef> contributions = module.getContributorDefsForService(serviceId);

        if (contributions.isEmpty()) return;

        Log log = getLog(serviceId);
        boolean debug = log.isDebugEnabled();

        ServiceLocator locator = new ServiceResourcesImpl(this, module, serviceDef, log);

        for (ContributionDef def : contributions)
        {
            MappedConfiguration<K, V> validating = new ValidatingMappedConfigurationWrapper<K, V>(
                    serviceId, def, log, keyClass, valueType, keyToContribution, configuration);

            if (debug) log.debug(IOCMessages.invokingMethod(def));

            def.contribute(module, locator, validating);
        }

    }

    private <T> void addToUnorderedConfiguration(Configuration<T> configuration,
            Class<T> valueType, ServiceDef serviceDef, Module module)
    {
        String serviceId = serviceDef.getServiceId();
        Set<ContributionDef> contributions = module.getContributorDefsForService(serviceId);

        if (contributions.isEmpty()) return;

        Log log = getLog(serviceId);
        boolean debug = log.isDebugEnabled();

        ServiceLocator locator = new ServiceResourcesImpl(this, module, serviceDef, log);

        for (ContributionDef def : contributions)
        {
            Configuration<T> validating = new ValidatingConfigurationWrapper<T>(serviceId, log,
                    valueType, def, configuration);

            if (debug) log.debug(IOCMessages.invokingMethod(def));

            def.contribute(module, locator, validating);
        }
    }

    private <T> void addToOrderedConfiguration(OrderedConfiguration<T> configuration,
            Class<T> valueType, ServiceDef serviceDef, Module module)
    {
        String serviceId = serviceDef.getServiceId();
        Set<ContributionDef> contributions = module.getContributorDefsForService(serviceId);

        if (contributions.isEmpty()) return;

        Log log = getLog(serviceId);
        boolean debug = log.isDebugEnabled();

        ServiceLocator locator = new ServiceResourcesImpl(this, module, serviceDef, log);

        for (ContributionDef def : contributions)
        {
            OrderedConfiguration<T> validating = new ValidatingOrderedConfigurationWrapper<T>(
                    serviceId, module.getModuleId(), def, log, valueType, configuration);

            if (debug) log.debug(IOCMessages.invokingMethod(def));

            def.contribute(module, locator, validating);
        }
    }

    // Seems like something that could be cached.
    public <T> T getService(Class<T> serviceInterface, Module module)
    {
        _lock.check();

        List<String> ids = CollectionFactory.newList();

        for (Module m : _modules.values())
        {
            Collection<String> matchingIds = m.findServiceIdsForInterface(serviceInterface, module);
            ids.addAll(matchingIds);
        }

        switch (ids.size())
        {
            case 0:

                throw new RuntimeException(IOCMessages.noServiceMatchesType(serviceInterface));

            case 1:

                String serviceId = ids.get(0);

                return getService(serviceId, serviceInterface, module);

            default:

                Collections.sort(ids);

                throw new RuntimeException(IOCMessages.manyServiceMatches(serviceInterface, ids));
        }
    }

    public <T> T getService(Class<T> serviceInterface)
    {
        _lock.check();

        return getService(serviceInterface, null);
    }

    public ServiceLifecycle getServiceLifecycle(String lifecycle)
    {
        _lock.check();

        ServiceLifecycle result = _lifecycles.get(lifecycle);

        if (result == null)
        {
            ServiceLifecycleSource source = getService(
                    "tapestry.ioc.ServiceLifecycleSource",
                    ServiceLifecycleSource.class);
            result = source.get(lifecycle);
        }

        if (result == null) throw new RuntimeException(IOCMessages.unknownLifecycle(lifecycle));

        return result;
    }

    public List<ServiceDecorator> findDecoratorsForService(ServiceDef serviceDef)
    {
        _lock.check();

        Log log = getLog(serviceDef.getServiceId());

        Orderer<DecoratorDef> orderer = new Orderer<DecoratorDef>(log);

        addDecoratorDefsToOrderer(orderer, serviceDef);

        List<DecoratorDef> ordered = orderer.getOrdered();

        return convertDecoratorDefsToServiceDecorators(ordered, serviceDef, log);
    }

    private List<ServiceDecorator> convertDecoratorDefsToServiceDecorators(
            List<DecoratorDef> ordered, ServiceDef serviceDef, Log log)
    {
        List<ServiceDecorator> result = newList();

        ServiceResources resources = null;
        String moduleId = null;
        Module module = null;

        for (DecoratorDef dd : ordered)
        {
            String decoratorModuleId = IOCUtilities.extractModuleId(dd.getDecoratorId());

            // Whenever the module id containing the decorator changes,
            // "refresh" the resources, etc., to point to the (new) module.
            // This means that the ServiceResources will identify services within
            // the decorators module (important in terms of service visibility
            // and abbreviated service ids).

            if (!decoratorModuleId.equals(moduleId))
            {
                moduleId = decoratorModuleId;

                module = _modules.get(moduleId);

                resources = new ServiceResourcesImpl(this, module, serviceDef, log);
            }

            ServiceDecorator decorator = dd.createDecorator(module, resources);

            result.add(decorator);
        }

        return result;
    }

    private void addDecoratorDefsToOrderer(Orderer<DecoratorDef> orderer, ServiceDef serviceDef)
    {
        if (serviceDef.isPrivate())
        {
            Set<DecoratorDef> privateDecorators = findDecoratorsDefsForPrivateService(serviceDef);
            addToOrderer(orderer, privateDecorators);
        }
        else
        {
            for (Module m : _modules.values())
            {
                Set<DecoratorDef> moduleDecorators = m.findMatchingDecoratorDefs(serviceDef);
                addToOrderer(orderer, moduleDecorators);
            }
        }
    }

    private void addToOrderer(Orderer<DecoratorDef> orderer, Set<DecoratorDef> decorators)
    {
        for (DecoratorDef df : decorators)
        {
            orderer.add(df.getDecoratorId(), df, df.getConstraints());
        }
    }

    private Set<DecoratorDef> findDecoratorsDefsForPrivateService(ServiceDef serviceDef)
    {
        String moduleId = IOCUtilities.extractModuleId(serviceDef.getServiceId());
        Module module = _modules.get(moduleId);

        return module.findMatchingDecoratorDefs(serviceDef);
    }

    public Log getLog(Class clazz)
    {
        _lock.check();

        return _logSource.getLog(clazz);
    }

    public Log getLog(String name)
    {
        _lock.check();

        return _logSource.getLog(name);
    }

    public ClassFab newClass(Class serviceInterface)
    {
        _lock.check();

        return _classFactory.newClass(serviceInterface);
    }

    public <T> T getObject(String reference, Class<T> objectType, ServiceLocator locator)
    {
        _lock.check();

        ObjectProvider masterProvider = getService(
                IOCConstants.MASTER_OBJECT_PROVIDER_SERVICE_ID,
                ObjectProvider.class);

        return masterProvider.provide(reference, objectType, locator);
    }

    public <T> T getObject(String reference, Class<T> objectType)
    {
        _lock.check();

        // Concerened about this causing potential endless loops.
        return getObject(reference, objectType, this);
    }

    public void addRegistryShutdownListener(RegistryShutdownListener listener)
    {
        _lock.check();

        _registryShutdownHub.addRegistryShutdownListener(listener);
    }

    public String expandSymbols(String input)
    {
        // Again, a bit of work to avoid instantiating the SymbolSource until absolutely necessary.

        if (!InternalUtils.containsSymbols(input)) return input;

        return getSymbolSource().expandSymbols(input);
    }

    /** Defers obtaining the symbol source until actually needed. */
    private synchronized SymbolSource getSymbolSource()
    {
        if (_symbolSource == null)
            _symbolSource = getService(SYMBOL_SOURCE_SERVICE_ID, SymbolSource.class);

        return _symbolSource;
    }

}
