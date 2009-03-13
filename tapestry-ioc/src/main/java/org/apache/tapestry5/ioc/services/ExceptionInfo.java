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

import java.util.List;

/**
 * Contains information about an analyzed exception.
 *
 * @see {@link org.apache.tapestry5.ioc.services.ExceptionAnalysis}
 */
public interface ExceptionInfo
{
    /**
     * The exception class name.
     */
    String getClassName();

    /**
     * The message associated with the exception, possibly null.
     */
    String getMessage();

    /**
     * Returns the names of the properties of the exception, sorted alphabetically.
     */
    List<String> getPropertyNames();

    /**
     * Returns a specific property of the exception by name.
     */
    Object getProperty(String name);

    /**
     * Returns the stack trace elements. Generally this is an empty list except for the deepest exception.
     */
    List<StackTraceElement> getStackTrace();
}
