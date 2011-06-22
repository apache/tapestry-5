// Copyright 2010 The Apache Software Foundation
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

import java.util.Map;

import org.apache.tapestry5.Translator;
import org.apache.tapestry5.services.TranslatorAlternatesSource;

@SuppressWarnings("unchecked")
public class TranslatorAlternatesSourceImpl implements TranslatorAlternatesSource
{
    private final Map<String, Translator> configuration;

    public TranslatorAlternatesSourceImpl(Map<String, Translator> configuration)
    {
        this.configuration = configuration;

        for (Map.Entry<String, Translator> me : configuration.entrySet())
        {
            if (!me.getKey().equalsIgnoreCase(me.getValue().getName()))
                throw new RuntimeException(String.format(
                        "Contribution key '%s' does not match '%s' (the name of the corresponding Translator).", me
                                .getKey(), me.getValue().getName()));
        }
    }

    public Map<String, Translator> getTranslatorAlternates()
    {
        return configuration;
    }

}
