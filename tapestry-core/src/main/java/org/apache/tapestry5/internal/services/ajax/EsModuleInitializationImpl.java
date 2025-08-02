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
package org.apache.tapestry5.internal.services.ajax;

import java.util.Collections;
import java.util.Map;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.javascript.EsModuleInitialization;
import org.apache.tapestry5.services.javascript.ImportPlacement;

public class EsModuleInitializationImpl extends BaseInitialization<EsModuleInitialization> implements EsModuleInitialization
{
    
    private Map<String, String> attributes;
    private ImportPlacement placement = ImportPlacement.BODY_BOTTOM;
    private JSONArray arguments;
    
    public EsModuleInitializationImpl(String moduleName) 
    {
        super(moduleName);
    }

    public EsModuleInitialization withAttribute(String id, String value) 
    {
        if (attributes == null)
        {
            attributes = CollectionFactory.newMap();
        }
        attributes.put(id, value);
        return this;
    }

    public EsModuleInitialization placement(ImportPlacement placement) 
    {
        this.placement = placement;
        return null;
    }

    public String getModuleName() {
        return moduleName;
    }

    public Map<String, String> getAttributes() {
        return attributes != null ? 
                Collections.unmodifiableMap(attributes) : 
                    Collections.emptyMap();
    }

    public ImportPlacement getPlacement() {
        return placement;
    }
    
    public String getFunctionName() {
        return functionName;
    }

    @Override
    public void with(Object... arguments) 
    {
        assert arguments != null;
        this.arguments = new JSONArray(arguments);
    }
    
    public JSONArray getArguments() 
    {
        return arguments;
    }
    
    public boolean isPure() 
    {
        return functionName == null && arguments == null;
    }

    @Override
    public String toString() 
    {
        return "ESInit[moduleName=" + moduleName + ", functionName=" + functionName + ", arguments=" + arguments + "]";
    }
    
}