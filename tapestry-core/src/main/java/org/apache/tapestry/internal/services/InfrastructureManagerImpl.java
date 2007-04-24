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

package org.apache.tapestry.internal.services;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.services.InfrastructureContribution;
import org.apache.tapestry.services.InfrastructureManager;

/**
 * 
 */
public class InfrastructureManagerImpl implements InfrastructureManager
{
    private final Log _log;

    private final Collection<InfrastructureContribution> _contributions;

    public InfrastructureManagerImpl(Log log, Collection<InfrastructureContribution> contributions)
    {
        _log = log;
        _contributions = contributions;
    }

    public Map<String, Object> getContributionsForMode(String mode)
    {
        Map<String, Object> general = buildMapForMode("");
        Map<String, Object> specific = buildMapForMode(mode);

        // Anything in specific overrides anything in general

        general.putAll(specific);

        return general;
    }

    private Map<String, Object> buildMapForMode(String mode)
    {
        Map<String, Object> result = CollectionFactory.newMap();

        for (InfrastructureContribution ic : _contributions)
        {
            if (!ic.getMode().equals(mode))
                continue;

            String name = ic.getName();

            Object existing = result.get(name);

            if (existing != null)
            {
                _log.warn(ServicesMessages.duplicateContribution(ic.getObject(), name, existing));
                continue;
            }

            result.put(name, ic.getObject());
        }

        return result;
    }

}
