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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.internal.util.Defense;

/**
 * A contribution to the configuration of the {@link ApplicationStateManager}, identifying the strategy and creator for
 * a particular Session State Object (SSO), identified by the SSO's class.
 */
public final class ApplicationStateContribution
{
    private final String strategy;

    private final ApplicationStateCreator creator;

    public ApplicationStateContribution(String strategy)
    {
        this(strategy, null);
    }

    public ApplicationStateContribution(String strategy, ApplicationStateCreator creator)
    {
        Defense.notBlank(strategy, "strategy");

        this.strategy = strategy;
        this.creator = creator;
    }

    /**
     * The creator for the ASO. If null, the the ASO is created directly from the ASO class, via its public no-arguments
     * constructor.
     */
    public ApplicationStateCreator getCreator()
    {
        return creator;
    }

    /**
     * The name of the strategy used to control where the ASO is stored.
     */
    public String getStrategy()
    {
        return strategy;
    }

}
