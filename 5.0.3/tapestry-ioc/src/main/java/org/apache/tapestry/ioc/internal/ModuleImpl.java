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

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.ObjectCreator;
import org.apache.tapestry.ioc.ServiceBuilderResources;
import org.apache.tapestry.ioc.ServiceDecorator;
import org.apache.tapestry.ioc.ServiceLifecycle;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.ioc.ServiceResources;
import org.apache.tapestry.ioc.def.ContributionDef;
import org.apache.tapestry.ioc.def.DecoratorDef;
import org.apache.tapestry.ioc.def.ModuleDef;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.services.ClassFab;
import org.apache.tapestry.ioc.services.MethodSignature;
import org.apache.tapestry.ioc.services.RegistryShutdownListener;
import org.apache.tapestry.ioc.services.TapestryIOCModule;
import org.apache.tapestry.ioc.util.BodyBuilder;

public class ModuleImpl implements Module
{
    private final InternalRegistry _registry;

    private final ModuleDef _moduleDef;

    private final Log _log;

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

    public ModuleImpl(InternalRegistry registry, ModuleDef moduleDef, Log log)
    {
        _registry = registry;
        _moduleDef = moduleDef;
        _log = log;
    }

    /** Keyed on fully qualified service id; values are instantiated services (proxies). */
    private final Map<String, Object> _services = newCaseInsensitiveMap();

