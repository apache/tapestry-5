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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;

public class PartialMarkupDocumentLinker implements DocumentLinker
{
    private final StringBuilder buffer = new StringBuilder(1000);

    private final JSONArray scripts = new JSONArray();

    private final JSONArray stylesheets = new JSONArray();

    public void addScriptLink(String scriptURL)
    {
        scripts.put(scriptURL);
    }

    public void addStylesheetLink(String styleURL, String media)
    {
        JSONObject object = new JSONObject();
        object.put("href", styleURL);

        if (media != null) object.put("media", media);

        stylesheets.put(object);
    }

    public void addScript(String script)
    {
        buffer.append(script);
        buffer.append("\n");
    }

    /**
     * Commits changes, adding one or more keys to the reply.
     *
     * @param reply JSON Object to be sent to client
     */
    public void commit(JSONObject reply)
    {
        if (buffer.length() > 0)
            reply.put("script", buffer.toString());

        if (scripts.length() > 0)
            reply.put("scripts", scripts);

        if (stylesheets.length() > 0)
            reply.put("stylesheets", stylesheets);

    }
}
