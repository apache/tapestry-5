// Copyright 2007 The Apache Software Foundation
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

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ValueEncoder;
import org.apache.tapestry.internal.events.InvalidationListener;
import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;
import org.apache.tapestry.ioc.util.StrategyRegistry;
import org.apache.tapestry.services.ValueEncoderFactory;
import org.apache.tapestry.services.ValueEncoderSource;

import java.util.Map;

public class ValueEncoderSourceImpl implements ValueEncoderSource, InvalidationListener
{
    private final StrategyRegistry<ValueEncoderFactory> _registry;

    public ValueEncoderSourceImpl(Map<Class, ValueEncoderFactory> configuration)
    {
        _registry = StrategyRegistry.newInstance(ValueEncoderFactory.class, configuration);
    }

    @SuppressWarnings("unchecked")
    public ValueEncoder createEncoder(String parameterName, ComponentResources resources)
    {
        notBlank(parameterName, "parameterName");
        notNull(resources, "resources");

        Class parameterType = resources.getBoundType(parameterName);

        if (parameterType == null) return null;

        ValueEncoderFactory factory = _registry.get(parameterType);

        return factory.create(parameterType);
    }

    public void objectWasInvalidated()
    {
        _registry.clearCache();
    }
}
