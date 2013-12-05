// Copyright 2013 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.services;

import java.util.Date;

/**
 * Formatting utilities for dates; this is primarily used when communicating server-side dates to the clients in ISO-8601    format.
 *
 * @since 5.4
 */
public interface DateUtilities
{
    /**
     * Formats the time instant in ISO-8601 format for the UTC time zone.
     *
     * @param date
     *         to format, may not be null
     * @return formatted date
     */
    String formatISO8601(Date date);
}
