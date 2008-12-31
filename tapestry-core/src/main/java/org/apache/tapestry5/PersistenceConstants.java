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

package org.apache.tapestry5;

/**
 * Constants for persistent field strategies.
 *
 * @see org.apache.tapestry5.annotations.Persist#value()
 */
public class PersistenceConstants
{
    /**
     * The field's value is stored in the {@link org.apache.tapestry5.services.Session}.
     */
    public static final String SESSION = "session";

    /**
     * The field's value is stored on the client, as a query parameter or hidden form field.
     */
    public static final String CLIENT = "client";

    /**
     * The page field persistence strategy that stores data in the session until the next request.
     */
    public static final String FLASH = "flash";
}
