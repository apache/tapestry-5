// Copyright 2006, 2007, 2011 The Apache Software Foundation
//
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

import java.util.List;

import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.DefaultImplementationBuilder;
import org.apache.tapestry5.ioc.services.PipelineBuilder;
import org.slf4j.Logger;

public class PipelineBuilderImpl implements PipelineBuilder
{

    private final PlasticProxyFactory proxyFactory;

    private final DefaultImplementationBuilder defaultImplementationBuilder;

    public PipelineBuilderImpl(@Builtin
    PlasticProxyFactory proxyFactory,

    DefaultImplementationBuilder defaultImplementationBuilder)
    {
        this.proxyFactory = proxyFactory;
        this.defaultImplementationBuilder = defaultImplementationBuilder;
    }

    @Override
    public <S, F> S build(Logger logger, Class<S> serviceInterface, Class<F> filterInterface, List<F> filters)
    {
        S terminator = defaultImplementationBuilder.createDefaultImplementation(serviceInterface);

        return build(logger, serviceInterface, filterInterface, filters, terminator);
    }

    @Override
    public <S, F> S build(Logger logger, Class<S> serviceInterface, Class<F> filterInterface, List<F> filters,
            S terminator)
    {
        if (filters.isEmpty())
            return terminator;

        BridgeBuilder<S, F> bb = new BridgeBuilder<S, F>(logger, serviceInterface, filterInterface, proxyFactory);

        // The first bridge will point to the terminator.
        // Like service decorators, we work deepest (last)
        // to shallowest (first)

        S next = terminator;
        int count = filters.size();

        for (int i = count - 1; i >= 0; i--)
        {
            F filter = filters.get(i);

            next = bb.instantiateBridge(next, filter);
        }

        return next;
    }

}
