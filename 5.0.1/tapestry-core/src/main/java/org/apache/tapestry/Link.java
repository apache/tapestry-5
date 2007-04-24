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

package org.apache.tapestry;

import java.util.List;

import org.apache.tapestry.services.Dispatcher;

/**
 * A link is the Tapestry representation of a URL or URI that triggers dynamic behavior. This link
 * is in two parts: a path portion and a set of query parameters. A request for a link will
 * ultimately be recognized by a {@link Dispatcher}.
 * <p>
 * Query parameter values are kept separate from the path portion to support encoding those values
 * into hidden form fields (where appropriate).
 */
public interface Link
{
    /**
     * The names of any additional query parameters for the URI. Query parameters store less regular
     * or less often used values that can not be expressed in the path. They also are used to store,
     * or link to, persistent state.
     * 
     * @return list of query parameter names, is alphabetical order
     */
    List<String> getParameterNames();

    /**
     * Returns the value of a specifically named query parameter, or null if no such query parameter
     * is stored in the link.
     */

    String getParameterValue(String name);

    /**
     * Adds a parameter value.
     * 
     * @param parameterName
     *            the name of the parameter to store
     * @param value
     *            the value to store
     * @throws IllegalArgumentException
     *             if the link already has a parameter with the given name
     */
    void addParameter(String parameterName, String value);

    /**
     * Returns the URI portion of the link. When the link is created for a form, this will not
     * include query parameters. This is the same value returned from toString().
     * 
     * @return the URI, ready to be added as an element attribute
     */
    String toURI();

    /** Returns the link as a redirect URI. The URI includes any query parameters. */
    String toRedirectURI();
}
