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

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.BooleanHook;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.FieldFocusPriority;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Worker;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.internal.services.DocumentLinker;
import org.apache.tapestry5.internal.services.javascript.JavaScriptStackPathConstructor;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.IdAllocator;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.*;

import java.util.*;

public class JavaScriptSupportImpl implements JavaScriptSupport
{
    private final IdAllocator idAllocator;

    private final DocumentLinker linker;

    // Using a Map as a case-insensitive set of stack names.

    private final Map<String, Boolean> addedStacks = CollectionFactory.newCaseInsensitiveMap();

    private final Set<String> otherLibraries = CollectionFactory.newSet();

    private final Set<String> importedStylesheetURLs = CollectionFactory.newSet();

    private final List<StylesheetLink> stylesheetLinks = CollectionFactory.newList();

    private final List<InitializationImpl> inits = CollectionFactory.newList();

    private final JavaScriptStackSource javascriptStackSource;

    private final JavaScriptStackPathConstructor stackPathConstructor;

    private final boolean partialMode;

    private final BooleanHook suppressCoreStylesheetsHook;

    private FieldFocusPriority focusPriority;

    private String focusFieldId;

    private Map<String, String> libraryURLToStackName, moduleNameToStackName;

    class InitializationImpl implements Initialization
    {
        InitializationPriority priority = InitializationPriority.NORMAL;

        final String moduleName;

        String functionName;

        JSONArray arguments;

        InitializationImpl(String moduleName)
        {
            this.moduleName = moduleName;
        }

        public Initialization invoke(String functionName)
        {
            assert InternalUtils.isNonBlank(functionName);

            this.functionName = functionName;

            return this;
        }

        public Initialization priority(InitializationPriority priority)
        {
            assert priority != null;

            this.priority = priority;

            return this;
        }

        public void with(Object... arguments)
        {
            assert arguments != null;

            this.arguments = new JSONArray(arguments);
        }
    }

    public JavaScriptSupportImpl(DocumentLinker linker, JavaScriptStackSource javascriptStackSource,
                                 JavaScriptStackPathConstructor stackPathConstructor, BooleanHook suppressCoreStylesheetsHook)
    {
        this(linker, javascriptStackSource, stackPathConstructor, new IdAllocator(), false, suppressCoreStylesheetsHook);
    }

    /**
     * @param linker
     *         responsible for assembling all the information gathered by JavaScriptSupport and
     *         attaching it to the Document (for a full page render) or to the JSON response (in a partial render)
     * @param javascriptStackSource
     *         source of information about {@link org.apache.tapestry5.services.javascript.JavaScriptStack}s, used when handling the import
     *         of libraries and stacks (often, to handle transitive dependencies)
     * @param stackPathConstructor
     *         encapsulates the knowledge of how to represent a stack (which may be converted
     *         from a series of JavaScript libraries into a single virtual JavaScript library)
     * @param idAllocator
     *         used when allocating unique ids (it is usually pre-initialized in an Ajax request to ensure
     *         that newly allocated ids do not conflict with previous renders and partial updates)
     * @param partialMode
     *         if true, then the JSS configures itself for a partial page render (part of an Ajax request)
     *         which automatically assumes the "core" library has been added (to the original page render)
     * @param suppressCoreStylesheetsHook
     *         a hook that enables ignoring CSS files on the core stack
     */
    public JavaScriptSupportImpl(DocumentLinker linker, JavaScriptStackSource javascriptStackSource,
                                 JavaScriptStackPathConstructor stackPathConstructor, IdAllocator idAllocator, boolean partialMode,
                                 BooleanHook suppressCoreStylesheetsHook)
    {
        this.linker = linker;
        this.idAllocator = idAllocator;
        this.javascriptStackSource = javascriptStackSource;
        this.stackPathConstructor = stackPathConstructor;
        this.partialMode = partialMode;
        this.suppressCoreStylesheetsHook = suppressCoreStylesheetsHook;

        // In partial mode, assume that the infrastructure stack is already present
        // (from the original page render).

        if (partialMode)
        {
            addedStacks.put(InternalConstants.CORE_STACK_NAME, true);
        }
    }

