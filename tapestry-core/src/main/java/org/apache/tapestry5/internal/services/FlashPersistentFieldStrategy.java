// Copyright 2007 The Apache Software Foundation
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
 * The "flash" strategy stores data inside the {@link Session session}, just like {@link
 * SessionPersistentFieldStrategy}, but also removes the values from the session on first use. In this way, a short-term
 * value (such as an error message) will "survive" from an action request to a render request and then disappear.
 */
public class FlashPersistentFieldStrategy extends AbstractSessionPersistentFieldStrategy
{
    /**
     * Prefix used to identify keys stored in the session.
     */
    static final String PREFIX = "flash:";

    public FlashPersistentFieldStrategy(Request request)
    {
        super(PREFIX, request);
    }

    @Override
    protected void didReadChange(Session session, String attributeName)
    {
        // For flash persistence, after reading a value, get rid of it.
        session.setAttribute(attributeName, null);
    }

}
