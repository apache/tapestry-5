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
import org.apache.tapestry5.ioc.internal.services.JustInTimeObjectCreator;
import org.apache.tapestry5.ioc.internal.util.*;
import org.apache.tapestry5.ioc.services.*;
import org.slf4j.Logger;

import java.io.ObjectStreamException;
import java.io.Serializable;
import static java.lang.String.format;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

public class ModuleImpl implements Module
{
    private final InternalRegistry registry;

    private final ServiceActivityTracker tracker;

    private final ModuleDef moduleDef;

    private final ClassFactory classFactory;

    private final Logger logger;

    /**
     * Lazily instantiated.  Access is guarded by BARRIER.
     */
    private Object moduleBuilder;

    // Set to true when invoking the module constructor. Used to
    // detect endless loops caused by irresponsible dependencies in
    // the constructor.
    private boolean insideConstructor;

    /**
     * Keyed on fully qualified service id; values are instantiated services (proxies). Guarded by BARRIER.
     */
    private final Map<String, Object> services = CollectionFactory.newCaseInsensitiveMap();

    /**
     * The barrier is shared by all modules, which means that creation of *any* service for any module is single
     * threaded.
     */
    private final static ConcurrentBarrier BARRIER = new ConcurrentBarrier();

    public ModuleImpl(InternalRegistry registry, ServiceActivityTracker tracker, ModuleDef moduleDef,
                      ClassFactory classFactory, Logger logger)
    {
        this.registry = registry;
        this.tracker = tracker;
        this.moduleDef = moduleDef;
        this.classFactory = classFactory;
        this.logger = logger;
    }

    public <T> T getService(String serviceId, Class<T> serviceInterface)
    {
        Defense.notBlank(serviceId, "serviceId");
        Defense.notNull(serviceInterface, "serviceInterface");
        // module may be null.

        ServiceDef def = moduleDef.getServiceDef(serviceId);

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
        Set<DecoratorDef> result = CollectionFactory.newSet();

        for (DecoratorDef def : moduleDef.getDecoratorDefs())
        {
            if (def.matches(serviceDef)) result.add(def);
        }

        return result;
    }

    public List<ServiceDecorator> findDecoratorsForService(String serviceId)
    {
        ServiceDef sd = moduleDef.getServiceDef(serviceId);

        return registry.findDecoratorsForService(sd);
    }

    @SuppressWarnings("unchecked")
    public Collection<String> findServiceIdsForInterface(Class serviceInterface)
    {
        Defense.notNull(serviceInterface, "serviceInterface");

        Collection<String> result = CollectionFactory.newList();

        for (String id : moduleDef.getServiceIds())
        {
            ServiceDef def = moduleDef.getServiceDef(id);

            if (serviceInterface.isAssignableFrom(def.getServiceInterface())) result.add(id);
        }

        return result;
    }

    /**
     * Locates the service proxy for a particular service (from the service definition).
     *
     * @param def              defines the service
     * @param eagerLoadProxies collection into which proxies for eager loaded services are added (or null)
     * @return the service proxy
     */
    private Object findOrCreate(final ServiceDef def,
                                final Collection<EagerLoadServiceProxy> eagerLoadProxies)
    {
        final String key = def.getServiceId();

        final Invokable create = new Invokable()
        {
            public Object invoke()
            {
                // In a race condition, two threads may try to create the same service simulatenously.
                // The second will block until after the first creates the service.

                Object result = services.get(key);

                // Normally, result is null, unless some other thread slipped in and created the service
                // proxy.

                if (result == null)
                {
                    result = create(def, eagerLoadProxies);

                    services.put(key, result);
                }

                return result;
            }
        };

        Invokable find = new Invokable()
        {
            public Object invoke()
            {
                Object result = services.get(key);

                if (result == null)
                {
                    result = BARRIER.withWrite(create);
                }

                return result;
            }
        };

        return BARRIER.withRead(find);
    }

    public void collectEagerLoadServices(Collection<EagerLoadServiceProxy> proxies)
    {
        for (String serviceId : moduleDef.getServiceIds())
        {
            ServiceDef def = moduleDef.getServiceDef(serviceId);

            if (def.isEagerLoad()) findOrCreate(def, proxies);
        }
    }

