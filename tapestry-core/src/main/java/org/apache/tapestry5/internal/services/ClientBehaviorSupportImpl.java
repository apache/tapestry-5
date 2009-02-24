// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.Field;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.corelib.data.InsertPosition;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ClientBehaviorSupport;

public class ClientBehaviorSupportImpl implements ClientBehaviorSupport
{
    private final RenderSupport renderSupport;

    private final JSONObject validations = new JSONObject();

    public ClientBehaviorSupportImpl(RenderSupport renderSupport)
    {
        this.renderSupport = renderSupport;
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
            renderSupport.addInit(functionName, clientId);
            return;
        }

        spec.put("element", clientId);

        renderSupport.addInit(functionName, spec);
    }


    private void addFunction(JSONObject spec, String key, String showFunctionName)
    {
        if (showFunctionName != null) spec.put(key, showFunctionName.toLowerCase());
    }

    public void linkZone(String linkId, String elementId, Link eventLink)
    {
        JSONArray spec = new JSONArray();
        spec.put(linkId);
        spec.put(elementId);
        spec.put(eventLink.toAbsoluteURI());

        renderSupport.addInit("linkZone", spec);
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

        renderSupport.addInit("formInjector", spec);
    }


    public void addValidation(Field field, String validationName, String message, Object constraint)
    {
        String fieldId = field.getClientId();

        JSONArray specs;

        if (validations.has(fieldId)) specs = validations.getJSONArray(fieldId);
        else
        {
            specs = new JSONArray();
            validations.put(fieldId, specs);
        }

        JSONArray thisSpec = new JSONArray();

        thisSpec.put(validationName);
        thisSpec.put(message);

        if (constraint != null) thisSpec.put(constraint);

        specs.put(thisSpec);
    }

    /**
     * Invoked at the end of rendering to commit (to the {@link org.apache.tapestry5.RenderSupport}) any accumulated
     * validations.
     */
    public void commit()
    {
        for (String field : validations.keys())
        {
            JSONArray specs = validations.getJSONArray(field);

            JSONArray parameters = new JSONArray();
            parameters.put(field);
            parameters.put(specs);

            renderSupport.addInit("validate", parameters);
        }
    }
}
