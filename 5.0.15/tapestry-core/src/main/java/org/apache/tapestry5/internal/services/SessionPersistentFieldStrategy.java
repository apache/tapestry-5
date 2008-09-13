// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Session;

/**
 * A strategy for storing persistent page properties into the {@link Session session}.
 * <p/>
 * Builds attribute names as: <code>state:<em>page-name</em>:<em>component-id</em>:<em>field-name</em></code>
 */

public class SessionPersistentFieldStrategy extends AbstractSessionPersistentFieldStrategy
{
    /**
     * Prefix used to identify keys stored in the session that are being used to store persistent field data.
     */
    static final String PREFIX = "state:";

    public SessionPersistentFieldStrategy(Request request)
    {
        super(PREFIX, request);
    }
}
