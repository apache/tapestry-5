// Copyright 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.slf4j.Logger;

/**
 * A service used in conjuction with a service advisor method to add logging advice to a service.
 *
 * @since 5.1.0.0
 */
public interface LoggingAdvisor
{
    /**
     * Adds logging advice to all methods of the object.
     *
     * @param logger               log used for debug level logging messages by the interceptor
     * @param methodAdviceReceiver
     * @return a new object implementing the interface that can be used in place of the delegate, providing logging
     *         behavior around each method call on the service interface
     */
    <T> void addLoggingAdvice(Logger logger, MethodAdviceReceiver methodAdviceReceiver);
}
