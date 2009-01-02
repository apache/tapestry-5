// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.ModuleBuilderSource;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.services.SymbolProvider;

/**
 * Makes a contribution to the SymbolSource service configuration.
 */
public class SyntheticSymbolSourceContributionDef extends AbstractContributionDef
{
    private final String contributionName;

    private final SymbolProvider provider;

    private final String[] constraints;

    public SyntheticSymbolSourceContributionDef(String contributionName, SymbolProvider provider,
                                                String... constraints)
    {
        this.contributionName = contributionName;
        this.provider = provider;
        this.constraints = constraints;
    }


    @SuppressWarnings("unchecked")
    public void contribute(ModuleBuilderSource moduleSource, ServiceResources resources,
                           OrderedConfiguration configuration)
    {
        configuration.add(contributionName, provider, constraints);
    }


    /**
     * Returns "SymbolSource".
     */
    public String getServiceId()
    {
        return "SymbolSource";
    }
}
