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

import org.apache.tapestry5.Translator;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.StrategyRegistry;
import org.apache.tapestry5.services.InvalidationListener;
import org.apache.tapestry5.services.TranslatorSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TranslatorSourceImpl implements TranslatorSource, InvalidationListener
{
    private final Map<String, Translator> translators = CollectionFactory.newCaseInsensitiveMap();

    private final StrategyRegistry<Translator> registry;

    public TranslatorSourceImpl(Collection<Translator> configuration)
    {
        Map<Class, Translator> typeToTranslator = CollectionFactory.newMap();

        for (Translator t : configuration)
        {
            translators.put(t.getName(), t);
            typeToTranslator.put(t.getType(), t);
        }

        registry = StrategyRegistry.newInstance(Translator.class, typeToTranslator, true);
    }

    public Translator get(String name)
    {

        Translator result = translators.get(name);

        if (result == null)
            throw new RuntimeException(ServicesMessages.unknownTranslatorType(name, InternalUtils
                    .sortedKeys(translators)));

        return result;
    }

    public Translator getByType(Class valueType)
    {
        Translator result = registry.get(valueType);

        if (result == null)
        {
            List<String> names = CollectionFactory.newList();

            for (Class type : registry.getTypes())
            {
                names.add(type.getName());
            }

            throw new IllegalArgumentException(ServicesMessages.noTranslatorForType(valueType, names));
        }

        return result;
    }

    public Translator findByType(Class valueType)
    {
        return registry.get(valueType);
    }

    public void objectWasInvalidated()
    {
        registry.clearCache();
    }
}
