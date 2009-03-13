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

package org.apache.tapestry5.ioc;

/**
 * Obtained from a {@link org.apache.tapestry5.ioc.Messages}, used to format messages for a specific localized message
 * key.
 */
public interface MessageFormatter
{
    /**
     * Formats the message. The arguments are passed to {@link java.util.Formatter} as is with one exception: Object of
     * type {@link Throwable} are converted to their {@link Throwable#getMessage()} (or, if that is null, to the name of
     * the class).
     *
     * @param args
     * @return formatted string
     */
    String format(Object... args);
}
