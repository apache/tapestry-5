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

package org.apache.tapestry5.commons.benchmarks;

import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.services.CoercionTuple;

/**
 * Collects coercion contributions into a plain map, so that benchmarks can build a
 * {@link org.apache.tapestry5.commons.internal.services.TypeCoercerImpl} without an IoC registry.
 *
 * Mirrors BeanModelSourceBuilder.CoercionTupleConfiguration, which is private.
 */
@SuppressWarnings("rawtypes")
public class CoercionTupleCollector implements MappedConfiguration<CoercionTuple.Key, CoercionTuple>
{
    private final Map<CoercionTuple.Key, CoercionTuple> tuples = new HashMap<>();

    @Override
    public void add(CoercionTuple.Key key, CoercionTuple value)
    {
        tuples.put(key, value);
    }

    @Override
    public void override(CoercionTuple.Key key, CoercionTuple value)
    {
        tuples.put(key, value);
    }

    @Override
    public void addInstance(CoercionTuple.Key key, Class<? extends CoercionTuple> clazz)
    {
        throw new UnsupportedOperationException("Not needed by benchmarks.");
    }

    @Override
    public void overrideInstance(CoercionTuple.Key key, Class<? extends CoercionTuple> clazz)
    {
        throw new UnsupportedOperationException("Not needed by benchmarks.");
    }

    public Map<CoercionTuple.Key, CoercionTuple> getTuples()
    {
        return tuples;
    }
}
