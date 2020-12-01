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

package org.apache.tapestry5.commons.services;

import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Makes use of {@link org.apache.tapestry5.commons.services.Coercion}s to convert between an input value (of some specific
 * type) and a desired output type. Smart about coercing, even if it requires multiple coercion steps (i.e., via an
 * intermediate type, such as String).
 */
@UsesMappedConfiguration(key = CoercionTuple.Key.class, value = CoercionTuple.class)
public interface TypeCoercer
{
    /**
     * Performs a coercion from an input type to a desired output type. When the target type is a primitive, the actual
     * conversion will be to the equivalent wrapper type. In some cases, the TypeCoercer will need to search for an
     * appropriate coercion, and may even combine existing coercions to form new ones; in those cases, the results of
     * the search are cached.
     *
     * The TypeCoercer also caches the results of a coercion search.
     * 
     * @param <S>
     *            source type (input)
     * @param <T>
     *            target type (output)
     * @param input
     * @param targetType
     *            defines the target type
     * @return the coerced value
     * @throws RuntimeException
     *             if the input can not be coerced
     */
    <S, T> T coerce(S input, Class<T> targetType);

    /**
     * Given a source and target type, computes the coercion that will be used.
     *
     * Note: holding the returned coercion past the time when {@linkplain #clearCache() the cache is cleared} can cause
     * a memory leak, especially in the context of live reloading (wherein holding a reference to a single class make
     * keep an entire ClassLoader from being reclaimed).
     * 
     * @since 5.2.0
     * @param <S>
     *            source type (input)
     * @param <T>
     *            target type (output)
     * @param sourceType
     *            type to coerce from
     * @param targetType
     *            defines the target type
     * @return the coercion that will ultimately be used
     */
    <S, T> Coercion<S, T> getCoercion(Class<S> sourceType, Class<T> targetType);

    /**
     * Used primarily inside test suites, this method performs the same steps as {@link #coerce(Object, Class)}, but
     * returns a string describing the series of coercions, such as "Object --&gt; String --&gt; Long --&gt; Integer".
     * 
     * @param <S>
     *            source type (input)
     * @param <T>
     *            target type (output)
     * @param sourceType
     *            the source coercion type (use void.class for coercions from null)
     * @param targetType
     *            defines the target type
     * @return a string identifying the series of coercions, or the empty string if no coercion is necessary
     */
    <S, T> String explain(Class<S> sourceType, Class<T> targetType);

    /**
     * Clears cached information stored by the TypeCoercer.
     */
    void clearCache();
}