    public <T> T getService(String serviceId, Class<T> serviceInterface)
    {
        notBlank(serviceId, "serviceId");
        notNull(serviceInterface, "serviceInterface");
        // module may be null.

        ServiceDef def = _moduleDef.getServiceDef(serviceId);

        // RegistryImpl should already have checked that the service exists.
        assert def != null;

        Object service = findOrCreate(def);

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

    public Collection<String> findServiceIdsForInterface(Class serviceInterface)
    {
        notNull(serviceInterface, "serviceInterface");

        Collection<String> result = newList();

        for (String id : _moduleDef.getServiceIds())
        {
            ServiceDef def = _moduleDef.getServiceDef(id);

            if (def.getServiceInterface() != serviceInterface) continue;

            result.add(id);
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
     * @return the service proxy
     */
    private Object findOrCreate(ServiceDef def)
    {
        synchronized (MUTEX)
        {
            String key = def.getServiceId();

            Object result = _services.get(key);

            if (result == null)
            {
                result = create(def);
                _services.put(key, result);
            }

            return result;
        }
    }

    public void eagerLoadServices()
    {
        for (String id : _moduleDef.getServiceIds())
        {
            ServiceDef def = _moduleDef.getServiceDef(id);

            if (!def.isEagerLoad()) continue;

            // The proxy implements the service interface, and RegistryShutdownListener, and (for
            // eager load services), EagerLoadServiceProxy

            EagerLoadServiceProxy proxy = (EagerLoadServiceProxy) findOrCreate(def);

            proxy.eagerLoadService();
        }
    }

    /**
     * Creates the service and updates the cache of created services. Access is synchronized via
     * {@link #MUTEX}.
     */
    private Object create(ServiceDef def)
    {
        String serviceId = def.getServiceId();

        Log log = _registry.logForService(serviceId);

        if (log.isDebugEnabled()) log.debug(IOCMessages.creatingService(serviceId));

        try
        {
            ServiceLifecycle lifecycle = _registry.getServiceLifecycle(def.getServiceLifeycle());

            ServiceBuilderResources resources = new ServiceResourcesImpl(_registry, this, def, log);

            // Build up a stack of operations that will be needed to instantiate the service
            // (by the proxy, at a later date).

            ObjectCreator creator = def.createServiceCreator(resources);

            creator = new LifecycleWrappedServiceCreator(lifecycle, resources, creator);

            // Don't allow the tapestry.ioc services to be decorated.

            if (!_moduleDef.getBuilderClass().equals(TapestryIOCModule.class))
                creator = new InterceptorStackBuilder(this, serviceId, creator);

            // Add a wrapper that makes sure that it only gets created once.

            creator = new OneShotServiceCreator(def, creator, log);

            return createProxy(resources, creator, def.isEagerLoad());
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

            _log.warn(IOCMessages.tooManyPublicConstructors(builderClass, constructors[0]));
        }

        Constructor constructor = constructors[0];

        if (_insideConstructor)
            throw new RuntimeException(IOCMessages.recursiveModuleConstructor(
                    builderClass,
                    constructor));

        ServiceLocator locator = new ServiceLocatorImpl(_registry, this);
        Map<Class, Object> parameterDefaults = newMap();

        parameterDefaults.put(Log.class, _log);
        parameterDefaults.put(ServiceLocator.class, locator);

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

    private Object createProxy(ServiceResources resources, ObjectCreator creator, boolean eagerLoad)
    {
        String serviceId = resources.getServiceId();
        Class serviceInterface = resources.getServiceInterface();

        String toString = format("<Proxy for %s(%s)>", serviceId, serviceInterface.getName());

        RegistryShutdownListener proxy = createProxyInstance(
                creator,
                serviceId,
                serviceInterface,
                eagerLoad,
                toString);

        _registry.addRegistryShutdownListener(proxy);

        return proxy;
    }

    private RegistryShutdownListener createProxyInstance(ObjectCreator creator, String serviceId,
            Class serviceInterface, boolean eagerLoad, String description)
    {
        Class proxyClass = createProxyClass(serviceId, serviceInterface, eagerLoad, description);

        try
        {
            return (RegistryShutdownListener) proxyClass.getConstructors()[0].newInstance(creator);
        }
        catch (Exception ex)
        {
            // This should never happen, so we won't go to a lot of trouble
            // reporting it.
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private Class createProxyClass(String serviceId, Class serviceInterface, boolean eagerLoad,
            String proxyDescription)
    {
        ClassFab cf = _registry.newClass(serviceInterface);

        cf.addField("_creator", ObjectCreator.class);
        cf.addField("_delegate", serviceInterface);
        cf.addField("_shutdown", boolean.class);

        cf.addConstructor(new Class[]
        { ObjectCreator.class }, null, "_creator = $1;");

        addDelegateGetter(cf, serviceInterface, serviceId);

        addShutdownListenerMethod(cf);

        cf.proxyMethodsToDelegate(serviceInterface, "_delegate()", proxyDescription);

        // For eager load services, add an eagerLoadService() method that calls _delegate(), to
        // force the creation of the service.

        if (eagerLoad)
        {
            cf.addInterface(EagerLoadServiceProxy.class);

            cf.addMethod(Modifier.PUBLIC, new MethodSignature(void.class, "eagerLoadService", null,
                    null), "_delegate();");
        }

        return cf.createClass();
    }

    private void addDelegateGetter(ClassFab cf, Class serviceInterface, String serviceId)
    {
        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        // Check to see if the registry has shutdown. The registryShutdown() method
        // throws IllegalStateException.

        builder.addln("if (_shutdown) %s.registryShutdown(\"%s\");", IOCProxyUtilities.class
                .getName(), serviceId);

        // We can release the creator after invoking it, we only create the service once.

        builder.addln("if (_delegate == null)");
        builder.begin();
        builder.addln("_delegate = (%s) _creator.createObject();", serviceInterface.getName());
        builder.addln("_creator = null;");
        builder.end();

        builder.addln("return _delegate;");
        builder.end();

        MethodSignature sig = new MethodSignature(serviceInterface, "_delegate", null, null);

        // Here's the rub, this _delegate() method has to be synchronized. But after the first
        // time through (when we create the service), the time inside the method is infintesmal.
        // Let's hope that they aren't lying when they say that synchronized is now super cheap!

        cf.addMethod(Modifier.PRIVATE | Modifier.SYNCHRONIZED, sig, builder.toString());
    }

    /**
     * All proxies implement {@link RegistryShutdownListener}. When the registry shuts down, the
     * proxy sets a flag that ultimately converts method invocations to
     * {@link IllegalStateException}s, and discards its delegate and creator.
     */
    private void addShutdownListenerMethod(ClassFab cf)
    {
        cf.addInterface(RegistryShutdownListener.class);

        MethodSignature sig = new MethodSignature(void.class, "registryDidShutdown", null, null);

        cf.addMethod(
                Modifier.PUBLIC | Modifier.SYNCHRONIZED,
                sig,
                "{ _shutdown = true; _delegate = null; _creator = null; }");
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

    public String getLogName()
    {
        return _moduleDef.getLogName();
    }

}
