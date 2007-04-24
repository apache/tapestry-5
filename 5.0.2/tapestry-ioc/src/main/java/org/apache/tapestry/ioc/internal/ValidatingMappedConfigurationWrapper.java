// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.MappedConfiguration;
import org.apache.tapestry.ioc.def.ContributionDef;

/**
 * Provides two forms of validation for mapped configurations:
 * <ul>
 * <li>If either key or value is null, then a warning is logged </li>
 * <li>If the key has previously been stored (by some other
 * {@link org.apache.tapestry.ioc.def.ContributionDef}, then a warning is logged</li>
 * </ul>
 * <p>
 * When a warning is logged, the key/value pair is not added to the delegate.
 * 
 * @param <K>
 * @param <V>
 */
public class ValidatingMappedConfigurationWrapper<K, V> implements MappedConfiguration<K, V>
{
    private final String _serviceId;

    private final ContributionDef _contributionDef;

    private final Log _log;

    private final Class<K> _expectedKeyType;

    private final Class<V> _expectedValueType;

    private final Map<K, ContributionDef> _keyToContributor;

    private final MappedConfiguration<K, V> _delegate;

    public ValidatingMappedConfigurationWrapper(String serviceId, ContributionDef contributionDef,
            Log log, Class<K> expectedKeyType, Class<V> expectedValueType,
            Map<K, ContributionDef> keyToContributor, MappedConfiguration<K, V> delegate)
    {
        _serviceId = serviceId;
        _contributionDef = contributionDef;
        _log = log;
        _expectedKeyType = expectedKeyType;
        _expectedValueType = expectedValueType;
        _keyToContributor = keyToContributor;
        _delegate = delegate;
    }

    public void add(K key, V value)
    {
        if (key == null)
        {
            _log.warn(IOCMessages.contributionKeyWasNull(_serviceId, _contributionDef));
            return;
        }

        if (value == null)
        {
            _log.warn(IOCMessages.contributionWasNull(_serviceId, _contributionDef));
            return;
        }

        if (!_expectedKeyType.isInstance(key))
        {
            _log.warn(IOCMessages.contributionWrongKeyType(_serviceId, _contributionDef, key
                    .getClass(), _expectedKeyType));
            return;
        }

        if (!_expectedValueType.isInstance(value))
        {
            _log.warn(IOCMessages.contributionWrongValueType(_serviceId, _contributionDef, value
                    .getClass(), _expectedValueType));
            return;
        }

        ContributionDef existing = _keyToContributor.get(key);

        if (existing != null)
        {
            _log.warn(IOCMessages.contributionDuplicateKey(_serviceId, _contributionDef, existing));
            return;
        }

        _delegate.add(key, value);

        // Remember that this key is provided by this contribution, when looking
        // for future conflicts.

        _keyToContributor.put(key, _contributionDef);
    }

}
