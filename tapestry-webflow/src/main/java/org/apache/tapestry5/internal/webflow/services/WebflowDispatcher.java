// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.webflow.services;

import org.apache.tapestry5.services.Dispatcher;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;
import org.apache.tapestry5.webflow.WebflowConstants;

import java.io.IOException;

public class WebflowDispatcher implements Dispatcher
{
    private final InternalFlowManager flowManager;

    public WebflowDispatcher(InternalFlowManager flowManager)
    {
        this.flowManager = flowManager;
    }

    public boolean dispatch(Request request, Response response) throws IOException
    {
        if (request.getPath().equalsIgnoreCase(WebflowConstants.WEB_FLOW_PATH))
        {
            flowManager.continueFlow();

            return true;
        }

        // Continue on to other handlers.

        return false;
    }
}
