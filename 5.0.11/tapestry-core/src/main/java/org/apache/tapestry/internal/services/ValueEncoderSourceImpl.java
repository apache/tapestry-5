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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ValueEncoder;
import org.apache.tapestry.internal.events.InvalidationListener;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.ioc.util.StrategyRegistry;
import org.apache.tapestry.services.ValueEncoderFactory;
import org.apache.tapestry.services.ValueEncoderSource;

import java.util.Map;

public class ValueEncoderSourceImpl implements ValueEncoderSource, InvalidationListener
{
    private final StrategyRegistry<ValueEncoderFactory> _registry;

    private final Map<Class, ValueEncoder> _cache = CollectionFactory.newConcurrentMap();

    public ValueEncoderSourceImpl(Map<Class, ValueEncoderFactory> configuration)
    {
        _registry = StrategyRegistry.newInstance(ValueEncoderFactory.class, configuration);
    }

    @SuppressWarnings({ "unchecked" })
    public <T> ValueEncoder<T> getValueEncoder(Class<T> type)
    {
        Defense.notNull(type, "type");

        ValueEncoder<T> result = _cache.get(type);

        if (result == null)
        {
            ValueEncoderFactory<T> factory = _registry.get(type);

            result = factory.create(type);

            _cache.put(type, result);
        }

        return result;
    }

    public void objectWasInvalidated()
    {
        _registry.clearCache();
        _cache.clear();
    }
}
