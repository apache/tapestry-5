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
    
    private final List<EsModuleInitialization> initializations = CollectionFactory.newList();
    
    public void add(EsModuleInitialization initialization)
    {
        assert initialization != null;

        // We ignore a module being added again.
        final String moduleName = ((EsModuleInitializationImpl) initialization).getModuleName();
        if (!modules.contains(moduleName))
        {
            initializations.add(initialization);
            modules.add(moduleName);
        }
    }

    /**
     * Returns all previously added inits.
     */
    public List<EsModuleInitialization> getInits()
    {
        return initializations;
    }
    
    /**
     * Returns all previously added inits as JSONArray instances.
     */
    public List<JSONArray> getInitsAsJsonArrays()
    {
        
        List<JSONArray> list;
        if (!initializations.isEmpty()) 
        {
            list = new ArrayList<>(initializations.size());
            for (EsModuleInitialization init : initializations) 
            {
                final EsModuleInitializationImpl initImpl = (EsModuleInitializationImpl) init;
                final JSONArray arguments = initImpl.getArguments();
                final JSONArray initArray = new JSONArray();
                
                initArray.add(initImpl.getModuleName());
                
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
