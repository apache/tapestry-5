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

package org.apache.tapestry5.ioc.services;

/**
 * Used by {@link org.apache.tapestry5.ioc.services.LoggingDecorator} to track which exceptions have been logged during
 * the current request (the ExceptionTracker is perthread). This keeps redundant information from appearing in the
 * console output.
 */
public interface ExceptionTracker
{
    /**
     * Returns true if the indicated exception has already been logged (it is assumed that the exception will be logged
     * if this method returns false). The exception is recorded for later checks.
     *
     * @param exception to check
     * @return false if the exception has not been previously checked, true otherwise
     */
    boolean exceptionLogged(Throwable exception);
}
