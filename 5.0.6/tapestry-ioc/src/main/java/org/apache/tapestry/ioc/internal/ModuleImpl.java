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

package org.apache.tapestry.ioc.internal;

import static java.lang.String.format;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.ioc.ServiceBuilderResources;
import org.apache.tapestry.ioc.ServiceDecorator;
import org.apache.tapestry.ioc.ServiceResources;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.apache.tapestry.ioc.def.DecoratorDef;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.internal.services.JustInTimeObjectCreator;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.ClassFab;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.MethodSignature;
import org.apache.tapestry.ioc.services.Status;
import org.apache.tapestry.ioc.services.TapestryIOCModule;
import org.slf4j.Logger;

public class ModuleImpl implements Module
{
    private final InternalRegistry _registry;

    private final ServiceActivityTracker _tracker;

    private final ModuleDef _moduleDef;

    private final ClassFactory _classFactory;

    private final Logger _logger;

    // Guarded by MUTEX
    private Object _moduleBuilder;

    /**
     * A single mutex, shared by all modules, that serializes creation of services across all
     * threads. This is a bit draconian, but appears to be necessary. Fortunately, service creation
     * is a very tiny part of any individual service's lifecycle.
     */
    private static final Object MUTEX = new Object();

    // Set to true when invoking the module constructor. Used to
    // detect endless loops caused by irresponsible dependencies in
    // the constructor. Guarded by MUTEX.
    private boolean _insideConstructor;

    /** Keyed on fully qualified service id; values are instantiated services (proxies). */
    private final Map<String, Object> _services = newCaseInsensitiveMap();

    public ModuleImpl(InternalRegistry registry, ServiceActivityTracker tracker,
            ModuleDef moduleDef, ClassFactory classFactory, Logger logger)
    {
        _registry = registry;
        _tracker = tracker;
        _moduleDef = moduleDef;
        _classFactory = classFactory;
        _logger = logger;
    }

    public <T> T getService(String serviceId, Class<T> serviceInterface)
    {
        notBlank(serviceId, "serviceId");
        notNull(serviceInterface, "serviceInterface");
        // module may be null.

        ServiceDef def = _moduleDef.getServiceDef(serviceId);

        // RegistryImpl should already have checked that the service exists.
        assert def != null;

        Object service = findOrCreate(def, null);

        try
        {
            return serviceInterface.cast(service);
        }
        catch (ClassCastException ex)
        {
            // This may be overkill: I don't know how this could happen
            // given that the return type of the method determines
            // the service interface.

            throw new RuntimeException(IOCMessages.serviceWrongInterface(serviceId, def
                    .getServiceInterface(), serviceInterface));
        }
    }

    public Set<DecoratorDef> findMatchingDecoratorDefs(ServiceDef serviceDef)
    {
        Set<DecoratorDef> result = newSet();

        for (DecoratorDef def : _moduleDef.getDecoratorDefs())
        {
            if (def.matches(serviceDef)) result.add(def);
        }

        return result;
    }

    public List<ServiceDecorator> findDecoratorsForService(String serviceId)
    {
        ServiceDef sd = _moduleDef.getServiceDef(serviceId);

        return _registry.findDecoratorsForService(sd);
    }

    @SuppressWarnings("unchecked")
    public Collection<String> findServiceIdsForInterface(Class serviceInterface)
    {
        notNull(serviceInterface, "serviceInterface");

        Collection<String> result = newList();

        for (String id : _moduleDef.getServiceIds())
        {
            ServiceDef def = _moduleDef.getServiceDef(id);

            if (serviceInterface.isAssignableFrom(def.getServiceInterface())) result.add(id);
        }

        return result;
    }

    /**
     * Locates the service proxy for a particular service (from the service definition).
     * <p>
     * Access is synchronized via {@link #MUTEX}.
     * 
     * @param def
     *            defines the service
     * @param eagerLoadProxies
     *            TODO
     * @return the service proxy
     */
    private Object findOrCreate(ServiceDef def, List<EagerLoadServiceProxy> eagerLoadProxies)
    {
        synchronized (MUTEX)
        {
            String key = def.getServiceId();

            Object result = _services.get(key);

            if (result == null)
            {
                result = create(def, eagerLoadProxies);
                _services.put(key, result);
            }

            return result;
        }
    }

    public void eagerLoadServices()
    {
        List<EagerLoadServiceProxy> proxies = newList();

        synchronized (MUTEX)
        {
            for (String serviceId : _moduleDef.getServiceIds())
            {
                ServiceDef def = _moduleDef.getServiceDef(serviceId);

                if (def.isEagerLoad()) findOrCreate(def, proxies);
            }

            for (EagerLoadServiceProxy proxy : proxies)
                proxy.eagerLoadService();
        }
    }

