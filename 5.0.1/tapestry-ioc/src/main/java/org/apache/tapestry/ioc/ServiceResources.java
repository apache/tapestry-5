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

package org.apache.tapestry.ioc;

import org.apache.commons.logging.Log;

/**
 * Contains resources that may be provided to a service when it initializes, which includes other
 * services visible to the service. ServiceResources provides access to other services (it extends
 * {@link org.apache.tapestry.ioc.ServiceLocator}), however service ids (when used with
 * {@link org.apache.tapestry.ioc.ServiceLocator#getService(String, Class)}) may be unqualified (to
 * access another service within the same containing module). Futher,
 * {@link org.apache.tapestry.ioc.ServiceLocator#getService(Class)} will take into account private
 * services visible only within the module, as well as public services from both the containing
 * module and from other modules.
 * 
 * 
 */
public interface ServiceResources extends ServiceLocator
{
    /** Returns the fully qualified id of the service. */
    String getServiceId();

    /** Returns the service interface implemented by the service. */
    Class getServiceInterface();

    /**
     * Returns a Log object appropriate for logging messages. This includes debug level messages
     * about the creation and configuration of the underlying service, as well as debug, warning, or
     * error level messages from the service itself. Often service interceptors will make use of the
     * service's log.
     */
    Log getServiceLog();
}
