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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.internal.services.ajax.EsModuleInitializationImpl;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.javascript.EsModuleInitialization;

public class EsModuleInitsManager
{
    private final Set<String> modules = CollectionFactory.newSet();
    
    private final List<EsModuleInitialization> imports = CollectionFactory.newList();
    
    private final List<EsModuleInitialization> initializations = CollectionFactory.newList();
    
    public void add(EsModuleInitialization initialization)
    {
        assert initialization != null;

        // We avoid having the same module being imported more than twice.
        // Also notice non-pure inits (i.e. ones having a function name or 
        // both) are added both to the imports, so they can have
        // <script type="module"> added for them, and to initializations,
        // the function call will be made by t5/core/pageinit.
        final EsModuleInitializationImpl init = (EsModuleInitializationImpl) initialization;
        final String moduleName = init.getModuleName();
        final boolean alreadyPresent = modules.contains(moduleName);
        if (!init.isPure())
        {
            initializations.add(initialization);
        }
        if (!alreadyPresent)
        {
            imports.add(initialization);
            modules.add(moduleName);
        }
    }

    /**
     * Returns all imports.
     */
    public List<EsModuleInitialization> getImports()
    {
        return imports;
    }
    
    /**
     * Returns all inits (pure imports and initializations) as a JSONArray list.
     */
    public List<JSONArray> getAllInitsAsJsonArrayList()
    {
        List<JSONArray> list;
        if (!imports.isEmpty() || !initializations.isEmpty())
        {
            list = new ArrayList<>(imports.size() + initializations.size());
            list.addAll(toJSONArray(imports));
            list.addAll(toJSONArray(initializations));
        }
        else
        {
            list = Collections.emptyList();
        }
        return list;
    }
    
    /**
     * Returns all previously added inits as a JSONArray list.
     */
    public List<JSONArray> getInitsAsJsonArrayList()
    {
        return toJSONArray(initializations);
    }

    private List<JSONArray> toJSONArray(final List<EsModuleInitialization> inits) {
        List<JSONArray> list;
        if (!inits.isEmpty()) 
        {
            list = new ArrayList<>(inits.size());
            for (EsModuleInitialization init : inits) 
            {
                final EsModuleInitializationImpl initImpl = (EsModuleInitializationImpl) init;
                final JSONArray arguments = initImpl.getArguments();
                final JSONArray initArray = new JSONArray();
                
                String moduleName = initImpl.getModuleName();
                if (initImpl.getFunctionName() != null)
                {
                    moduleName = moduleName + ":" + initImpl.getFunctionName();
                }
                
                initArray.add(moduleName);
                
                if (arguments != null)
                {
                    initArray.addAll(arguments);
                }
                
                list.add(initArray);
                
            }
        }
        else
        {
            list = Collections.emptyList();
        }
        return list;
    }

}

