//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.EventContext;

/**
 * A service to provide utilities needed for event context encoding and decoding to and from (partial) URL paths. This
 * is used for both component event contexts and page activation contexts.
 */
public interface ContextPathEncoder
{
    /**
     * Encodes the context values into a path string. Each context value (if non-null) is first value encoded into a
     * string via the {@link org.apache.tapestry5.services.ContextValueEncoder} service.  Those values are then encoded,
     * via {@link URLEncoder#encode(String)} into URL-safe strings.  The URL-safe strings are then concatinated
     * together, seperated with "/" characters.
     *
     * @param context an array of objects to encode as the context (may be null)
     * @return the path-encoded context, or the blank string if the context is empty
     */
    String encodeIntoPath(Object[] context);

    /**
     * Encodes the context into a string. Returns the empty string if the context is empty.
     *
     * @param context to encode
     * @return encoded values seperated by "/" characters, or the empty string
     * @since 5.1.0.2
     */
    String encodeIntoPath(EventContext context);

    /**
     * Inverse of {@link #encodeIntoPath(Object[])}; the path is split into strings, and the string are decoded and
     * constructed into an {@link org.apache.tapestry5.EventContext}.
     *
     * @param path to decode, possibly empty or null
     * @return corresponding event context
     */
    EventContext decodePath(String path);
}
