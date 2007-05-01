// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import java.io.IOException;

import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.internal.InternalConstants;
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.services.ActionResponseGenerator;
import org.apache.tapestry.services.Dispatcher;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Response;

/**
 * Processes component action events sent as requests from the client. Action events include an
 * event type, identify a page and a component, and may provide additional context strings.
 */
public class ComponentActionDispatcher implements Dispatcher
{
    private final ActionLinkHandler _actionLinkHandler;

    private final String[] _emptyString = new String[0];

    public ComponentActionDispatcher(ActionLinkHandler actionLinkHandler)
    {
        _actionLinkHandler = actionLinkHandler;
    }

    public boolean dispatch(Request request, Response response) throws IOException
    {
        String path = request.getPath();

        String logicalPageName = null;
        String nestedComponentId = "";
        String eventType = TapestryConstants.ACTION_EVENT;

        // Will always have a dot or a colon

        int dotx = path.indexOf('.');
        int colonx = path.indexOf(':');

        int contextStart = -1;

        if (dotx > 0)
        {
            logicalPageName = path.substring(1, dotx);

            int slashx = path.indexOf('/', dotx + 1);

            // The nested id ends at the colon (if present) or
            // the first slash (if present) or the end of the path.

            if (slashx < 0)
            {
                slashx = path.length() ;
            }
            else
            {
                contextStart = slashx + 1;
            }

            int nestedIdEnd = slashx;

            if (colonx > 0 && colonx < slashx)
            {
                nestedIdEnd = colonx;
                eventType = path.substring(colonx + 1, slashx);
            }

            nestedComponentId = path.substring(dotx + 1, nestedIdEnd);
        }
        else if (colonx > 0)
        {
            // No dot, but a colon. Therefore no nested component id, but an action name and
            // maybe some event context.

            int slashx = path.indexOf('/', colonx + 1);
            if (slashx < 0)
            {
                slashx = path.length();
            }
            else
            {
                contextStart = slashx + 1;
            }

            eventType = path.substring(colonx + 1, slashx);
            logicalPageName = path.substring(1, colonx);
        }

        if (logicalPageName == null)
            return false;

        String[] eventContext = contextStart > 0 ? decodeContext(path.substring(contextStart))
                : _emptyString;

        String activationContextValue = request.getParameter(InternalConstants.PAGE_CONTEXT_NAME);

        String[] activationContext = activationContextValue == null ? _emptyString
                : decodeContext(activationContextValue);

        ActionResponseGenerator responseGenerator = _actionLinkHandler.handle(
                logicalPageName,
                nestedComponentId,
                eventType,
                eventContext,
                activationContext);

        responseGenerator.sendClientResponse(response);

        return true;
    }

    private String[] decodeContext(String input)
    {
        String[] result = input.split("/");

        for (int i = 0; i < result.length; i++)
        {
            result[i] = TapestryInternalUtils.urlDecode(result[i]);
        }

        return result;
    }

}
