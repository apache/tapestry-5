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

import static org.easymock.EasyMock.isA;

import java.util.Collections;
import java.util.List;

import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.RegistryBuilder;
import org.apache.tapestry.ioc.ServiceDecorator;
import org.apache.tapestry.ioc.ServiceLifecycle;
import org.apache.tapestry.ioc.def.ServiceDef;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.test.IOCTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class IOCInternalTestCase extends IOCTestCase implements Registry
{
    private static Registry _registry;

    private static ClassFactory _classFactory;

    @BeforeSuite
    public final void setup_registry()
    {
        RegistryBuilder builder = new RegistryBuilder();

        _registry = builder.build();

        _classFactory = _registry.getService(ClassFactory.class);
    }

    @AfterSuite
    public final void shutdown_registry()
    {
        _registry.shutdown();

        _registry = null;
        _classFactory = null;
    }

    @AfterMethod
    public final void cleanupThread()
    {
        _registry.cleanupThread();
    }

    public final ClassFactory getClassFactory()
    {
        return _classFactory;
    }

    public final <T> T getObject(String reference, Class<T> objectType)
    {
        return _registry.getObject(reference, objectType);
    }

    public final <T> T getService(Class<T> serviceInterface)
    {
        return _registry.getService(serviceInterface);
    }

    public final <T> T getService(String serviceId, Class<T> serviceInterface)
    {
        return _registry.getService(serviceId, serviceInterface);
    }

    public final void shutdown()
    {
        throw new UnsupportedOperationException("No registry shutdown until @AfterSuite.");
    }

    protected final void train_findDecoratorsForService(Module module, String serviceId,
            List<ServiceDecorator> decorators)
    {
        expect(module.findDecoratorsForService(serviceId)).andReturn(decorators);
    }

    protected final void train_findDecoratorsForService(InternalRegistry registry)
    {
        List<ServiceDecorator> result = Collections.emptyList();

        expect(registry.findDecoratorsForService(isA(ServiceDef.class))).andReturn(result);
    }

    protected final InternalRegistry newInternalRegistry()
    {
        return newMock(InternalRegistry.class);
    }

    protected final void train_getLifecycle(InternalRegistry registry, String name,
            ServiceLifecycle lifecycle)
    {
        expect(registry.getServiceLifecycle(name)).andReturn(lifecycle);
    }

    protected final <T> void train_getService(InternalRegistry registry, String serviceId,
            Class<T> serviceInterface, T service)
    {
        expect(registry.getService(serviceId, serviceInterface)).andReturn(service);
    }

    protected final Module newModule()
    {
        return newMock(Module.class);
    }

}
