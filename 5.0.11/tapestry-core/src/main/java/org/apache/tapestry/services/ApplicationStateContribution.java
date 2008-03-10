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

package org.apache.tapestry.services;

import org.apache.tapestry.ioc.internal.util.Defense;

/**
 * A contribution to the configuration of the {@link ApplicationStateManager}, identifying the strategy and creator for
 * a particular ASO (identified by the ASO's class).
 */
public final class ApplicationStateContribution
{
    private final String _strategy;

    private final ApplicationStateCreator _creator;

    public ApplicationStateContribution(String strategy)
    {
        this(strategy, null);
    }

    public ApplicationStateContribution(String strategy, ApplicationStateCreator creator)
    {
        Defense.notBlank(strategy, "strategy");

        _strategy = strategy;
        _creator = creator;
    }

    /**
     * The creator for the ASO. If null, the the ASO is created directly from the ASO class, via its public no-arguments
     * constructor.
     */
    public ApplicationStateCreator getCreator()
    {
        return _creator;
    }

    /**
     * The name of the strategy used to control where the ASO is stored.
     */
    public String getStrategy()
    {
        return _strategy;
    }

}
