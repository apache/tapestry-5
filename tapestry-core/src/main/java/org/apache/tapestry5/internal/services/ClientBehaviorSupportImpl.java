// Copyright 2007, 2008, 2009, 2010 The Apache Software Foundation
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

import org.apache.tapestry5.Field;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.corelib.data.InsertPosition;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONLiteral;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ClientBehaviorSupport;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

public class ClientBehaviorSupportImpl implements ClientBehaviorSupport
{
    private final JavaScriptSupport javascriptSupport;

    private final Environment environment;

    private final JSONObject validations = new JSONObject();

    public ClientBehaviorSupportImpl(JavaScriptSupport javascriptSupport, Environment environment)
    {
        this.javascriptSupport = javascriptSupport;
        this.environment = environment;
    }

    public void addZone(String clientId, String showFunctionName, String updateFunctionName)
    {
        JSONObject spec = new JSONObject("element", clientId);

        addFunction(spec, "show", showFunctionName);
        addFunction(spec, "update", updateFunctionName);

        FormSupport formSupport = environment.peek(FormSupport.class);

        if (formSupport != null)
        {
            JSONObject parameters = new JSONObject(RequestConstants.FORM_CLIENTID_PARAMETER, formSupport.getClientId(),
                    RequestConstants.FORM_COMPONENTID_PARAMETER, formSupport.getFormComponentId());
            spec.put("parameters", parameters);
        }

        javascriptSupport.addInitializerCall("zone", spec);
    }

    private void addFunction(JSONObject spec, String key, String functionName)
    {
        if (functionName != null)
            spec.put(key, functionName.toLowerCase());
    }

    public void linkZone(String linkId, String elementId, Link eventLink)
    {
        JSONObject spec = new JSONObject("linkId", linkId, "zoneId", elementId, "url", eventLink.toURI());

        javascriptSupport.addInitializerCall("linkZone", spec);
    }

    /**
     * @deprecated Use {@link #addFormFragment(String,boolean,String,String)} instead
     */
    public void addFormFragment(String clientId, String showFunctionName, String hideFunctionName)
    {
        addFormFragment(clientId, false, showFunctionName, hideFunctionName, null);
    }

    /**
     * @deprecated Use {@link #addFormFragment(String, boolean, String, String, String)} instead
     */
    public void addFormFragment(String clientId, boolean alwaysSubmit, String showFunctionName, String hideFunctionName)
    {
        addFormFragment(clientId, false, showFunctionName, hideFunctionName, null);
    }

    public void addFormFragment(String clientId, boolean alwaysSubmit, String showFunctionName, String hideFunctionName, String visibilityBoundFunctionName)
    {
        JSONObject spec = new JSONObject("element", clientId);

        addFunction(spec, "show", showFunctionName);
        addFunction(spec, "hide", hideFunctionName);

        if (visibilityBoundFunctionName != null)
            spec.put("bound", new JSONLiteral(visibilityBoundFunctionName));

        if (alwaysSubmit)
            spec.put("alwaysSubmit", true);

        javascriptSupport.addInitializerCall("formFragment", spec);
    }

    public void addFormInjector(String clientId, Link link, InsertPosition insertPosition, String showFunctionName)
    {
        JSONObject spec = new JSONObject("element", clientId, "url", link.toURI());

        if (insertPosition == InsertPosition.BELOW)
            spec.put("below", true);

        addFunction(spec, "show", showFunctionName);

        // Always has at least two properties.

        javascriptSupport.addInitializerCall("formInjector", spec);
    }

    public void addValidation(Field field, String validationName, String message, Object constraint)
    {
        String fieldId = field.getClientId();

        JSONArray specs;

        if (validations.has(fieldId))
            specs = validations.getJSONArray(fieldId);
        else
        {
            specs = new JSONArray();
            validations.put(fieldId, specs);
        }

        JSONArray thisSpec = new JSONArray();

        thisSpec.put(validationName);
        thisSpec.put(message);

        if (constraint != null)
            thisSpec.put(constraint);

        specs.put(thisSpec);
    }

    /**
     * Invoked at the end of rendering to commit (to the {@link JavaScriptSupport}) any accumulated
     * validations.
     */
    public void commit()
    {
        if (validations.length() != 0)
            javascriptSupport.addInitializerCall("validate", validations);
    }
}
