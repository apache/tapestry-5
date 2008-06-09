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

package org.apache.tapestry5.services;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * Generic version of {@link HttpSession}, used to bridge the gaps between the Servlet API and the Portlet API.
 */
public interface Session
{
    /**
     * Returns a list of the names of all attributes stored in the session. The names are returned sorted
     * alphabetically.
     */
    List<String> getAttributeNames();

    /**
     * Returns a list of the names of all attributes stored in the session whose name has the provided prefix. The names
     * are returned in alphabetical order.
     */
    List<String> getAttributeNames(String prefix);

    /**
     * Returns the value previously stored in the session.
     */
    Object getAttribute(String name);

    /**
     * Sets the value of an attribute. If the value is null, then the attribute is deleted.
     */
    void setAttribute(String name, Object value);

    /**
     * Returns the maximum time interval, in seconds, that the servlet container will keep this session open between
     * client accesses. After this interval, the servlet container will invalidate the session. The maximum time
     * interval can be set with the setMaxInactiveInterval method. A negative time indicates the session should never
     * timeout.
     */
    int getMaxInactiveInterval();

    /**
     * Specifies the time, in seconds, between client requests before the servlet container will invalidate this
     * session. A negative time indicates the session should never timeout.
     */
    void setMaxInactiveInterval(int seconds);

    /**
     * Invalidates this session then unbinds any objects bound to it.
     *
     * @throws IllegalStateException if this method is called on an already invalidated session
     */
    void invalidate();
}
