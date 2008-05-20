// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.commons.codec.net.URLCodec;

import java.util.List;

/**
 * A link is the Tapestry representation of a URL or URI that triggers dynamic behavior. This link is in three parts: a
 * path portion, an optional anchor, and a set of query parameters. A request for a link will ultimately be recognized
 * by a {@link org.apache.tapestry5.services.Dispatcher}.
 * <p/>
 * Query parameter values are kept separate from the path portion to support encoding those values into hidden form
 * fields (where appropriate).
 */
public interface Link
{
    /**
     * Returns the names of any additional query parameters for the URI. Query parameters store less regular or less
     * often used values that can not be expressed in the path. They also are used to store, or link to, persistent
     * state.
     *
     * @return list of query parameter names, is alphabetical order
     */
    List<String> getParameterNames();

    /**
     * Returns the value of a specifically named query parameter, or <tt>null</tt> if no such query parameter is stored
     * in the link.
     *
     * @return the value of the named parameter
     */
    String getParameterValue(String name);

    /**
     * Adds a parameter value. The value will be added, as is, to the URL. In many cases, the value should be URL
     * encoded via {@link URLCodec}.
     *
     * @param parameterName the name of the parameter to store
     * @param value         the value to store
     * @throws IllegalArgumentException if the link already has a parameter with the given name
     */
    void addParameter(String parameterName, String value);

    /**
     * Returns the URI portion of the link. When the link is created for a form, this will not include query parameters.
     * This is the same value returned from toString().  In some circumstances, this may be a relative URI (relative to
     * the current Request's URI).
     *
     * @return the URI, ready to be added as an element attribute
     */
    String toURI();

    /**
     * Returns the link as a redirect URI. The URI includes any query parameters.
     */
    String toRedirectURI();

    /**
     * Returns the link anchor. If this link does not have an anchor, this method returns <tt>null</tt>.
     *
     * @return the link anchor
     */
    String getAnchor();

    /**
     * Sets the link anchor. Null and empty anchors will be ignored when building the link URI.
     *
     * @param anchor the link anchor
     */
    void setAnchor(String anchor);

    /**
     * Converts the link to an absolute URI, a complete path, starting with a leading slash. This is necessary in many
     * cases for client-side JavaScript that must send a request to application via XMLHttpRequest.
     *
     * @return the complete URI (not abbreviated relative to the current request path)
     */
    String toAbsoluteURI();
}
