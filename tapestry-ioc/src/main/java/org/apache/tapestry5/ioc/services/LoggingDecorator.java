// Copyright 2006, 2007, 2009 The Apache Software Foundation
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

import org.slf4j.Logger;

/**
 * Service that can create a logging interceptor that wraps around a service implementation (or interceptor). The
 * interceptor works with the service's log to log, at debug level, method entry (with arguments), method exit (with
 * return value, if any) as well as any thrown exceptions.
 * <p/>
 * This represents the Tapestry 5.0 decorator approach; for Tapestry 5.1 you may want to use the {@link
 * org.apache.tapestry5.ioc.services.LoggingAdvisor} in conjunction with a service advisor method.
 */
public interface LoggingDecorator
{
    /**
     * Builds a logging interceptor instance.
     *
     * @param <T>
     * @param serviceInterface interface implemented by the delegate
     * @param delegate         existing object to be wrapped
     * @param serviceId        id of service
     * @param logger           log used for debug level logging messages by the interceptor
     * @return a new object implementing the interface that can be used in place of the delegate, providing logging
     *         behavior around each method call on the service interface
     */
    <T> T build(Class<T> serviceInterface, T delegate, String serviceId, Logger logger);
}
