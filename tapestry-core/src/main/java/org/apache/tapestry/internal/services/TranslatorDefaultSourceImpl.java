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

import java.util.Map;

import org.apache.tapestry.Translator;
import org.apache.tapestry.events.InvalidationListener;
import org.apache.tapestry.ioc.util.StrategyRegistry;
import org.apache.tapestry.services.TranslatorDefaultSource;

public class TranslatorDefaultSourceImpl implements TranslatorDefaultSource, InvalidationListener
{
    private final StrategyRegistry<Translator> _registry;

    public TranslatorDefaultSourceImpl(Map<Class, Translator> configuration)
    {
        _registry = StrategyRegistry.newInstance(Translator.class, configuration);
    }

    public Translator find(Class valueType)
    {
        return _registry.get(valueType);
    }

    public void objectWasInvalidated()
    {
        _registry.clearCache();
    }

}
