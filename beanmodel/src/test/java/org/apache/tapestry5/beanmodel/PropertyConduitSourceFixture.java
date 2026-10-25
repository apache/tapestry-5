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

package org.apache.tapestry5.beanmodel;

import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.beanmodel.internal.services.PropertyAccessImpl;
import org.apache.tapestry5.beanmodel.internal.services.PropertyConduitSourceImpl;
import org.apache.tapestry5.beanmodel.services.PlasticProxyFactoryImpl;
import org.apache.tapestry5.beanmodel.services.PropertyConduitSource;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.internal.BasicTypeCoercions;
import org.apache.tapestry5.commons.internal.services.StringInternerImpl;
import org.apache.tapestry5.commons.internal.services.TypeCoercerImpl;
import org.apache.tapestry5.commons.services.CoercionTuple;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.slf4j.LoggerFactory;

/**
 * Builds a {@link PropertyConduitSourceImpl} without an IoC registry, with the basic and JSR-310 type coercions.
 */
final class PropertyConduitSourceFixture
{
    private PropertyConduitSourceFixture()
    {
    }

    static PropertyConduitSource create()
    {
        PlasticProxyFactory proxyFactory = new PlasticProxyFactoryImpl(PropertyConduitSourceFixture.class.getClassLoader(),
                LoggerFactory.getLogger(PlasticProxyFactory.class));

        CoercionTupleCollector collector = new CoercionTupleCollector();
        BasicTypeCoercions.provideBasicTypeCoercions(collector);
        BasicTypeCoercions.provideJSR310TypeCoercions(collector);

        return new PropertyConduitSourceImpl(new PropertyAccessImpl(), proxyFactory,
                new TypeCoercerImpl(collector.tuples), new StringInternerImpl());
    }

    @SuppressWarnings("rawtypes")
    private static final class CoercionTupleCollector implements MappedConfiguration<CoercionTuple.Key, CoercionTuple>
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
            throw new UnsupportedOperationException("Not needed by the tests.");
        }

        @Override
        public void overrideInstance(CoercionTuple.Key key, Class<? extends CoercionTuple> clazz)
        {
            throw new UnsupportedOperationException("Not needed by the tests.");
        }
    }
}
