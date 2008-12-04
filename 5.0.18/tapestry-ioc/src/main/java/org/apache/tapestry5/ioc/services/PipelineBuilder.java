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

package org.apache.tapestry5.ioc.services;

import org.slf4j.Logger;

import java.util.List;

/**
 * Creates a pipeline from a service inteface and an ordered list of filters. Each filter is defined in terms of a
 * filter interface: the filter interface is a variant of the service interface, where each method has an additional
 * parameter that is an instance of the service interface. Typically, this service parameter (often named "delegate") is
 * either the first or the last parameter of each method.
 * <p/>
 * The implementation of a filter method is expected to pass all of its parameters to the service instance passed into
 * it.
 * <p/>
 * The interesting thing is that there may be multiple filters in the pipeline. A fabricated "bridge" object (that
 * implements the service interface) is created to let each filter invoke methods on the next filter down the pipeline.
 * This simplifies the model for creating pipelines, as each filter is coded as if it was directly "in front of" the
 * terminator. In fact, it may be indirectly invoking methods on the next filter in the pipeline via a bridge instance.
 * <p/>
 * The builder is fairly smart about matching up service interface methods to filter interface methods, but keeping it
 * simple is also a good idea.
 */
public interface PipelineBuilder
{
    /**
     * Creates a pipeline from the filters and a terminator.
     *
     * @param <S>              service type
     * @param <F>              filter type
     * @param logger           logs any warnings generated when constructing the pipeline
     * @param serviceInterface
     * @param filterInterface
     * @param filters          sorted list of filters
     * @param terminator       end of the pipeline
     * @return an object that encapsulates the filters and the terminator
     */
    <S, F> S build(Logger logger, Class<S> serviceInterface, Class<F> filterInterface, List<F> filters, S terminator);

    /**
     * Creates a pipeline from just the filters. A {@link DefaultImplementationBuilder default implementation} is
     * created as the terminator.
     *
     * @param <S>
     * @param <F>
     * @param logger
     * @param serviceInterface
     * @param filterInterface
     * @param filters
     * @return
     */
    <S, F> S build(Logger logger, Class<S> serviceInterface, Class<F> filterInterface, List<F> filters);

}