    /**
     * Creates the service and updates the cache of created services.
     *
     * @param eagerLoadProxies a list into which any eager loaded proxies should be added
     */
    private Object create(ServiceDef def, Collection<EagerLoadServiceProxy> eagerLoadProxies)
    {
        String serviceId = def.getServiceId();

        Logger logger = registry.getServiceLogger(serviceId);

        if (logger.isDebugEnabled()) logger.debug(IOCMessages.creatingService(serviceId));

        try
        {
            ServiceBuilderResources resources = new ServiceResourcesImpl(registry, this, def, classFactory, logger);

            // Build up a stack of operations that will be needed to realize the service
            // (by the proxy, at a later date).

            ObjectCreator creator = def.createServiceCreator(resources);

            Class serviceInterface = def.getServiceInterface();

            // For non-proxyable services, we immediately create the service implementation
            // and return it. There's no interface to proxy, which throws out the possibility of
            // deferred instantiation, service lifecycles, and decorators.

            if (!serviceInterface.isInterface()) return creator.createObject();

            creator = new LifecycleWrappedServiceCreator(registry, def.getServiceScope(), resources, creator);

            // Don't allow the core IOC services services to be decorated.

            if (!TapestryIOCModule.class.equals(moduleDef.getBuilderClass()))
                creator = new InterceptorStackBuilder(this, serviceId, creator);

            // Add a wrapper that checks for recursion.

            creator = new RecursiveServiceCreationCheckWrapper(def, creator, logger);

            JustInTimeObjectCreator delegate = new JustInTimeObjectCreator(tracker, creator, serviceId);

            Object proxy = createProxy(resources, delegate);

            registry.addRegistryShutdownListener(delegate);

            // Occasionally service A may invoke service B from its service builder method; if
            // service B
            // is eager loaded, we'll hit this method but eagerLoadProxies will be null. That's OK
            // ... service B
            // is being realized anyway.

            if (def.isEagerLoad() && eagerLoadProxies != null) eagerLoadProxies.add(delegate);

            tracker.setStatus(serviceId, Status.VIRTUAL);

            return proxy;
        }
        catch (Exception ex)
        {
            throw new RuntimeException(IOCMessages.errorBuildingService(serviceId, def, ex), ex);
        }
    }

    private final Runnable instantiateModuleBuilder = new Runnable()
    {
        public void run()
        {
            moduleBuilder = constructModuleBuilder();
        }
    };

    private final Invokable provideModuleBuilder = new Invokable<Object>()
    {
        public Object invoke()
        {
            if (moduleBuilder == null) BARRIER.withWrite(instantiateModuleBuilder);

            return moduleBuilder;
        }
    };

    public Object getModuleBuilder()
    {
        return BARRIER.withRead(provideModuleBuilder);
    }

    private Object constructModuleBuilder()
    {
        Class builderClass = moduleDef.getBuilderClass();

        Constructor[] constructors = builderClass.getConstructors();

        if (constructors.length == 0) throw new RuntimeException(IOCMessages.noPublicConstructors(builderClass));

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

            logger.warn(IOCMessages.tooManyPublicConstructors(builderClass, constructors[0]));
        }

        Constructor constructor = constructors[0];

        if (insideConstructor)
            throw new RuntimeException(IOCMessages.recursiveModuleConstructor(builderClass, constructor));

        ObjectLocator locator = new ObjectLocatorImpl(registry, this);
        Map<Class, Object> parameterDefaults = CollectionFactory.newMap();

        parameterDefaults.put(Logger.class, logger);
        parameterDefaults.put(ObjectLocator.class, locator);

        Throwable fail = null;

        try
        {
            insideConstructor = true;

            Object[] parameterValues = InternalUtils.calculateParameters(locator, parameterDefaults,
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
            insideConstructor = false;
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

    private Object createProxyInstance(ObjectCreator creator, String serviceId, Class serviceInterface,
                                       String description)
    {
        ServiceProxyToken token = SerializationSupport.createToken(serviceId);

        ClassFab classFab = registry.newClass(serviceInterface);

        classFab.addField("creator", Modifier.PRIVATE | Modifier.FINAL, ObjectCreator.class);
        classFab.addField("token", Modifier.PRIVATE | Modifier.FINAL, ServiceProxyToken.class);

        classFab.addConstructor(new Class[] {ObjectCreator.class, ServiceProxyToken.class}, null,
                                "{ creator = $1; token = $2; }");

        // Make proxies serializable by writing the token to the stream.

        classFab.addInterface(Serializable.class);

        // This is the "magic" signature that allows an object to substitute some other
        // object for itself.
        MethodSignature writeReplaceSig = new MethodSignature(Object.class, "writeReplace", null,
                                                              new Class[] {ObjectStreamException.class});

        classFab.addMethod(Modifier.PRIVATE, writeReplaceSig, "return token;");

        // Now delegate all the methods.

        String body = format("return (%s) creator.createObject();", serviceInterface.getName());

        MethodSignature sig = new MethodSignature(serviceInterface, "delegate", null, null);

        classFab.addMethod(Modifier.PRIVATE, sig, body);

        classFab.proxyMethodsToDelegate(serviceInterface, "delegate()", description);

        Class proxyClass = classFab.createClass();

        try
        {
            return proxyClass.getConstructors()[0].newInstance(creator, token);
        }
        catch (Exception ex)
        {
            // Exceptions should not happen.

            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public Set<ContributionDef> getContributorDefsForService(String serviceId)
    {
        Set<ContributionDef> result = CollectionFactory.newSet();

        for (ContributionDef def : moduleDef.getContributionDefs())
        {
            if (def.getServiceId().equals(serviceId)) result.add(def);
        }

        return result;
    }

    public ServiceDef getServiceDef(String serviceId)
    {
        return moduleDef.getServiceDef(serviceId);
    }

    public String getLoggerName()
    {
        return moduleDef.getLoggerName();
    }
}
