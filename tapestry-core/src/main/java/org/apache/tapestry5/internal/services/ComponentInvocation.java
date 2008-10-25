// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.EventContext;

import java.util.List;

/**
 * Represents an invocation of a page (to render) or a component (to handle an event). This is the core of the {@link
 * org.apache.tapestry5.Link} implementation, and is seperated out to faciliate the {@link
 * org.apache.tapestry5.test.PageTester}.
 */
public interface ComponentInvocation
{
    /**
     * Constructs the URI for the component invocation. This may include the event context or page activation context.
     * If the invocation was constructed for a form, then parameters will be omitted (such that they can be rendered as
     * individual hidden fields within the form) ... otherwise, the URI will include query parameters.
     */
    String buildURI();

    /**
     * Returns the event context associated with the component event.  This will be an empty event context for a page
     * render request.
     */
    EventContext getEventContext();

    /**
     * Returns the page activation context for the page referenced in a page render or component event request.
     */
    EventContext getPageActivationContext();

    /**
     * Adds an additional parameter to be encoded into the URL.
     *
     * @param parameterName name of parameter
     * @param value         parameter value (should be URL safe)
     */
    void addParameter(String parameterName, String value);

    /**
     * Returns sorted list of parameter names.
     */
    List<String> getParameterNames();

    /**
     * Returns value for a parameter.
     */
    String getParameterValue(String name);

    /**
     * Returns the target of the invocation (this is used by {@link org.apache.tapestry5.test.PageTester}).
     */
    InvocationTarget getTarget();
}
