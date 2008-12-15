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

import org.apache.tapestry5.ioc.annotations.UsesConfiguration;

import java.util.Map;

/**
 * A thin wrapper around a set of {@link org.apache.tapestry5.services.AliasContribution}s. An {@link
 * org.apache.tapestry5.ioc.ObjectProvider} is contributed to the {@link org.apache.tapestry5.ioc.services.MasterObjectProvider}
 * service, to allow contributed objects to replace other objects (typically, built in services).
 */
@UsesConfiguration(AliasContribution.class)
public interface AliasManager
{
    /**
     * Filters down the contributions based on the mode. Each {@link AliasContribution contribution} will identify a
     * contribution type and a non-null object that implements the type and may identify a mode. Only contributions
     * where the mode is blank or the mode matches the provided mode are returned. Mode specific contributions quietly
     * override non-specific contributions (where the mode is blank).
     *
     * @param mode
     * @return map from contribution type to contribution object
     */
    Map<Class, Object> getAliasesForMode(String mode);
}
