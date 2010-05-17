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

package org.apache.tapestry5.internal.services.ajax;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.DocumentLinker;
import org.apache.tapestry5.internal.services.javascript.JavascriptStackPathConstructor;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.internal.util.Func;
import org.apache.tapestry5.ioc.internal.util.IdAllocator;
import org.apache.tapestry5.ioc.internal.util.Operation;
import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.JavascriptStack;
import org.apache.tapestry5.services.javascript.JavascriptStackSource;
import org.apache.tapestry5.services.javascript.JavascriptSupport;

public class JavascriptSupportImpl implements JavascriptSupport
{
    private class Stylesheet
    {
        final String path;

        final String media;

        public Stylesheet(String path, String media)
        {
            this.path = path;
            this.media = media;
        }
    }

    private final IdAllocator idAllocator;

    private final DocumentLinker linker;

    // Using a Map as a case-insensitive set of stack names.

    private final Map<String, Boolean> addedStacks = CollectionFactory.newCaseInsensitiveMap();

    private final List<String> stackLibraries = CollectionFactory.newList();

    private final List<String> otherLibraries = CollectionFactory.newList();

    private final List<String> stackStylesheets = CollectionFactory.newList();

    private final Set<String> importedStylesheets = CollectionFactory.newSet();

    private final List<Stylesheet> otherStylesheets = CollectionFactory.newList();

    private final Map<InitializationPriority, JSONObject> inits = CollectionFactory.newMap();

    private final JavascriptStackSource javascriptStackSource;

    private final JavascriptStackPathConstructor stackPathConstructor;

    private static final Coercion<Asset, String> toPath = new Coercion<Asset, String>()
    {
        public String coerce(Asset input)
        {
            return input.toClientURL();
        }
    };

    public JavascriptSupportImpl(DocumentLinker linker, JavascriptStackSource javascriptStackSource,
            JavascriptStackPathConstructor stackPathConstructor)
    {
        this(linker, javascriptStackSource, stackPathConstructor, new IdAllocator(), false);
    }

    public JavascriptSupportImpl(DocumentLinker linker, JavascriptStackSource javascriptStackSource,
            JavascriptStackPathConstructor stackPathConstructor, IdAllocator idAllocator, boolean partialMode)
    {
        this.linker = linker;
        this.idAllocator = idAllocator;
        this.javascriptStackSource = javascriptStackSource;
        this.stackPathConstructor = stackPathConstructor;

        // In partial mode, assume that the infrastructure stack is already present
        // (from the original page render).

        if (partialMode)
            addedStacks.put(InternalConstants.CORE_STACK_NAME, true);
    }

    public void commit()
    {
        Func.each(new Operation<String>()
        {
            public void op(String value)
            {
                linker.addStylesheetLink(value, null);
            }
        }, stackStylesheets);

        Func.each(new Operation<Stylesheet>()
        {
            public void op(Stylesheet value)
            {
                linker.addStylesheetLink(value.path, value.media);
            }
        }, otherStylesheets);

        Operation<String> linkLibrary = new Operation<String>()
        {
            public void op(String value)
            {
                linker.addScriptLink(value);
            }
        };

        Func.each(linkLibrary, stackLibraries);
        Func.each(linkLibrary, otherLibraries);

        for (InitializationPriority p : InitializationPriority.values())
        {
            JSONObject init = inits.get(p);

            if (init != null)
                linker.setInitialization(p, init);
        }
    }

    public void addInitializerCall(InitializationPriority priority, String functionName, JSONObject parameter)
    {
        storeInitializerCall(priority, functionName, parameter);
    }

    private void storeInitializerCall(InitializationPriority priority, String functionName, Object parameter)
    {
        Defense.notNull(priority, "priority");
        Defense.notBlank(functionName, "functionName");
        Defense.notNull(parameter, "parameter");

        addCoreStackIfNeeded();

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

    public void addInitializerCall(InitializationPriority priority, String functionName, String parameter)
    {
        storeInitializerCall(priority, functionName, parameter);
    }

    public void addInitializerCall(String functionName, String parameter)
    {
        addInitializerCall(InitializationPriority.NORMAL, functionName, parameter);
    }

    public void addScript(InitializationPriority priority, String format, Object... arguments)
    {
        addCoreStackIfNeeded();

        Defense.notNull(priority, "priority");
        Defense.notBlank(format, "format");

        String newScript = arguments.length == 0 ? format : String.format(format, arguments);

        linker.addScript(priority, newScript);
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

        importJavascriptLibrary(asset.toClientURL());
    }

    public void importJavascriptLibrary(String libraryURL)
    {
        addCoreStackIfNeeded();

        if (otherLibraries.contains(libraryURL))
            return;

        otherLibraries.add(libraryURL);
    }

    private void addCoreStackIfNeeded()
    {
        addAssetsFromStack(InternalConstants.CORE_STACK_NAME);
    }

    private void addAssetsFromStack(String stackName)
    {
        if (addedStacks.containsKey(stackName))
            return;

        JavascriptStack stack = javascriptStackSource.getStack(stackName);

        stackLibraries.addAll(stackPathConstructor.constructPathsForJavascriptStack(stackName));

        List<String> stylesheetPaths = Func.map(toPath, stack.getStylesheets());

        stackStylesheets.addAll(stylesheetPaths);

        String initialization = stack.getInitialization();

        if (initialization != null)
            linker.addScript(InitializationPriority.IMMEDIATE, initialization);

        addedStacks.put(stackName, true);
    }

    public void importStylesheet(Asset stylesheet, String media)
    {
        Defense.notNull(stylesheet, "stylesheet");

        importStylesheet(stylesheet.toClientURL(), media);
    }

    public void importStylesheet(String stylesheetURL, String media)
    {
        Defense.notBlank(stylesheetURL, "stylesheetURL");

        // Assumes no overlap between stack stylesheets and all other stylesheets

        if (importedStylesheets.contains(stylesheetURL))
            return;

        importedStylesheets.add(stylesheetURL);

        otherStylesheets.add(new Stylesheet(stylesheetURL, media));
    }

    public void importStack(String stackName)
    {
        Defense.notBlank(stackName, "stackName");

        addCoreStackIfNeeded();

        addAssetsFromStack(stackName);
    }

}
