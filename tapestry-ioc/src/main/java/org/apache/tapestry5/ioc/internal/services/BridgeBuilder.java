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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.plastic.*;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.List;

/**
 * Used by the {@link org.apache.tapestry5.ioc.internal.services.PipelineBuilderImpl} to create bridge classes and to
 * create instances of bridge classes. A bridge class implements the <em>service</em> interface. Within the chain,
 * bridge 1 is passed to filter 1. Invoking methods on bridge 1 will invoke methods on filter 2.
 */
public class BridgeBuilder<S, F>
{
    private final Logger logger;

    private final Class<S> serviceInterface;

    private final Class<F> filterInterface;

    private final FilterMethodAnalyzer filterMethodAnalyzer;

    private final PlasticProxyFactory proxyFactory;

    private ClassInstantiator<S> instantiator;

    public BridgeBuilder(Logger logger, Class<S> serviceInterface, Class<F> filterInterface, PlasticProxyFactory proxyFactory)
    {
        this.logger = logger;
        this.serviceInterface = serviceInterface;
        this.filterInterface = filterInterface;

        this.proxyFactory = proxyFactory;

        filterMethodAnalyzer = new FilterMethodAnalyzer(serviceInterface);
    }

    /**
     * Instantiates a bridge object.
     *
     * @param nextBridge
     *         the next Bridge object in the pipeline, or the terminator service
     * @param filter
     *         the filter object for this step of the pipeline
     */
    public S instantiateBridge(S nextBridge, F filter)
    {
        if (instantiator == null)
            createInstantiator();

        return instantiator.with(filterInterface, filter).with(serviceInterface, nextBridge).newInstance();
    }

    private void createInstantiator()
    {
        instantiator = proxyFactory.createProxy(serviceInterface, new PlasticClassTransformer()
        {
            @Override
            public void transform(PlasticClass plasticClass)
            {
                PlasticField filterField = plasticClass.introduceField(filterInterface, "filter")
                        .injectFromInstanceContext();
                PlasticField nextField = plasticClass.introduceField(serviceInterface, "next")
                        .injectFromInstanceContext();

                processMethods(plasticClass, filterField, nextField);

                plasticClass.addToString(String.format("<PipelineBridge from %s to %s>", serviceInterface.getName(),
                        filterInterface.getName()));
            }
        });
    }

    private void processMethods(PlasticClass plasticClass, PlasticField filterField, PlasticField nextField)
    {
        List<MethodSignature> serviceMethods = CollectionFactory.newList();
        List<MethodSignature> filterMethods = CollectionFactory.newList();

        MethodIterator mi = new MethodIterator(serviceInterface);

        while (mi.hasNext())
        {
            serviceMethods.add(mi.next());
        }

        mi = new MethodIterator(filterInterface);

        while (mi.hasNext())
        {
            filterMethods.add(mi.next());
        }

        while (!serviceMethods.isEmpty())
        {
            MethodSignature ms = serviceMethods.remove(0);

            addBridgeMethod(plasticClass, filterField, nextField, ms, filterMethods);
        }

        reportExtraFilterMethods(filterMethods);
    }

    private void reportExtraFilterMethods(List filterMethods)
    {
        Iterator i = filterMethods.iterator();

        while (i.hasNext())
        {
            MethodSignature ms = (MethodSignature) i.next();

            logger.error("Method {} of filter interface {} does not have a matching method in {}.", ms, filterInterface.getName(), serviceInterface.getName());
        }
    }

    /**
     * Finds a matching method in filterMethods for the given service method. A matching method has the same signature
     * as the service interface method, but with an additional parameter matching the service interface itself.
     *
     * The matching method signature from the list of filterMethods is removed and code generation strategies for making
     * the two methods call each other are added.
     */
    private void addBridgeMethod(PlasticClass plasticClass, PlasticField filterField, PlasticField nextField,
                                 final MethodSignature ms, List filterMethods)
    {
        PlasticMethod method = plasticClass.introduceMethod(ms.getMethod());

        Iterator i = filterMethods.iterator();

        while (i.hasNext())
        {
            MethodSignature fms = (MethodSignature) i.next();

            int position = filterMethodAnalyzer.findServiceInterfacePosition(ms, fms);

            if (position >= 0)
            {
                bridgeServiceMethodToFilterMethod(method, filterField, nextField, position, ms, fms);
                i.remove();
                return;
            }
        }

        method.changeImplementation(new InstructionBuilderCallback()
        {
            @Override
            public void doBuild(InstructionBuilder builder)
            {
                String message = String.format("Method %s has no match in filter interface %s.", ms, filterInterface.getName());

                logger.error(message);

                builder.throwException(RuntimeException.class, message);
            }
        });
    }

    private void bridgeServiceMethodToFilterMethod(PlasticMethod method, final PlasticField filterField,
                                                   final PlasticField nextField, final int position, MethodSignature ms, final MethodSignature fms)
    {
        method.changeImplementation(new InstructionBuilderCallback()
        {
            @Override
            public void doBuild(InstructionBuilder builder)
            {
                builder.loadThis().getField(filterField);

                int argumentIndex = 0;

                for (int i = 0; i < fms.getParameterTypes().length; i++)
                {
                    if (i == position)
                    {
                        builder.loadThis().getField(nextField);
                    } else
                    {
                        builder.loadArgument(argumentIndex++);
                    }
                }

                builder.invoke(fms.getMethod()).returnResult();
            }
        });
    }

}
