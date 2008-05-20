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

package org.apache.tapestry5.services;

/**
 * Used when switching between normal/insecure (HTTP) and secure (HTTPS) mode.  When a switch occurs, it is no longer
 * possible to use relative URLs, instead absolute URLs must be generated. The default implementation of this is
 * simple-minded: it just tacks the correct scheme in front of {@link org.apache.tapestry5.services.Request#getServerName()}.
 * In production, behind a firewall, it is often necessary to do a bit more, since <code>getServerName()</code> will
 * often be the name of the internal server (not visible to the client web browser), and a hard-coded name of a server
 * that <em>is</em> visible to the web browser is needed.  Further, in testing, non-default ports are often used. In
 * those cases, an overriding contribution to the {@link org.apache.tapestry5.services.Alias} service will allow a custom
 * implementation to supercede the default version.
 */
public interface BaseURLSource
{
    /**
     * Returns the base portion of the URL, before the context path and servlet path are appended. The return value
     * should <em>not</em> end with a slash; it should end after the host name, or after the port number.  The context
     * path, servlet path, and path info will be appended to the returned value.
     *
     * @param secure whether a secure "https" or insecure "http" base URL should be returned
     * @return the base URL ready for additional extensions
     */
    String getBaseURL(boolean secure);
}
