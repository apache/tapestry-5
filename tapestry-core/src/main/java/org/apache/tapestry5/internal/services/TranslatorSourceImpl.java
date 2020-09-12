// Copyright 2007, 2008, 2010, 2011, 2012 The Apache Software Foundation
//
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Translator;
import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.commons.util.AvailableValues;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.commons.util.StrategyRegistry;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.ioc.annotations.ComponentClasses;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.plastic.PlasticUtils;
import org.apache.tapestry5.services.TranslatorSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class TranslatorSourceImpl implements TranslatorSource, Runnable
{
    private final Map<String, Translator> nameToTranslator = CollectionFactory.newCaseInsensitiveMap();

    private final StrategyRegistry<Translator> registry;

    private static final Map<String, Translator> EMPTY = Collections.emptyMap();

    public TranslatorSourceImpl(Map<Class, Translator> configuration)
    {
        this(configuration, EMPTY);
    }

    public TranslatorSourceImpl(Map<Class, Translator> configuration, Map<String, Translator> alternates)
    {
        for (Map.Entry<Class, Translator> me : configuration.entrySet())
        {
            Class type = me.getKey();
            Translator translator = me.getValue();

            if (!type.equals(translator.getType()))
                throw new RuntimeException(
                        String
                                .format(
                                        "Contributed translator for type %s reports its type as %s. Please change the contribution so that the key matches that translator type.",
                                        type.getName(), translator.getType().getName()));

            String name = translator.getName();

            if (nameToTranslator.containsKey(name))
                throw new RuntimeException(
                        String
                                .format(
                                        "Two different Translators contributed to the TranslatorSource service use the same translator name: '%s'.  Translator names must be unique.",
                                        name));

            nameToTranslator.put(name, translator);
        }

        for (String name : alternates.keySet())
        {
            if (nameToTranslator.containsKey(name))
                throw new RuntimeException(
                        String
                                .format(
                                        "Translator '%s' contributed to the TranslatorAlternatesSource service has the same name as a standard Translator contributed to the TranslatorSource service.",
                                        name));

            nameToTranslator.put(name, alternates.get(name));
        }

        registry = StrategyRegistry.newInstance(Translator.class, configuration, true);
    }

public Translator get(String name)
    {
        Translator result = nameToTranslator.get(name);

        if (result == null)
            throw new UnknownValueException(String.format("Unknown translator type '%s'.", name), new AvailableValues(
                    "Configured translators", nameToTranslator));

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

            throw new IllegalArgumentException(String.format("No translator is defined for type %s.  Registered types: %s.", PlasticUtils.toTypeName(valueType), InternalUtils
                    .joinSorted(names)));
        }

        return result;
    }

    public Translator findByType(Class valueType)
    {
        return registry.get(valueType);
    }

    /**
     * Invoked by InvalidationEventHub
     */
    public void run()
    {
        registry.clearCache();
    }
}