    /**
     * Creates the service and updates the cache of created services. Access is synchronized via
     * {@link #MUTEX}.
     * 
     * @param eagerLoadProxies
     *            a list into which any eager loaded proxies should be added
     */
    private Object create(ServiceDef def, List<EagerLoadServiceProxy> eagerLoadProxies)
    {
        String serviceId = def.getServiceId();

        Logger logger = _registry.getServiceLogger(serviceId);

        if (logger.isDebugEnabled()) logger.debug(IOCMessages.creatingService(serviceId));

        try
        {
            ServiceBuilderResources resources = new ServiceResourcesImpl(_registry, this, def,
                    _classFactory, logger);

            // Build up a stack of operations that will be needed to realize the service
            // (by the proxy, at a later date).

            ObjectCreator creator = def.createServiceCreator(resources);

            Class serviceInterface = def.getServiceInterface();

            // For non-proxyable services, we immediately create the service implementation
            // and return it. There's no interface to proxy, which throws out the possibility of
            // deferred instantiation, service lifecycles, and decorators.

            if (!serviceInterface.isInterface()) return creator.createObject();

            creator = new LifecycleWrappedServiceCreator(_registry, def.getServiceScope(),
                    resources, creator);

            // Don't allow the core IOC services services to be decorated.

            if (!TapestryIOCModule.class.equals(_moduleDef.getBuilderClass()))
                creator = new InterceptorStackBuilder(this, serviceId, creator);

            // Add a wrapper that checks for recursion.

            creator = new RecursiveServiceCreationCheckWrapper(def, creator, logger);

            JustInTimeObjectCreator delegate = new JustInTimeObjectCreator(_tracker, creator,
                    serviceId);

            Object proxy = createProxy(resources, delegate);

            _registry.addRegistryShutdownListener(delegate);

            // Occasionally service A may invoke service B from its service builder method; if
            // service B
            // is eager loaded, we'll hit this method but eagerLoadProxies will be null. That's OK
            // ... service B
            // is being realized anyway.

            if (def.isEagerLoad() && eagerLoadProxies != null) eagerLoadProxies.add(delegate);

            _tracker.setStatus(serviceId, Status.VIRTUAL);

            return proxy;
        }
        catch (Exception ex)
        {
            throw new RuntimeException(IOCMessages.errorBuildingService(serviceId, def, ex), ex);
        }
    }

    public Object getModuleBuilder()
    {
        synchronized (MUTEX)
        {
            if (_moduleBuilder == null) _moduleBuilder = instantiateModuleBuilder();

            return _moduleBuilder;
        }
    }

    /** Access synchronized by MUTEX. */
    private Object instantiateModuleBuilder()
    {
        Class builderClass = _moduleDef.getBuilderClass();

        Constructor[] constructors = builderClass.getConstructors();

        if (constructors.length == 0)
            throw new RuntimeException(IOCMessages.noPublicConstructors(builderClass));

        if (constructors.length > 1)
        {
            // Sort the constructors ascending by number of parameters (descending); this is really
            // just to allow the test suite to work properly across different JVMs (which will
            // often order the constructors differently).

            Comparator<Constructor> comparator = new Comparator<Constructor>()
            {
                public int compare(Constructor c1, Constructor c2)
                {
                    return c2.getParameterTypes().length - c1.getParameterTypes().length;
                }
            };

            Arrays.sort(constructors, comparator);

            _logger.warn(IOCMessages.tooManyPublicConstructors(builderClass, constructors[0]));
        }

        Constructor constructor = constructors[0];

        if (_insideConstructor)
            throw new RuntimeException(IOCMessages.recursiveModuleConstructor(
                    builderClass,
                    constructor));

        ObjectLocator locator = new ObjectLocatorImpl(_registry, this);
        Map<Class, Object> parameterDefaults = newMap();

        parameterDefaults.put(Logger.class, _logger);
        parameterDefaults.put(ObjectLocator.class, locator);

        Throwable fail = null;

        try
        {
            _insideConstructor = true;

            Object[] parameterValues = InternalUtils.calculateParameters(
                    locator,
                    parameterDefaults,
                    constructor.getParameterTypes(),
                    constructor.getParameterAnnotations());

            return constructor.newInstance(parameterValues);
        }
        catch (InvocationTargetException ex)
        {
            fail = ex.getTargetException();
        }
        catch (Exception ex)
        {
            fail = ex;
        }
        finally
        {
            _insideConstructor = false;
        }

        throw new RuntimeException(IOCMessages.instantiateBuilderError(builderClass, fail), fail);
    }

    private Object createProxy(ServiceResources resources, ObjectCreator creator)
    {
        String serviceId = resources.getServiceId();
        Class serviceInterface = resources.getServiceInterface();

        String toString = format("<Proxy for %s(%s)>", serviceId, serviceInterface.getName());

        return createProxyInstance(creator, serviceId, serviceInterface, toString);
    }

    private Object createProxyInstance(ObjectCreator creator, String serviceId,
            Class serviceInterface, String description)
    {
        Class proxyClass = createProxyClass(serviceId, serviceInterface, description);

        try
        {
            return proxyClass.getConstructors()[0].newInstance(creator);
        }
        catch (Exception ex)
        {
            // This should never happen, so we won't go to a lot of trouble
            // reporting it.
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private Class createProxyClass(String serviceId, Class serviceInterface, String proxyDescription)
    {
        ClassFab cf = _registry.newClass(serviceInterface);

        cf.addField("_creator", Modifier.PRIVATE | Modifier.FINAL, ObjectCreator.class);

        cf.addConstructor(new Class[]
        { ObjectCreator.class }, null, "_creator = $1;");

        addDelegateGetter(cf, serviceInterface, serviceId);

        cf.proxyMethodsToDelegate(serviceInterface, "_delegate()", proxyDescription);

        return cf.createClass();
    }

    private void addDelegateGetter(ClassFab cf, Class serviceInterface, String serviceId)
    {
        String body = format("return (%s) _creator.createObject();", serviceInterface.getName());

        MethodSignature sig = new MethodSignature(serviceInterface, "_delegate", null, null);

        cf.addMethod(Modifier.PRIVATE, sig, body);
    }

    public Set<ContributionDef> getContributorDefsForService(String serviceId)
    {
        Set<ContributionDef> result = newSet();

        for (ContributionDef def : _moduleDef.getContributionDefs())
        {
            if (def.getServiceId().equals(serviceId)) result.add(def);
        }

        return result;
    }

    public ServiceDef getServiceDef(String serviceId)
    {
        return _moduleDef.getServiceDef(serviceId);
    }

    public String getLoggerName()
    {
        return _moduleDef.getLoggerName();
    }

}
