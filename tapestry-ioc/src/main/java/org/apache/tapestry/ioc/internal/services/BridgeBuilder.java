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

package org.apache.tapestry.ioc.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import org.apache.tapestry.ioc.services.ClassFab;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.MethodIterator;
import org.apache.tapestry.ioc.services.MethodSignature;
import org.slf4j.Logger;

import static java.lang.String.format;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

/**
 * Used by the {@link org.apache.tapestry.ioc.internal.services.PipelineBuilderImpl} to create
 * bridge classes and to create instances of bridge classes. A bridge class implements the
 * <em>service</em> interface. Within the chain, bridge 1 is passed to filter 1. Invoking methods
 * on bridge 1 will invoke methods on filter 2.
 */
class BridgeBuilder<S, F>
{
    private final Logger _logger;

    private final Class<S> _serviceInterface;

    private final Class<F> _filterInterface;

    private final ClassFab _classFab;

    private final FilterMethodAnalyzer _filterMethodAnalyzer;

    private Constructor _constructor;

    BridgeBuilder(Logger logger, Class<S> serviceInterface, Class<F> filterInterface,
                  ClassFactory classFactory)
    {
        _logger = logger;
        _serviceInterface = serviceInterface;
        _filterInterface = filterInterface;

        _classFab = classFactory.newClass(_serviceInterface);

        _filterMethodAnalyzer = new FilterMethodAnalyzer(serviceInterface);
    }

    private void createClass()
    {
        List<MethodSignature> serviceMethods = newList();
        List<MethodSignature> filterMethods = newList();

        createInfrastructure();

        MethodIterator mi = new MethodIterator(_serviceInterface);

        while (mi.hasNext())
        {
            serviceMethods.add(mi.next());
        }

        boolean toStringMethodExists = mi.getToString();

        mi = new MethodIterator(_filterInterface);

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
                    _serviceInterface.getName(),
                    _filterInterface.getName());
            _classFab.addToString(toString);
        }

        Class bridgeClass = _classFab.createClass();

        _constructor = bridgeClass.getConstructors()[0];
    }

    private void createInfrastructure()
    {
        _classFab.addField("_next", Modifier.PRIVATE | Modifier.FINAL, _serviceInterface);
        _classFab.addField("_filter", Modifier.PRIVATE | Modifier.FINAL, _filterInterface);

        _classFab.addConstructor(new Class[]
                {_serviceInterface, _filterInterface}, null, "{ _next = $1; _filter = $2; }");

        _classFab.addInterface(_serviceInterface);
    }

    /**
     * Instantiates a bridge object.
     *
     * @param nextBridge the next Bridge object in the pipeline, or the terminator service
     * @param filter     the filter object for this step of the pipeline
     */
    public S instantiateBridge(S nextBridge, F filter)
    {
        if (_constructor == null) createClass();

        try
        {
            Object instance = _constructor.newInstance(nextBridge, filter);

            return _serviceInterface.cast(instance);
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

            _logger.error(ServiceMessages
                    .extraFilterMethod(ms, _filterInterface, _serviceInterface));
        }
    }

    /**
     * Finds a matching method in filterMethods for the given service method. A matching method has
     * the same signature as the service interface method, but with an additional parameter matching
     * the service interface itself.
     * <p/>
     * The matching method signature from the list of filterMethods is removed and code generation
     * strategies for making the two methods call each other are added.
     */
    private void addBridgeMethod(MethodSignature ms, List filterMethods)
    {
        Iterator i = filterMethods.iterator();

        while (i.hasNext())
        {
            MethodSignature fms = (MethodSignature) i.next();

            int position = _filterMethodAnalyzer.findServiceInterfacePosition(ms, fms);

            if (position >= 0)
            {
                addBridgeMethod(position, ms, fms);
                i.remove();
                return;
            }
        }

        String message = ServiceMessages.unmatchedServiceMethod(ms, _filterInterface);

        _logger.error(message);

        String code = format("throw new %s(\"%s\");", RuntimeException.class.getName(), message);

        _classFab.addMethod(Modifier.PUBLIC, ms, code);
    }

    /**
     * Adds a method to the class which bridges from the service method to the corresponding method
     * in the filter interface. The next service (either another Bridge, or the terminator at the
     * end of the pipeline) is passed to the filter).
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

        _classFab.addMethod(Modifier.PUBLIC, ms, buffer.toString());
    }

}