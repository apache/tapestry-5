// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.ServiceLifecycle;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.services.*;

import static java.lang.String.format;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * Allows a service to exist "per thread" (in each thread). This involves an inner proxy, which caches an object derived
 * from a {@link org.apache.tapestry5.ioc.ObjectCreator} as a key in the {@link org.apache.tapestry5.ioc.services.PerthreadManager}.
 * Method invocations are delegated to the per-thread service instance.
 * <p/>
 * This scheme ensures that, although the service builder method will be invoked many times over the life of the
 * application, the service decoration process occurs only once. The final calling chain is: Service Proxy --&gt;
 * Decorator(s) --&gt; PerThread Proxy --&gt; (per thread) instance.
 */
public class PerThreadServiceLifecycle implements ServiceLifecycle
{
    private static final String PER_THREAD_METHOD_NAME = "_perThreadInstance";

    private final PerthreadManager perthreadManager;

    private final ClassFactory classFactory;

    public PerThreadServiceLifecycle(@Builtin
    PerthreadManager perthreadManager,

                                     @Builtin
                                     ClassFactory classFactory)
    {
        this.perthreadManager = perthreadManager;
        this.classFactory = classFactory;
    }

    /**
     * Returns false; this lifecycle represents a service that will be created many times (by each thread).
     */
    public boolean isSingleton()
    {
        return false;
    }

    public Object createService(ServiceResources resources, ObjectCreator creator)
    {
        Class proxyClass = createProxyClass(resources);

        ObjectCreator perThreadCreator = new PerThreadServiceCreator(perthreadManager, creator);

        try
        {
            Constructor ctor = proxyClass.getConstructors()[0];

            return ctor.newInstance(perThreadCreator);
        }
        catch (InvocationTargetException ex)
        {
            throw new RuntimeException(ex.getCause());
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private Class createProxyClass(ServiceResources resources)
    {
        Class serviceInterface = resources.getServiceInterface();

        ClassFab cf = classFactory.newClass(serviceInterface);

        cf.addField("_creator", Modifier.PRIVATE | Modifier.FINAL, ObjectCreator.class);

        // Constructor takes a ServiceCreator

        cf.addConstructor(new Class[]
                { ObjectCreator.class }, null, "_creator = $1;");

        String body = format("return (%s) _creator.createObject();", serviceInterface.getName());

        MethodSignature sig = new MethodSignature(serviceInterface, PER_THREAD_METHOD_NAME, null,
                                                  null);

        cf.addMethod(Modifier.PRIVATE, sig, body);

        String toString = format(
                "<PerThread Proxy for %s(%s)>",
                resources.getServiceId(),
                serviceInterface.getName());

        cf.proxyMethodsToDelegate(serviceInterface, PER_THREAD_METHOD_NAME + "()", toString);

        return cf.createClass();
    }
}