    public void commit()
    {
        if (focusFieldId != null)
        {
            require("t5/core/pageinit").invoke("focus").with(focusFieldId);
        }

        F.flow(stylesheetLinks).each(new Worker<StylesheetLink>()
        {
            public void work(StylesheetLink value)
            {
                linker.addStylesheetLink(value);
            }
        });

        F.flow(inits).sort(new Comparator<InitializationImpl>()
        {
            public int compare(InitializationImpl o1, InitializationImpl o2)
            {
                return o1.priority.compareTo(o2.priority);
            }
        }).each(new Worker<InitializationImpl>()
        {
            public void work(InitializationImpl element)
            {
                linker.addInitialization(element.priority, element.moduleName, element.functionName, element.arguments);
            }
        });
    }

    public void addInitializerCall(InitializationPriority priority, String functionName, JSONObject parameter)
    {
        createInitializer(priority).with(functionName, parameter);
    }

    public void addInitializerCall(String functionName, JSONArray parameter)
    {
        addInitializerCall(InitializationPriority.NORMAL, functionName, parameter);
    }

    public void addInitializerCall(InitializationPriority priority, String functionName,
                                   JSONArray parameter)
    {
        // TAP5-2300: In 5.3, a JSONArray implied an array of method arguments, so unwrap and add
        // functionName to the arguments

        List parameterList = new ArrayList(parameter.length() + 1);
        parameterList.add(functionName);
        parameterList.addAll(parameter.toList());
        createInitializer(priority).with(parameterList.toArray());
    }

    private Initialization createInitializer(InitializationPriority priority)
    {
        assert priority != null;

        importCoreStack();

        return require("t5/core/init").priority(priority);
    }

    public void addInitializerCall(String functionName, JSONObject parameter)
    {
        addInitializerCall(InitializationPriority.NORMAL, functionName, parameter);
    }

    public void addInitializerCall(InitializationPriority priority, String functionName, String parameter)
    {
        createInitializer(priority).with(functionName, parameter);
    }

    public void addInitializerCall(String functionName, String parameter)
    {
        addInitializerCall(InitializationPriority.NORMAL, functionName, parameter);
    }

    public void addScript(InitializationPriority priority, String format, Object... arguments)
    {
        assert priority != null;
        assert InternalUtils.isNonBlank(format);

        importCoreStack();

        String newScript = arguments.length == 0 ? format : String.format(format, arguments);

        if (partialMode)
        {
            require("t5/core/pageinit").invoke("evalJavaScript").with(newScript);
        } else
        {
            linker.addScript(priority, newScript);
        }
    }

    public void addScript(String format, Object... arguments)
    {
        addScript(InitializationPriority.NORMAL, format, arguments);
    }

    public void addModuleConfigurationCallback(ModuleConfigurationCallback callback)
    {
        linker.addModuleConfigurationCallback(callback);
    }

    public String allocateClientId(ComponentResources resources)
    {
        return allocateClientId(resources.getId());
    }

    public String allocateClientId(String id)
    {
        return idAllocator.allocateId(id);
    }

    public JavaScriptSupport importJavaScriptLibrary(Asset asset)
    {
        assert asset != null;

        return importJavaScriptLibrary(asset.toClientURL());
    }

    public JavaScriptSupport importJavaScriptLibrary(String libraryURL)
    {
        importCoreStack();

        String stackName = findStackForLibrary(libraryURL);

        if (stackName != null)
        {
            return importStack(stackName);
        }

        if (!otherLibraries.contains(libraryURL))
        {
            linker.addLibrary(libraryURL);

            otherLibraries.add(libraryURL);
        }

        return this;
    }

    private void importCoreStack()
    {
        addAssetsFromStack(InternalConstants.CORE_STACK_NAME);
    }

