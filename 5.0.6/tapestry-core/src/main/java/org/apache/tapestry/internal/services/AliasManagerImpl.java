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

package org.apache.tapestry.internal.services;

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.util.Collection;
import java.util.Map;

import org.apache.tapestry.services.AliasContribution;
import org.apache.tapestry.services.AliasManager;
import org.slf4j.Logger;

public class AliasManagerImpl implements AliasManager
{
    private final Logger _logger;

    private final Collection<AliasContribution> _contributions;

    public AliasManagerImpl(Logger logger, Collection<AliasContribution> contributions)
    {
        _logger = logger;
        _contributions = contributions;
    }

    public Map<Class, Object> getAliasesForMode(String mode)
    {
        Map<Class, Object> general = buildMapForMode("");
        Map<Class, Object> specific = buildMapForMode(mode);

        // Anything in specific overrides anything in general

        general.putAll(specific);

        return general;
    }

    private Map<Class, Object> buildMapForMode(String mode)
    {
        Map<Class, Object> result = newMap();

        for (AliasContribution ic : _contributions)
        {
            if (!ic.getMode().equalsIgnoreCase(mode)) continue;

            Class contributionType = ic.getContributionType();

            Object existing = result.get(contributionType);

            if (existing != null)
            {
                _logger.error(ServicesMessages.duplicateContribution(
                        ic.getObject(),
                        contributionType,
                        existing));
                continue;
            }

            result.put(contributionType, ic.getObject());
        }

        return result;
    }

}
