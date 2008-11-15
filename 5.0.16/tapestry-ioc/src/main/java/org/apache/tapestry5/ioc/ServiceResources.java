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

package org.apache.tapestry5.ioc;

import org.slf4j.Logger;

/**
 * Contains resources that may be provided to a service when it initializes, which includes other services defined in
 * the registry. ServiceResources provides access to other services (it extends {@link
 * org.apache.tapestry5.ioc.ObjectLocator}).
 */
public interface ServiceResources extends ObjectLocator
{
    /**
     * Returns the fully qualified id of the service.
     */
    String getServiceId();

    /**
     * Returns the service interface implemented by the service.
     */
    Class getServiceInterface();

    /**
     * Returns a Logger appropriate for logging messages. This includes debug level messages about the creation and
     * configuration of the underlying service, as well as debug, warning, or error level messages from the service
     * itself. Often service interceptors will make use of the service's logger.
     */
    Logger getLogger();

    /**
     * Returns an object that can be used to track operations related to constructing, configuring, decorating and
     * initializing the service.
     */
    OperationTracker getTracker();
}
