// Copyright 2006, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.ServiceLifecycle;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Provides access to user defined lifecycles (beyond the two built-in lifecycles: "singleton" and "primitive"). The
 * user defined lifecycles are contributed into the service's configuration.
 * <p/>
 * Note that the scope {@linkplain org.apache.tapestry5.ioc.ScopeConstants#DEFAULT default} is special and not a
 * contribution.
 */
@UsesMappedConfiguration(ServiceLifecycle.class)
public interface ServiceLifecycleSource
{
    /**
     * Used to locate a configuration lifecycle, by name.
     *
     * @param scope
     * @return the named lifecycle, or null if the name is not found
     */
    ServiceLifecycle get(String scope);
}
