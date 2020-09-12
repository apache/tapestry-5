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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.ioc.def.ContributionDef;

import java.util.Map;

/**
 * A wrapper around a Map that provides the {@link org.apache.tapestry5.commons.MappedConfiguration} interface, and provides
 * two forms of validation for mapped configurations:
 * <ul>
 * <li>If either key or value is null, then a warning is logged</li>
 * <li>If the key has previously been stored (by some other {@link org.apache.tapestry5.ioc.def.ContributionDef}, then a
 * warning is logged</li>
 * </ul>
 *
 * When a warning is logged, the key/value pair is not added to the delegate.
 *
 * Handles instantiation of instances.
 * 
 * @param <K> the key type
 * @param <V> the value type
 */
public class ValidatingMappedConfigurationWrapper<K, V> extends AbstractConfigurationImpl<V> implements
        MappedConfiguration<K, V>
{
    private final TypeCoercerProxy typeCoercer;

    private final Map<K, V> map;

    private final Map<K, MappedConfigurationOverride<K, V>> overrides;

    private final String serviceId;

    private final ContributionDef contributionDef;

    private final Class<K> expectedKeyType;

    private final Class<V> expectedValueType;

    private final Map<K, ContributionDef> keyToContributor;

    public ValidatingMappedConfigurationWrapper(Class<V> expectedValueType, ObjectLocator locator,
            TypeCoercerProxy typeCoercer, Map<K, V> map, Map<K, MappedConfigurationOverride<K, V>> overrides,
            String serviceId, ContributionDef contributionDef, Class<K> expectedKeyType,
            Map<K, ContributionDef> keyToContributor)
    {
        super(expectedValueType, locator);

        this.typeCoercer = typeCoercer;
        this.map = map;
        this.overrides = overrides;
        this.serviceId = serviceId;
        this.contributionDef = contributionDef;
        this.expectedKeyType = expectedKeyType;
        this.expectedValueType = expectedValueType;
        this.keyToContributor = keyToContributor;
    }

    @Override
    public void add(K key, V value)
    {
        validateKey(key);

        if (value == null)
            throw new NullPointerException(IOCMessages.contributionWasNull(serviceId));

        V coerced = typeCoercer.coerce(value, expectedValueType);

        ContributionDef existing = keyToContributor.get(key);

        if (existing != null)
            throw new IllegalArgumentException(IOCMessages.contributionDuplicateKey(serviceId, key, existing));

        map.put(key, coerced);

        // Remember that this key is provided by this contribution, when looking
        // for future conflicts.

        keyToContributor.put(key, contributionDef);
    }

    private void validateKey(K key)
    {
        if (key == null)
            throw new NullPointerException(IOCMessages.contributionKeyWasNull(serviceId));

        // Key types don't get coerced; not worth the effort, keys are almost always String or Class
        // anyway.

        if (!expectedKeyType.isInstance(key))
            throw new IllegalArgumentException(IOCMessages.contributionWrongKeyType(serviceId, key.getClass(),
                    expectedKeyType));
    }

    @Override
    public void addInstance(K key, Class<? extends V> clazz)
    {
        add(key, instantiate(clazz));
    }

    @Override
    public void override(K key, V value)
    {
        validateKey(key);

        V coerced = value == null ? null : typeCoercer.coerce(value, expectedValueType);

        MappedConfigurationOverride<K, V> existing = overrides.get(key);

        if (existing != null)
            throw new IllegalArgumentException(String.format(
                    "Contribution key %s has already been overridden (by %s).", key, existing.getContribDef()));

        overrides.put(key, new MappedConfigurationOverride<K, V>(contributionDef, map, key, coerced));
    }

    @Override
    public void overrideInstance(K key, Class<? extends V> clazz)
    {
        override(key, instantiate(clazz));
    }
}
