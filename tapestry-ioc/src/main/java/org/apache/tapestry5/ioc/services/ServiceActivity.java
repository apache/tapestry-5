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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.def.ServiceDef;

/**
 * Provided by the {@link ServiceActivityScoreboard} to track a single service's state and activity.
 *
 * @see ServiceDef
 */
public interface ServiceActivity
{
    /**
     * The unique id for the service.
     */
    String getServiceId();

    /**
     * The interface implemented by the service (this may occasionally be a class, for non-proxied services).
     */
    Class getServiceInterface();

    /**
     * The scope of the service (typically "singleton" or "perthread").
     */
    String getScope();

    /**
     * Indicates the lifecycle status of the service.
     */
    Status getStatus();
}
