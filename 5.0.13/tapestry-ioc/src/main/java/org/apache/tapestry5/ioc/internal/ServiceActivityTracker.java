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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.def.ServiceDef;
import org.apache.tapestry5.ioc.services.ServiceActivityScoreboard;
import org.apache.tapestry5.ioc.services.Status;

/**
 * Used to update the status of services defined by the {@link ServiceActivityScoreboard}.
 */
public interface ServiceActivityTracker
{

    /**
     * Defines a service in the tracker with an initial status.
     *
     * @param serviceDef    the service being defined
     * @param initialStatus typically {@link Status#BUILTIN} or {@link Status#DEFINED}
     */
    void define(ServiceDef serviceDef, Status initialStatus);

    /**
     * Updates the status for the service.
     *
     * @param serviceId identifies the service, which must be previously defined
     * @param status    the new status value
     */
    void setStatus(String serviceId, Status status);
}
