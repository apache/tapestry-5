// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.util;

/**
 * Contains static methods useful for manipulating exceptions.
 */
public class ExceptionUtils
{
    /**
     * Locates a particular type of exception, working its way via the cause property of each exception in the exception
     * stack.
     *
     * @param t    the outermost exception
     * @param type the type of exception to search for
     * @return the first exception of the given type, if found, or null
     */
    public static <T extends Throwable> T findCause(Throwable t, Class<T> type)
    {
        Throwable current = t;

        while (current != null)
        {
            if (type.isInstance(current)) return type.cast(current);

            // Not a match, work down.

            current = current.getCause();
        }

        return null;
    }
}
