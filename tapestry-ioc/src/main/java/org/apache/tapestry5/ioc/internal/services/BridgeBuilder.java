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

package org.apache.tapestry5.ioc.internal.services;

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;
import org.apache.tapestry5.ioc.services.ClassFab;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.services.MethodIterator;
import org.apache.tapestry5.ioc.services.MethodSignature;
import org.slf4j.Logger;

import static java.lang.String.format;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

/**
 * Used by the {@link org.apache.tapestry5.ioc.internal.services.PipelineBuilderImpl} to create bridge classes and to
 * create instances of bridge classes. A bridge class implements the <em>service</em> interface. Within the chain,
 * bridge 1 is passed to filter 1. Invoking methods on bridge 1 will invoke methods on filter 2.
 */
class BridgeBuilder<S, F>
{
    private final Logger logger;

    private final Class<S> serviceInterface;

    private final Class<F> filterInterface;

    private final ClassFab classFab;

    private final FilterMethodAnalyzer filterMethodAnalyzer;

    private Constructor constructor;

    BridgeBuilder(Logger logger, Class<S> serviceInterface, Class<F> filterInterface,
                  ClassFactory classFactory)
    {
        this.logger = logger;
        this.serviceInterface = serviceInterface;
        this.filterInterface = filterInterface;

        classFab = classFactory.newClass(this.serviceInterface);

        filterMethodAnalyzer = new FilterMethodAnalyzer(serviceInterface);
    }

    private void createClass()
    {
        List<MethodSignature> serviceMethods = newList();
        List<MethodSignature> filterMethods = newList();

        createInfrastructure();

        MethodIterator mi = new MethodIterator(serviceInterface);

        while (mi.hasNext())
        {
            serviceMethods.add(mi.next());
        }

        boolean toStringMethodExists = mi.getToString();

        mi = new MethodIterator(filterInterface);

        while (mi.hasNext())
        {
            filterMethods.add(mi.next());
        }

        while (!serviceMethods.isEmpty())
        {
            MethodSignature ms = serviceMethods.remove(0);

            addBridgeMethod(ms, filterMethods);
        }

        reportExtraFilterMethods(filterMethods);

        if (!toStringMethodExists)
        {
            String toString = format(
                    "<PipelineBridge from %s to %s>",
                    serviceInterface.getName(),
                    filterInterface.getName());
            classFab.addToString(toString);
        }

        Class bridgeClass = classFab.createClass();

        constructor = bridgeClass.getConstructors()[0];
    }

    private void createInfrastructure()
    {
        classFab.addField("_next", Modifier.PRIVATE | Modifier.FINAL, serviceInterface);
        classFab.addField("_filter", Modifier.PRIVATE | Modifier.FINAL, filterInterface);

        classFab.addConstructor(new Class[]
                { serviceInterface, filterInterface }, null, "{ _next = $1; _filter = $2; }");

        classFab.addInterface(serviceInterface);
    }

    /**
     * Instantiates a bridge object.
     *
     * @param nextBridge the next Bridge object in the pipeline, or the terminator service
     * @param filter     the filter object for this step of the pipeline
     */
    public S instantiateBridge(S nextBridge, F filter)
    {
        if (constructor == null) createClass();

        try
        {
            Object instance = constructor.newInstance(nextBridge, filter);

            return serviceInterface.cast(instance);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private void reportExtraFilterMethods(List filterMethods)
    {
        Iterator i = filterMethods.iterator();

        while (i.hasNext())
        {
            MethodSignature ms = (MethodSignature) i.next();

            logger.error(ServiceMessages
                    .extraFilterMethod(ms, filterInterface, serviceInterface));
        }
    }

    /**
     * Finds a matching method in filterMethods for the given service method. A matching method has the same signature
     * as the service interface method, but with an additional parameter matching the service interface itself.
     * <p/>
     * The matching method signature from the list of filterMethods is removed and code generation strategies for making
     * the two methods call each other are added.
     */
    private void addBridgeMethod(MethodSignature ms, List filterMethods)
    {
        Iterator i = filterMethods.iterator();

        while (i.hasNext())
        {
            MethodSignature fms = (MethodSignature) i.next();

            int position = filterMethodAnalyzer.findServiceInterfacePosition(ms, fms);

            if (position >= 0)
            {
                addBridgeMethod(position, ms, fms);
                i.remove();
                return;
            }
        }

        String message = ServiceMessages.unmatchedServiceMethod(ms, filterInterface);

        logger.error(message);

        String code = format("throw new %s(\"%s\");", RuntimeException.class.getName(), message);

        classFab.addMethod(Modifier.PUBLIC, ms, code);
    }

    /**
     * Adds a method to the class which bridges from the service method to the corresponding method in the filter
     * interface. The next service (either another Bridge, or the terminator at the end of the pipeline) is passed to
     * the filter).
     */
    private void addBridgeMethod(int position, MethodSignature ms, MethodSignature fms)
    {
        StringBuilder buffer = new StringBuilder(100);

        buffer.append("return ($r) _filter.");
        buffer.append(ms.getName());
        buffer.append("(");

        boolean comma = false;
        int filterParameterCount = fms.getParameterTypes().length;

        for (int i = 0; i < position; i++)
        {
            if (comma) buffer.append(", ");

            buffer.append("$");
            // Add one to the index to get the parameter symbol ($0 is the implicit
            // this parameter).
            buffer.append(i + 1);

            comma = true;
        }

        if (comma) buffer.append(", ");

        // _next is the variable in -this- Bridge that points to the -next- Bridge
        // or the terminator for the pipeline. The filter is expected to reinvoke the
        // method on the _next that's passed to it.

        buffer.append("_next");

        for (int i = position + 1; i < filterParameterCount; i++)
        {
            buffer.append(", $");
            buffer.append(i);
        }

        buffer.append(");");

        // This should work, unless the exception types turn out to not be compatble. We still
        // don't do a check on that, and not sure that Javassist does either!

        classFab.addMethod(Modifier.PUBLIC, ms, buffer.toString());
    }

}
