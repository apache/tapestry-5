// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.def.ContributionDef;

import java.util.Map;

public class MappedConfigurationOverride<K, V>
{
    private final Map<K, V> configuration;

    private final K key;

    private final V overrideValue;

    private final ContributionDef contribDef;

    public MappedConfigurationOverride(ContributionDef contribDef, Map<K, V> configuration, K key, V overrideValue)
    {
        this.contribDef = contribDef;
        this.configuration = configuration;
        this.key = key;
        this.overrideValue = overrideValue;
    }

    void apply()
    {
        if (!configuration.containsKey(key))
            throw new IllegalArgumentException(
                    String.format("Override for key %s (at %s) does not match an existing key.", key, contribDef));

        if (overrideValue == null)
            configuration.remove(key);
        else
            configuration.put(key, overrideValue);
    }

    public ContributionDef getContribDef()
    {
        return contribDef;
    }
}
