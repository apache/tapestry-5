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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.Field;
import org.apache.tapestry.Link;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.corelib.data.InsertPosition;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.json.JSONArray;
import org.apache.tapestry.json.JSONObject;

public class ClientBehaviorSupportImpl implements ClientBehaviorSupport
{
    private final PageRenderSupport pageRenderSupport;

    private final JSONObject _validations = new JSONObject();

    public ClientBehaviorSupportImpl(PageRenderSupport pageRenderSupport)
    {
        this.pageRenderSupport = pageRenderSupport;
    }

    public void addZone(String clientId, String showFunctionName, String updateFunctionName)
    {
        JSONObject spec = new JSONObject();

        addFunction(spec, "show", showFunctionName);
        addFunction(spec, "update", updateFunctionName);

        addElementInit("zone", clientId, spec);
    }

    private void addElementInit(String functionName, String clientId, JSONObject spec)
    {
        Defense.notBlank(clientId, "clientId");

        if (spec.length() == 0)
        {
            pageRenderSupport.addInit(functionName, clientId);
            return;
        }

        spec.put("element", clientId);

        pageRenderSupport.addInit(functionName, spec);
    }


    private void addFunction(JSONObject spec, String key, String showFunctionName)
    {
        if (showFunctionName != null) spec.put(key, showFunctionName.toLowerCase());
    }

    public void linkZone(String linkId, String elementId)
    {
        JSONArray spec = new JSONArray();
        spec.put(linkId);
        spec.put(elementId);

        pageRenderSupport.addInit("linkZone", spec);
    }

    public void addFormFragment(String clientId, String showFunctionName, String hideFunctionName)
    {
        JSONObject spec = new JSONObject();

        addFunction(spec, "show", showFunctionName);
        addFunction(spec, "hide", hideFunctionName);

        addElementInit("formFragment", clientId, spec);
    }

    public void addFormInjector(String clientId, Link link, InsertPosition insertPosition, String showFunctionName)
    {
        JSONObject spec = new JSONObject();
        spec.put("element", clientId);

        spec.put("url", link.toAbsoluteURI());

        if (insertPosition == InsertPosition.BELOW)
            spec.put("below", true);

        addFunction(spec, "show", showFunctionName);

        // Always has at least two properties.

        pageRenderSupport.addInit("formInjector", spec);
    }


    public void addValidation(Field field, String validationName, String message, Object constraint)
    {
        String fieldId = field.getClientId();

        JSONArray specs;

        if (_validations.has(fieldId)) specs = _validations.getJSONArray(fieldId);
        else
        {
            specs = new JSONArray();
            _validations.put(fieldId, specs);
        }

        JSONArray thisSpec = new JSONArray();

        thisSpec.put(validationName);
        thisSpec.put(message);

        if (constraint != null) thisSpec.put(constraint);

        specs.put(thisSpec);
    }

    /**
     * Invoked at the end of rendering to commit (to the {@link org.apache.tapestry.PageRenderSupport}) any accumulated
     * validations.
     */
    public void commit()
    {
        for (String field : _validations.keys())
        {
            JSONArray specs = _validations.getJSONArray(field);

            JSONArray parameters = new JSONArray();
            parameters.put(field);
            parameters.put(specs);

            pageRenderSupport.addInit("validate", parameters);
        }
    }
}