    /**
     * Locates the name of the stack that includes the library URL. Returns the stack,
     * or null if the library is free-standing.
     */
    private String findStackForLibrary(String libraryURL)
    {
        return getLibraryURLToStackName().get(libraryURL);
    }


    private Map<String, String> getLibraryURLToStackName()
    {
        if (libraryURLToStackName == null)
        {
            libraryURLToStackName = CollectionFactory.newMap();

            for (String stackName : javascriptStackSource.getStackNames())
            {
                for (Asset library : javascriptStackSource.getStack(stackName).getJavaScriptLibraries())
                {
                    libraryURLToStackName.put(library.toClientURL(), stackName);
                }
            }
        }

        return libraryURLToStackName;
    }

    private String findStackForModule(String moduleName)
    {
        return getModuleNameToStackName().get(moduleName);
    }

    private Map<String, String> getModuleNameToStackName()
    {

        if (moduleNameToStackName == null)
        {
            moduleNameToStackName = CollectionFactory.newMap();

            for (String stackName : javascriptStackSource.getStackNames())
            {
                for (String moduleName : javascriptStackSource.getStack(stackName).getModules())
                {
                    moduleNameToStackName.put(moduleName, stackName);
                }
            }
        }

        return moduleNameToStackName;
    }


    private void addAssetsFromStack(String stackName)
    {
        if (addedStacks.containsKey(stackName))
        {
            return;
        }

        JavaScriptStack stack = javascriptStackSource.getStack(stackName);

        for (String dependentStackname : stack.getStacks())
        {
            addAssetsFromStack(dependentStackname);
        }

        addedStacks.put(stackName, true);

        boolean addAsCoreLibrary = stackName.equals(InternalConstants.CORE_STACK_NAME);

        List<String> libraryURLs = stackPathConstructor.constructPathsForJavaScriptStack(stackName);

        for (String libraryURL : libraryURLs)
        {
            if (addAsCoreLibrary)
            {
                linker.addCoreLibrary(libraryURL);
            } else
            {
                linker.addLibrary(libraryURL);
            }
        }

        if (!(addAsCoreLibrary && suppressCoreStylesheetsHook.checkHook()))
        {
            stylesheetLinks.addAll(stack.getStylesheets());
        }

        String initialization = stack.getInitialization();

        if (initialization != null)
        {
            addScript(InitializationPriority.IMMEDIATE, initialization);
        }
    }

    public JavaScriptSupport importStylesheet(Asset stylesheet)
    {
        assert stylesheet != null;

        return importStylesheet(new StylesheetLink(stylesheet));
    }

    public JavaScriptSupport importStylesheet(StylesheetLink stylesheetLink)
    {
        assert stylesheetLink != null;

        importCoreStack();

        String stylesheetURL = stylesheetLink.getURL();

        if (!importedStylesheetURLs.contains(stylesheetURL))
        {
            importedStylesheetURLs.add(stylesheetURL);

            stylesheetLinks.add(stylesheetLink);
        }

        return this;
    }

    public JavaScriptSupport importStack(String stackName)
    {
        assert InternalUtils.isNonBlank(stackName);

        importCoreStack();

        addAssetsFromStack(stackName);

        return this;
    }

    public JavaScriptSupport autofocus(FieldFocusPriority priority, String fieldId)
    {
        assert priority != null;
        assert InternalUtils.isNonBlank(fieldId);

        if (focusFieldId == null || priority.compareTo(focusPriority) > 0)
        {
            this.focusPriority = priority;
            focusFieldId = fieldId;
        }

        return this;
    }

    public Initialization require(String moduleName)
    {
        assert InternalUtils.isNonBlank(moduleName);

        importCoreStack();

        String stackName = findStackForModule(moduleName);

        if (stackName != null)
        {
            importStack(stackName);
        }

        InitializationImpl init = new InitializationImpl(moduleName);

        inits.add(init);

        return init;
    }

}
