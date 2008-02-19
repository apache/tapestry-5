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

import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.json.JSONArray;
import org.apache.tapestry.json.JSONObject;

public class ZoneSetupImpl implements ZoneSetup
{
    static final String ZONE_INITIALIZER_STRING = "Tapestry.initializeZones(%s, %s);";

    private final PageRenderSupport _pageRenderSupport;

    private final JSONArray _zones = new JSONArray();

    private final JSONArray _links = new JSONArray();

    private final JSONArray _subForms = new JSONArray();

    private boolean _zonesDirty;

    private boolean _subformsDirty;

    public ZoneSetupImpl(PageRenderSupport pageRenderSupport)
    {
        _pageRenderSupport = pageRenderSupport;
    }

    public void addZone(String clientId, String showFunctionName, String updateFunctionName)
    {
        JSONObject spec = new JSONObject();
        spec.put("div", clientId);

        addFunction(spec, "show", showFunctionName);
        addFunction(spec, "update", updateFunctionName);

        _zones.put(spec);

        _zonesDirty = true;
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

        _links.put(spec);

        _zonesDirty = true;

    }

    public void addFormFragment(String clientId, String showFunctionName, String hideFunctionName)
    {
        JSONObject spec = new JSONObject();
        spec.put("element", clientId);

        addFunction(spec, "show", showFunctionName);
        addFunction(spec, "hide", hideFunctionName);

        _subForms.put(spec);

        _subformsDirty = true;

    }

    public void writeInitializationScript()
    {
        if (_zonesDirty) _pageRenderSupport.addScript(ZONE_INITIALIZER_STRING, _zones, _links);

        if (_subformsDirty)
            _pageRenderSupport.addScript("Tapestry.initializeFormFragments(%s);", _subForms);
    }
}
