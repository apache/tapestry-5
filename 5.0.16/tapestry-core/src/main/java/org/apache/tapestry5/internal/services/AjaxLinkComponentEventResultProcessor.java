//  Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.Link;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.Ajax;
import org.apache.tapestry5.services.ComponentEventResultProcessor;

import java.io.IOException;

/**
 * Handles {@link org.apache.tapestry5.Link} result types by building a JSON response with key "redirectURL".
 */
public class AjaxLinkComponentEventResultProcessor implements ComponentEventResultProcessor<Link>
{
    private final ComponentEventResultProcessor masterProcessor;

    public AjaxLinkComponentEventResultProcessor(@Ajax ComponentEventResultProcessor masterProcessor)
    {
        this.masterProcessor = masterProcessor;
    }

    public void processResultValue(Link value) throws IOException
    {
        JSONObject response = new JSONObject();

        response.put("redirectURL", value.toRedirectURI());

        masterProcessor.processResultValue(response);
    }
}
