// Copyright 2008, 2010, 2011, 2012 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.StylesheetLink;

import java.util.List;
import java.util.Map;

public class PartialMarkupDocumentLinker implements DocumentLinker
{
    private final JSONArray scripts = new JSONArray();

    private final JSONArray stylesheets = new JSONArray();

    private final Map<InitializationPriority, List<JSONArray>> initsByPriority = CollectionFactory.newMap();

    public void addScriptLink(String scriptURL)
    {
        scripts.put(scriptURL);
    }

    public void addStylesheetLink(StylesheetLink stylesheet)
    {
        JSONObject object = new JSONObject(

                "href", stylesheet.getURL(),

                "media", stylesheet.getOptions().media);

        stylesheets.put(object);
    }

    public void addScript(InitializationPriority priority, String script)
    {
        throw new UnsupportedOperationException(
                "DocumentLinker.addScript() is not implemented for partial page renders.");
    }

    @Override
    public void addInitialization(InitializationPriority priority, String moduleName, String functionName, JSONArray arguments)
    {
        String qualifiedName = functionName == null ? moduleName : moduleName + ":" + functionName;

        JSONArray init = new JSONArray().put(qualifiedName);

        init.putAll(arguments);

        InternalUtils.addToMapList(initsByPriority, priority, init);
    }

    /**
     * Commits changes, adding one or more keys to the reply.
     *
     * @param reply
     *         JSON Object to be sent to client
     */
    public void commit(JSONObject reply)
    {
        if (scripts.length() > 0)
        {
            reply.put("scripts", scripts);
        }

        if (stylesheets.length() > 0)
        {
            reply.put("stylesheets", stylesheets);
        }

        JSONArray fullInits = new JSONArray();

        for (InitializationPriority p : InitializationPriority.values())
        {
            List<JSONArray> priorityInits = initsByPriority.get(p);

            if (priorityInits != null)
            {
                for (JSONArray init : priorityInits)
                {
                    fullInits.put(init);
                }
            }
        }

        if (fullInits.length() > 0)
        {
            reply.put("inits", fullInits);
        }
    }
}
