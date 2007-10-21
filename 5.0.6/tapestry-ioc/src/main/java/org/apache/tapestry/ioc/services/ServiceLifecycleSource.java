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

package org.apache.tapestry.ioc.services;

import org.apache.tapestry.ioc.ServiceLifecycle;

/**
 * Provides access to user defined lifecycles (beyond the two built-in lifecycles: "singleton" and
 * "primitive"). The user defined lifecycles are contributed into the service's configuration.
 * 
 * 
 */
public interface ServiceLifecycleSource
{
    /**
     * Used to locate a configuration lifecycle, by name.
     * 
     * @param lifecycleName
     * @return the named lifecycle, or null if the name is not found
     */
    ServiceLifecycle get(String lifecycleName);
}
