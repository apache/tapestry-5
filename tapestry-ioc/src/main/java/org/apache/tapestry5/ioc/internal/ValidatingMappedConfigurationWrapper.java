// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.def.ContributionDef;

import java.util.Map;

/**
 * A wrapper around a Map that provides the {@link org.apache.tapestry5.ioc.MappedConfiguration} interface, and provides
 * two forms of validation for mapped configurations: <ul> <li>If either key or value is null, then a warning is logged
 * </li> <li>If the key has previously been stored (by some other {@link org.apache.tapestry5.ioc.def.ContributionDef},
 * then a warning is logged</li> </ul>
 * <p/>
 * When a warning is logged, the key/value pair is not added to the delegate.
 * <p/>
 * Handles instantiation of instances.
 *
 * @param <K>
 * @param <V>
 */
public class ValidatingMappedConfigurationWrapper<K, V> implements MappedConfiguration<K, V>
{
    private final Map<K, V> map;

    private final Map<K, MappedConfigurationOverride<K, V>> overrides;

    private final String serviceId;

    private final ContributionDef contributionDef;

    private final Class<K> expectedKeyType;

    private final Class<V> expectedValueType;

    private final Map<K, ContributionDef> keyToContributor;

    private final ObjectLocator locator;

    public ValidatingMappedConfigurationWrapper(Map<K, V> map, Map<K, MappedConfigurationOverride<K, V>> overrides,
                                                String serviceId, ContributionDef contributionDef,
                                                Class<K> expectedKeyType, Class<V> expectedValueType,
                                                Map<K, ContributionDef> keyToContributor,
                                                ObjectLocator locator)
    {
        this.map = map;
        this.overrides = overrides;
        this.serviceId = serviceId;
        this.contributionDef = contributionDef;
        this.expectedKeyType = expectedKeyType;
        this.expectedValueType = expectedValueType;
        this.keyToContributor = keyToContributor;
        this.locator = locator;
    }

    public void add(K key, V value)
    {
        validateKey(key);

        if (value == null)
            throw new NullPointerException(IOCMessages.contributionWasNull(serviceId));


        validateValue(value);

        ContributionDef existing = keyToContributor.get(key);

        if (existing != null)
            throw new IllegalArgumentException(IOCMessages.contributionDuplicateKey(serviceId, existing));

        map.put(key, value);

        // Remember that this key is provided by this contribution, when looking
        // for future conflicts.

        keyToContributor.put(key, contributionDef);
    }

    private void validateValue(V value)
    {
        if (!expectedValueType.isInstance(value))
            throw new IllegalArgumentException(IOCMessages.contributionWrongValueType(serviceId, value
                    .getClass(), expectedValueType));
    }

    private void validateKey(K key)
    {
        if (key == null)
            throw new NullPointerException(IOCMessages.contributionKeyWasNull(serviceId));

        if (!expectedKeyType.isInstance(key))
            throw new IllegalArgumentException(
                    IOCMessages.contributionWrongKeyType(serviceId, key
                            .getClass(), expectedKeyType));
    }

    public void addInstance(K key, Class<? extends V> clazz)
    {
        V value = locator.autobuild(clazz);

        add(key, value);
    }

    public void override(K key, V value)
    {
        validateKey(key);

        if (value != null) validateValue(value);

        MappedConfigurationOverride<K, V> existing = overrides.get(key);

        if (existing != null)
            throw new IllegalArgumentException(
                    String.format("Contribution key %s has already been overridden (by %s).",
                                  key, existing.getContribDef()));


        overrides.put(key, new MappedConfigurationOverride<K, V>(contributionDef, map, key, value));
    }


    public void overrideInstance(K key, Class<? extends V> clazz)
    {
        override(key, locator.autobuild(clazz));
    }
}
