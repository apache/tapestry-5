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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.services.*;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes component action events sent as requests from the client. Action events include an event type, identify a
 * page and a component, and may provide additional context strings.
 * <p/>
 * Forms: <ul> <li>/context/pagename:eventname -- event on the page, no action context</li>
 * <li>/context/pagename:eventname/foo/bar -- event on the page with action context "foo", "bar"</li>
 * <li>/context/pagename.foo.bar -- event on component foo.bar within the page, default event, no action context</li>
 * <li>/context/pagename.foo.bar/baz.gnu -- event on component foo.bar within the page, default event, with action
 * context "baz", "gnu"</li> <li>/context/pagename.bar.baz:eventname/foo/gnu -- event on component bar.baz within the
 * page with action context "foo" , "gnu"</li> </ul>
 * <p/>
 * The page name portion may itself consist of a series of folder names, i.e., "admin/user/create".  The context portion
 * isn't the concern of this code, since {@link org.apache.tapestry5.services.Request#getPath()} will already have
 * stripped that off.  We can act as if the context is always "/" (the path always starts with a slash).
 *
 * @see LinkFactory#createComponentEventLink(org.apache.tapestry5.internal.structure.Page, String, String,boolean,
 *      Object...)
 */
public class ComponentEventDispatcher implements Dispatcher
{
    private final ComponentClassResolver componentClassResolver;

    private final ComponentEventRequestHandler componentEventRequestHandler;

    private final ContextPathEncoder contextPathEncoder;

    public ComponentEventDispatcher(
            @Traditional
            ComponentEventRequestHandler componentEventRequestHandler,

            ComponentClassResolver componentClassResolver,

            ContextPathEncoder contextPathEncoder)
    {
        this.componentEventRequestHandler = componentEventRequestHandler;
        this.componentClassResolver = componentClassResolver;
        this.contextPathEncoder = contextPathEncoder;
    }

    // A beast that recognizes all the elements of a path in a single go.
    // We skip the leading slash, then take the next few terms (until a dot or a colon)
    // as the page name.  Then there's a sequence that sees a dot
    // and recognizes the nested component id (which may be missing), which ends
    // at the colon, or at the slash (or the end of the string).  The colon identifies
    // the event name (the event name is also optional).  A valid path will always have
    // a nested component id or an event name (or both) ... when both are missing, then the
    // path is most likely a page render request.  After the optional event name,
    // the next piece is the action context, which is the remainder of the path.

    private final Pattern PATH_PATTERN = Pattern.compile(

            "^/" +      // The leading slash is recognized but skipped
                    "(((\\w+)/)*(\\w+))" + // A series of folder names leading up to the page name, forming the logical page name
                    "(\\.(\\w+(\\.\\w+)*))?" + // The first dot separates the page name from the nested component id
                    "(\\:(\\w+))?" + // A colon, then the event type
                    "(/(.*))?", //  A slash, then the action context
                                Pattern.COMMENTS);

    // Constants for the match groups in the above pattern.
    private static final int LOGICAL_PAGE_NAME = 1;
    private static final int NESTED_ID = 6;
    private static final int EVENT_NAME = 9;
    private static final int CONTEXT = 11;

    public boolean dispatch(Request request, Response response) throws IOException
    {
        Matcher matcher = PATH_PATTERN.matcher(request.getPath());

        if (!matcher.matches()) return false;

        String activePageName = matcher.group(LOGICAL_PAGE_NAME);

        String nestedComponentId = matcher.group(NESTED_ID);

        String eventType = matcher.group(EVENT_NAME);

        if (nestedComponentId == null && eventType == null) return false;

        if (!componentClassResolver.isPageName(activePageName)) return false;

        EventContext eventContext = contextPathEncoder.decodePath(matcher.group(CONTEXT));

        EventContext activationContext = contextPathEncoder.decodePath(
                request.getParameter(InternalConstants.PAGE_CONTEXT_NAME));

        // The event type is often omitted, and defaults to "action".

        if (eventType == null) eventType = EventConstants.ACTION;

        if (nestedComponentId == null) nestedComponentId = "";

        String containingPageName = request.getParameter(InternalConstants.CONTAINER_PAGE_NAME);

        if (containingPageName == null) containingPageName = activePageName;

        ComponentEventRequestParameters parameters = new ComponentEventRequestParameters(activePageName,
                                                                                         containingPageName,
                                                                                         nestedComponentId, eventType,
                                                                                         activationContext,
                                                                                         eventContext);

        componentEventRequestHandler.handle(parameters);

        return true;
    }
}
