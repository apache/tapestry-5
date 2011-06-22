// Copyright 2007, 2009 The Apache Software Foundation
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

/**
 * Used by other services to obtain cookie values for the current request, or to write cookie values as part of the
 * request.  Note that when writing cookies, the cookie's secure flag will match {@link
 * org.apache.tapestry5.services.Request#isSecure()}.
 */
public interface Cookies
{
    /**
     * Returns the value of the first cookie whose name matches. Returns null if no such cookie exists. This method is
     * only aware of cookies that are part of the incoming request; it does not know about additional cookies added
     * since then (via {@link #writeCookieValue(String, String)}).
     */
    String readCookieValue(String name);

    /**
     * Creates or updates a cookie value. The value is stored using a max age (in seconds) defined by the symbol
     * <code>org.apache.tapestry5.default-cookie-max-age</code>. The factory default for this value is the equivalent of
     * one week.
     */

    void writeCookieValue(String name, String value);

    /**
     * As with {@link #writeCookieValue(String, String)} but an explicit maximum age may be set.
     *
     * @param name   the name of the cookie
     * @param value  the value to be stored in the cookie
     * @param maxAge the maximum age, in seconds, to store the cookie
     */

    void writeCookieValue(String name, String value, int maxAge);

    /**
     * As with {@link #writeCookieValue(String, String)} but an explicit path may be set.
     */
    void writeCookieValue(String name, String value, String path);

    /**
     * As with {@link #writeCookieValue(String, String)} but an explicit domain may be set.
     */
    void writeDomainCookieValue(String name, String value, String domain);

    /**
     * As with {@link #writeCookieValue(String, String)} but an explicit domain and maximum age may be set.
     */
    void writeDomainCookieValue(String name, String value, String domain, int maxAge);

    /**
     * As with {@link #writeCookieValue(String, String, String)} but an explicit domain and path may be set.
     */
    void writeCookieValue(String name, String value, String path, String domain);

    /**
     * Removes a previously written cookie, by writing a new cookie with a maxAge of 0.
     */
    void removeCookieValue(String name);
}
