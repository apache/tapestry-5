// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.javascript;

import java.util.Map;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.internal.services.DocumentLinker;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ClientInfrastructure;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.JavascriptSupport;

public class JavascriptSupportImpl implements JavascriptSupport
{
    private final IdAllocator idAllocator;

    private final DocumentLinker linker;

    private final ClientInfrastructure clientInfrastructure;

    private boolean stackAssetsAdded;

    private final Map<InitializationPriority, StringBuilder> scripts = CollectionFactory.newMap();

    private final Map<InitializationPriority, JSONObject> inits = CollectionFactory.newMap();

    public JavascriptSupportImpl(DocumentLinker linker, ClientInfrastructure clientInfrastructure)
    {
        this(linker, clientInfrastructure, new IdAllocator());
    }

    public JavascriptSupportImpl(DocumentLinker linker, ClientInfrastructure clientInfrastructure,
            IdAllocator idAllocator)
    {
        this.linker = linker;
        this.clientInfrastructure = clientInfrastructure;
        this.idAllocator = idAllocator;
    }

    public void commit()
    {
        convertInitsToScriptBlocks();

        if (scripts.isEmpty())
            return;

        addStack();

        String masterBlock = assembleMasterScriptBlock();

        linker.addScript(masterBlock);
    }

    private String assembleMasterScriptBlock()
    {
        StringBuilder master = new StringBuilder();

        addIfNonNull(master, InitializationPriority.IMMEDIATE);

        addDomLoadedScriptBlocks(master);

        return master.toString();
    }

    private void addDomLoadedScriptBlocks(StringBuilder master)
    {
        if (scripts.containsKey(InitializationPriority.EARLY) || scripts.containsKey(InitializationPriority.NORMAL)
                || scripts.containsKey(InitializationPriority.LATE))
        {
            master.append("Tapestry.onDOMLoaded(function() {\n");

            addIfNonNull(master, InitializationPriority.EARLY);
            addIfNonNull(master, InitializationPriority.NORMAL);
            addIfNonNull(master, InitializationPriority.LATE);

            master.append("});");
        }
    }

    private void convertInitsToScriptBlocks()
    {
        for (InitializationPriority p : InitializationPriority.values())
        {
            JSONObject init = inits.get(p);

            if (init != null)
                addScript(p, "Tapestry.init(%s);", init);
        }
    }

    private void addIfNonNull(StringBuilder builder, InitializationPriority priority)
    {
        if (scripts.containsKey(priority))
            builder.append(scripts.get(priority).toString());
    }

    public void addInitializerCall(InitializationPriority priority, String functionName, JSONObject parameter)
    {
        Defense.notNull(priority, "priority");
        Defense.notBlank(functionName, "functionName");
        Defense.notNull(parameter, "parameter");

        JSONObject init = inits.get(priority);

        if (init == null)
        {
            init = new JSONObject();
            inits.put(priority, init);
        }

        JSONArray invocations = init.has(functionName) ? init.getJSONArray(functionName) : null;

        if (invocations == null)
        {
            invocations = new JSONArray();
            init.put(functionName, invocations);
        }

        invocations.put(parameter);
    }

    public void addInitializerCall(String functionName, JSONObject parameter)
    {
        addInitializerCall(InitializationPriority.NORMAL, functionName, parameter);
    }

    public void addScript(InitializationPriority priority, String format, Object... arguments)
    {
        Defense.notNull(priority, "priority");
        Defense.notBlank(format, "format");

        StringBuilder script = scripts.get(priority);

        if (script == null)
        {
            script = new StringBuilder();
            scripts.put(priority, script);
        }

        if (arguments.length == 0)
            script.append(format);
        else
            script.append(String.format(format, arguments));

        script.append("\n");
    }

    public void addScript(String format, Object... arguments)
    {
        addScript(InitializationPriority.NORMAL, format, arguments);
    }

    public String allocateClientId(ComponentResources resources)
    {
        return allocateClientId(resources.getId());
    }

    public String allocateClientId(String id)
    {
        return idAllocator.allocateId(id);
    }

    public void importJavascriptLibrary(Asset asset)
    {
        Defense.notNull(asset, "asset");

        addStack();

        linker.addScriptLink(asset.toClientURL());
    }

    private void addStack()
    {
        if (!stackAssetsAdded)
        {
            for (Asset script : clientInfrastructure.getJavascriptStack())
            {
                linker.addScriptLink(script.toClientURL());
            }

            stackAssetsAdded = true;
        }
    }
}
