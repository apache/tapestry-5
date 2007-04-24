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

package org.apache.tapestry.internal.services;

import java.io.IOException;

import org.apache.tapestry.services.ActionResponseGenerator;
import org.apache.tapestry.services.Dispatcher;
import org.apache.tapestry.services.Request;
import org.apache.tapestry.services.Response;

/**
 * Processes component action events sent as requests from the client. Action events include an
 * event type, identify a page and a component, and may provide additional context strings.
 */
public class ComponentEventDispatcher implements Dispatcher
{
    private final ActionLinkHandler _actionLinkHandler;

    public ComponentEventDispatcher(ActionLinkHandler actionLinkHandler)
    {
        _actionLinkHandler = actionLinkHandler;
    }

    public boolean dispatch(Request request, Response response) throws IOException
    {
        String path = request.getPath();

        int dotx = path.indexOf('.');

        if (dotx < 0)
            return false;

        // Skip the leading slash, the rest is logical page name.

        String logicalPageName = path.substring(1, dotx);

        int slashx = path.indexOf('/', dotx + 1);
        if (slashx < 0)
            slashx = path.length();

        int lastDotx = path.lastIndexOf('.', slashx);

        String nestedComponentId = dotx != lastDotx ? path.substring(dotx + 1, lastDotx) : "";

        String eventType = path.substring(lastDotx + 1, slashx);

        String[] context = slashx < path.length() ? path.substring(slashx + 1).split("/")
                : new String[0];

        ActionResponseGenerator responseGenerator = _actionLinkHandler.handle(
                logicalPageName,
                nestedComponentId,
                eventType,
                context);

        responseGenerator.sendClientResponse(response);

        return true;
    }

}
