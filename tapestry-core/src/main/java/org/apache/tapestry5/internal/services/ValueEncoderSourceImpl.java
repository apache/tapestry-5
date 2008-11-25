// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.util.StrategyRegistry;
import org.apache.tapestry5.services.InvalidationListener;
import org.apache.tapestry5.services.ValueEncoderFactory;
import org.apache.tapestry5.services.ValueEncoderSource;

import java.util.Map;

public class ValueEncoderSourceImpl implements ValueEncoderSource, InvalidationListener
{
    private final StrategyRegistry<ValueEncoderFactory> registry;

    private final Map<Class, ValueEncoder> cache = CollectionFactory.newConcurrentMap();

    public ValueEncoderSourceImpl(Map<Class, ValueEncoderFactory> configuration)
    {
        registry = StrategyRegistry.newInstance(ValueEncoderFactory.class, configuration);
    }

    @SuppressWarnings({"unchecked"})
    public <T> ValueEncoder<T> getValueEncoder(Class<T> type)
    {
        Defense.notNull(type, "type");

        ValueEncoder<T> result = cache.get(type);

        if (result == null)
        {
            ValueEncoderFactory<T> factory = registry.get(type);

            result = factory.create(type);

            cache.put(type, result);
        }

        return result;
    }

    public void objectWasInvalidated()
    {
        registry.clearCache();
        cache.clear();
    }
}
