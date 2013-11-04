// Copyright 2008-2013 The Apache Software Foundation
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

import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.ModuleConfigurationCallback;
import org.apache.tapestry5.services.javascript.StylesheetLink;

import java.util.List;

public class PartialMarkupDocumentLinker implements DocumentLinker
{
    private final JSONArray libraryURLs = new JSONArray();

    private final JSONArray stylesheets = new JSONArray();

    private final ModuleInitsManager initsManager = new ModuleInitsManager();

    public void addCoreLibrary(String libraryURL)
    {
        notImplemented("addCoreLibrary");
    }

    public void addLibrary(String libraryURL)
    {
        libraryURLs.put(libraryURL);
    }

    public void addStylesheetLink(StylesheetLink stylesheet)
    {
        JSONObject object = new JSONObject(

                "href", stylesheet.getURL(),

                "media", stylesheet.getOptions().media);

        stylesheets.put(object);
    }

    private void notImplemented(String methodName)
    {
        throw new UnsupportedOperationException(String.format("DocumentLinker.%s() is not implemented for partial page renders.", methodName));
    }

    public void addScript(InitializationPriority priority, String script)
    {
        notImplemented("addScript");
    }
    
    public void addModuleConfigurationCallback(ModuleConfigurationCallback callback)
    {
        notImplemented("moduleConfigurationCallback");
    }

    public void addInitialization(InitializationPriority priority, String moduleName, String functionName, JSONArray arguments)
    {
        initsManager.addInitialization(priority, moduleName, functionName, arguments);
    }

    /**
     * Commits changes, adding one or more keys to the reply.
     *
     * @param reply
     *         JSON Object to be sent to client
     */
    public void commit(JSONObject reply)
    {
        if (libraryURLs.length() > 0)
        {
            reply.in(InternalConstants.PARTIAL_KEY).put("libraries", libraryURLs);
        }

        if (stylesheets.length() > 0)
        {
            reply.in(InternalConstants.PARTIAL_KEY).put("stylesheets", stylesheets);
        }

        List<?> inits = initsManager.getSortedInits();

        if (inits.size() > 0)
        {
            reply.in(InternalConstants.PARTIAL_KEY).put("inits", JSONArray.from(inits));
        }
    }
}
