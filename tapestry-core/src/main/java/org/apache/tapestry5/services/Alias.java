// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.ObjectProvider;

/**
 * The Alias service provides an ObjectProvider that fits into the MasterObjectProvider command chain and disambiguates
 * injections based on type. {@linkplain org.apache.tapestry5.services.AliasContribution Contibutions} to the Alias
 * service identify the desired service to inject for a particular service interface; this is only necessary when there
 * is more than one service implementing the same interface.
 * <p/>
 * The {@linkplain AliasManager AliasOverrides} service also takes an unordered configuration of {@link
 * org.apache.tapestry5.services.AliasContribution}; such contributions override the "factory" contributions to the Alias
 * service itself.  This is often used to replace built-in service implementations with ones that are specific to a
 * particular application.
 */
public interface Alias
{
    /**
     * Returns an object provideer that checks the desired type against the service's contributions.
     */
    ObjectProvider getObjectProvider();
}
