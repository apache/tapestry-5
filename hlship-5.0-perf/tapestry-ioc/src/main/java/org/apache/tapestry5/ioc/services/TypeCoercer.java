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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.annotations.UsesConfiguration;

/**
 * Makes use of {@link org.apache.tapestry5.ioc.services.Coercion}s  to convert between an input value (of some specific
 * type) and a desired output type. Smart about coercing, even if it requires multiple coercion steps (i.e., via an
 * intermediate type, such as String).
 */
@UsesConfiguration(CoercionTuple.class)
public interface TypeCoercer
{
    /**
     * Performs a coercion from an input type to a desired output type. When the target type is a primitive, the actual
     * conversion will be to the equivalent wrapper type. In some cases, the TypeCoercer will need to search for an
     * appropriate coercion, and may even combine existing coercions to form new ones; in those cases, the results of
     * the search are cached.
     * <p/>
     * <p/>
     * The TypeCoercer also caches the results of a coercion search.
     *
     * @param <S>        source type (input)
     * @param <T>        target type (output)
     * @param input
     * @param targetType defines the target type
     * @return the coerced value
     */
    <S, T> T coerce(S input, Class<T> targetType);

    /**
     * Used primarily inside test suites, this method performs the same steps as {@link #coerce(Object, Class)}, but
     * returns a string describing the series of coercision, such as "Object --&gt; String --&gt; Long --&gt; Integer".
     *
     * @param <S>        source type (input)
     * @param <T>        target type (output)
     * @param inputType  the source coercion type (use void.class for coercions from null)
     * @param targetType defines the target type
     * @return a string identifying the series of coercions, or the empty string if no coercion is necessary
     */
    <S, T> String explain(Class<S> inputType, Class<T> targetType);

    /**
     * Clears cached information stored by the TypeCoercer.
     */
    void clearCache();
}
