// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.ioc.def.ContributionDef;
import org.apache.tapestry5.ioc.def.DecoratorDef;
import org.apache.tapestry5.ioc.def.ModuleDef;
import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.util.Collections;
import java.util.Set;

/**
 * A synthetic module definition, used to mix in some additional "pre-built" service configuration contributions.
 */
public class SyntheticModuleDef implements ModuleDef
{
    private final Set<ContributionDef> contributionDefs;

    public SyntheticModuleDef(ContributionDef... contributionDefs)
    {
        this.contributionDefs = CollectionFactory.newSet(contributionDefs);
    }

    /**
     * Returns null.
     */
    public Class getBuilderClass()
    {
        return null;
    }

    /**
     * Returns the configured set.
     */
    public Set<ContributionDef> getContributionDefs()
    {
        return contributionDefs;
    }

    /**
     * Returns an empty set.
     */
    public Set<DecoratorDef> getDecoratorDefs()
    {
        return Collections.emptySet();
    }

    /**
     * Returns "SyntheticModule".
     */
    public String getLoggerName()
    {
        return "SyntheticModule";
    }

    /**
     * Returns null.
     */
    public ServiceDef getServiceDef(String serviceId)
    {
        return null;
    }

    /**
     * Returns an empty set.
     */
    public Set<String> getServiceIds()
    {
        return Collections.emptySet();
    }
}
