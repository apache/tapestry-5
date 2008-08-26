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
 * Defines a strategy, used by {@link Field} components such as {@link org.apache.tapestry5.corelib.components.TextField},
 * to handle the case where either the server-side value to be sent (as a string) to the client, or the client-side
 * string passed back up to the server, is null or blank.
 *
 * @see org.apache.tapestry5.services.NullFieldStrategySource
 */
public interface NullFieldStrategy
{
    /**
     * Provides a replacement value for null, when converting the server-side object to a client-side string. The
     * replacement value, if non-null, will be passed to {@link org.apache.tapestry5.Translator#toClient(Object)}.
     */
    Object replaceToClient();

    /**
     * Provides a replacement value for a null or blank string passed from the client to the server as part of a form
     * submission. This replacement value will be passed to {@link Translator#parseClient(Field, String, String)}  as if
     * it were the value supplied by the user.
     *
     * @return replacement value (this must not be null)
     */
    String replaceFromClient();
}
