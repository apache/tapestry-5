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

package org.apache.tapestry5.http;

import java.util.List;

import org.apache.commons.codec.net.URLCodec;
import org.apache.tapestry5.http.services.BaseURLSource;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.ioc.annotations.IncompatibleChange;

/**
 * A link is the Tapestry representation of a URL or URI that triggers dynamic behavior. This link is in three parts: a
 * path portion, an optional anchor, and a set of query parameters. A request for a link will ultimately be recognized
 * by a {@link org.apache.tapestry5.http.services.Dispatcher}.
 *
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
     * Returns the value of a specifically named query parameter, or <code>null</code> if no such query parameter is stored
     * in the link.
     *
     * Use this method only when you are sure the parameter has only one value. If the parameter might have more than
     * one value, use {@link #getParameterValues}.
     *
     * If you use this method with a multivalued parameter, the value returned is equal to the first value in the
     * array returned by <code>getParameterValues</code>.
     *
     * @return a string representing the single value of the named parameter
     */
    String getParameterValue(String name);

    /**
     * Adds a parameter value. The value will be added, as is, to the URL. In many cases, the value should be URL
     * encoded via {@link URLCodec}.
     *
     * @param parameterName
     *         the name of the parameter to store
     * @param value
     *         the value to store, a null or blank value is allowed (as of Tapestry 5.3)
     * @return this Link, to support method chaining
     */
    @IncompatibleChange(release = "5.4", details = "changed from void to Link")
    Link addParameter(String parameterName, String value);

    /**
     * Adds a parameter value as a value object; the value object is converted to a string via
     * <a href="http://tapestry.apache.org/current/apidocs/src-html/org/apache/tapestry5/services/ContextPathEncoder.html#line.31">ContextPathEncoder.encodeValue(Object)</a>
     * from tapestry-core and the result is added via {@link #addParameter(String, String)}.
     * The Link object is returned for further configuration.
     *
     * @since 5.2.2
     */
    Link addParameterValue(String parameterName, Object value);

    /**
     * Removes a parameter value, which is occasionally useful when transforming a parameter into a portion of
     * the path.
     *
     * @return this Link, to support method chaining
     * @since 5.2.0
     */
    @IncompatibleChange(release = "5.4", details = "changed from void to Link")
    Link removeParameter(String parameterName);

    /**
     * Returns the completely unadorned base path. Other methods (such as {@link #toURI()}), may append
     * an anchor or query parameters.
     *
     * @since 5.2.0
     */
    String getBasePath();

    /**
     * Creates a copy of this link that has the same parameters, anchor, and other attributes, but a different
     * {@linkplain #getBasePath() base path}.
     *
     * @return a new Link instance
     * @since 5.2.0
     */
    Link copyWithBasePath(String basePath);

    /**
     * Returns the URI portion of the link. When the link is created for a form, this will not include query parameters.
     * This is the same value returned from toString().
     *
     * @return the URI, ready to be added as an element attribute
     */
    String toURI();

    /**
     * Returns the link as a redirect URI. The URI includes any query parameters.
     */
    String toRedirectURI();

    /**
     * Returns the link anchor. If this link does not have an anchor, this method returns <code>null</code>.
     *
     * @return the link anchor
     */
    String getAnchor();

    /**
     * Sets the link anchor. Null and empty anchors will be ignored when building the link URI.
     *
     * @param anchor
     *         the link anchor
     * @return this Link, to support method chaining
     */
    @IncompatibleChange(release = "5.4", details = "changed from void to Link")
    Link setAnchor(String anchor);

    /**
     * Returns the absolute URL, which includes the scheme, hostname and possibly port (as per
     * {@link BaseURLSource#getBaseURL(boolean)}).
     * By default, the scheme is chosen to match the {@linkplain Request#isSecure() security} of the current request.
     *
     * Note: the semantics of this method changed between Tapestry 5.1 and 5.2. Most code should use toString() or
     * {@link #toURI()} (which are equivalent) instead.
     *
     * @return the complete, qualified URL, including query parameters.
     */
    String toAbsoluteURI();

    /**
     * Returns either the secure or insecure URL, with complete scheme, hostname and possibly port (as per
     * {@link BaseURLSource#getBaseURL(boolean)}).
     *
     * @return the complete, qualified URL, including query parameters.
     * @since 5.2.2
     */
    String toAbsoluteURI(boolean secure);

    /**
     * Changes the link's security, which can be useful to force a link to be either secure or insecure
     * when normally it might not be.
     *
     * @param newSecurity
     *         new security value, not null, typically {@link LinkSecurity#FORCE_SECURE} or {@link LinkSecurity#FORCE_INSECURE}
     * @since 5.3
     */
    @IncompatibleChange(release = "5.4", details = "LinkSecurity class moved from internal package to org.apache.tapestry5.")
    void setSecurity(LinkSecurity newSecurity);

    /**
     * Returns the current security for this link, which reflects whether the targeted page is itself secure or insecure.
     *
     * @since 5.3
     */
    @IncompatibleChange(release = "5.4", details = "LinkSecurity class moved from internal package to org.apache.tapestry5.")
    LinkSecurity getSecurity();

    /**
     * Returns the parameter values for the given name. Returns null if no such parameter is stored in the link.
     */
    String[] getParameterValues(String parameterName);

}
