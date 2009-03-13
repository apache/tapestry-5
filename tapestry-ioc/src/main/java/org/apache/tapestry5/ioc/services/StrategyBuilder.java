// Copyright 2006, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.util.StrategyRegistry;

import java.util.Map;

/**
 * A service implementation builder that operates around a {@link StrategyRegistry}, implementing a version of the Gang
 * of Four Strategy pattern.
 * <p/>
 * The constructed service is configured with a number of adapters (that implement the same service interface). Method
 * invocations on the service are routed to one of the adapters.
 * <p/>
 * The first parameter of each method is used to select the appropriate adapter.
 * <p/>
 * The ideal interface for use with this builder has only one method.
 */
public interface StrategyBuilder
{
    /**
     * Given a number of adapters implementing the service interface, builds a "dispatcher" implementations that
     * delegates to the one of the adapters. It is an error if any of the methods takes no parameters.
     *
     * @param <S>      the service interface type
     * @param registry defines the adapters based on parameter type (of the first parameter)
     * @return a service implementation
     */
    <S> S build(StrategyRegistry<S> registry);

    /**
     * @param registrations map frm class to the adapter type
     * @param <S>
     * @return the dispatcher
     * @since 5.1.0.0
     */
    <S> S build(Class<S> adapterType, Map<Class, S> registrations);
}
