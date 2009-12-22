// Copyright 2006, 2007 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.def.ContributionDef;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Provides two forms of validation for mapped configurations: <ul> <li>If either key or value is null, then a warning
 * is logged </li> <li>If the key has previously been stored (by some other {@link
 * org.apache.tapestry5.ioc.def.ContributionDef}, then a warning is logged</li> </ul>
 * <p/>
 * When a warning is logged, the key/value pair is not added to the delegate.
 *
 * @param <K>
 * @param <V>
 */
public class ValidatingMappedConfigurationWrapper<K, V> implements MappedConfiguration<K, V>
{
    private final String serviceId;

    private final ContributionDef contributionDef;

    private final Logger logger;

    private final Class<K> expectedKeyType;

    private final Class<V> expectedValueType;

    private final Map<K, ContributionDef> keyToContributor;

    private final MappedConfiguration<K, V> delegate;

    public ValidatingMappedConfigurationWrapper(String serviceId, ContributionDef contributionDef,
                                                Logger logger, Class<K> expectedKeyType, Class<V> expectedValueType,
                                                Map<K, ContributionDef> keyToContributor,
                                                MappedConfiguration<K, V> delegate)
    {
        this.serviceId = serviceId;
        this.contributionDef = contributionDef;
        this.logger = logger;
        this.expectedKeyType = expectedKeyType;
        this.expectedValueType = expectedValueType;
        this.keyToContributor = keyToContributor;
        this.delegate = delegate;
    }

    public void add(K key, V value)
    {
        if (key == null)
        {
            logger.warn(IOCMessages.contributionKeyWasNull(serviceId, contributionDef));
            return;
        }

        if (value == null)
        {
            logger.warn(IOCMessages.contributionWasNull(serviceId, contributionDef));
            return;
        }

        if (!expectedKeyType.isInstance(key))
        {
            logger.warn(IOCMessages.contributionWrongKeyType(serviceId, contributionDef, key
                    .getClass(), expectedKeyType));
            return;
        }

        if (!expectedValueType.isInstance(value))
        {
            logger.warn(IOCMessages.contributionWrongValueType(serviceId, contributionDef, value
                    .getClass(), expectedValueType));
            return;
        }

        ContributionDef existing = keyToContributor.get(key);

        if (existing != null)
        {
            logger.warn(IOCMessages.contributionDuplicateKey(
                    serviceId,
                    contributionDef,
                    existing));
            return;
        }

        delegate.add(key, value);

        // Remember that this key is provided by this contribution, when looking
        // for future conflicts.

        keyToContributor.put(key, contributionDef);
    }

}
