// Copyright 2014 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.pages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.UnknownActivationContextCheck;
import org.apache.tapestry5.annotations.WhitelistAccessOnly;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.internal.ThrowawayClassLoader;
import org.apache.tapestry5.internal.plastic.ClassLoaderDelegate;
import org.apache.tapestry5.internal.plastic.PlasticClassLoader;
import org.apache.tapestry5.internal.services.ComponentDependencyGraphvizGenerator;
import org.apache.tapestry5.internal.services.ComponentDependencyRegistry;
import org.apache.tapestry5.internal.services.ComponentDependencyRegistry.DependencyType;
import org.apache.tapestry5.ioc.annotations.Description;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentLibraryInfo;
import org.apache.tapestry5.services.ComponentLibraryInfoSource;
import org.apache.tapestry5.services.LibraryMapping;
import org.apache.tapestry5.util.TextStreamResponse;

/**
 * Page used to describe the component libraries being used in the application.
 * Notice: the implementation of this page was done to avoid creating components, so the
 * Tapestry 5 Core Library didn't get polluted with internal-only components.
 */
@UnknownActivationContextCheck(false)
@WhitelistAccessOnly
public class ComponentLibraries
{

    final private static String[] EMTPY_STRING_ARRAY = {};

    private static final Comparator<LibraryMapping> LIBRARY_MAPPING_COMPARATOR = new Comparator<LibraryMapping>()
    {
        @Override
        public int compare(LibraryMapping mapping1, LibraryMapping mapping2)
        {
            return mapping1.libraryName.compareTo(mapping2.libraryName);
        }
    };

    private static enum Type { PAGE, COMPONENT, MIXIN }
    
    @InjectComponent
    private Zone zone;

    @Inject
    private ComponentClassResolver componentClassResolver;

    @Property
    private LibraryMapping libraryMapping;

    @Property
    private String logicalName;
    
    @Property
    private List<String> logicalNames;

    @Property
    private String headerName;

    @Property
    private List<String> pages;
    
    @Property
    private List<String> components;
    
    @Property
    private List<String> mixins;
    
    private Type type; 
    
    @Inject
    private Block classesTable;
    
    @Inject
    @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
    @Property
    private boolean productionMode;
    
    @Inject
    private ComponentLibraryInfoSource componentLibraryInfoSource;
    
    @Inject
    private ComponentDependencyRegistry componentDependencyRegistry;
    
    @Inject
    private ComponentDependencyGraphvizGenerator componentDependencyGraphvizGenerator;
    
    @Property
    private String selectedComponent;
    
    @Property
    private String dependency;
    
    @Persist
    @Property
    private boolean showEverything;
    
//    void onActivate(List<String> context)
//    {
//        if (context.size() > 0)
//        {
//            selectedComponent = String.join("/", context);
//        }
//        else
//        {
//            selectedComponent = null;
//        }
//    }
//    
//    Object[] onPassivate()
//    {
//        return selectedComponent.split("/");
//    }
    
    @Cached
    public List<LibraryMapping> getLibraryMappings()
    {
        List<LibraryMapping> mappings = new ArrayList<LibraryMapping>();
        
        // add all the library mappings, except the "" (empty string) one.
        for (LibraryMapping libraryMapping : componentClassResolver.getLibraryMappings())
        {
            if (showEverything || !"".equals(libraryMapping.libraryName)) {
                mappings.add(libraryMapping);
            }
        }
        
        Collections.sort(mappings, LIBRARY_MAPPING_COMPARATOR);
        return mappings;
    }
    
    @Cached(watch="libraryMapping")
    public ComponentLibraryInfo getInfo()
    {
        return componentLibraryInfoSource.find(libraryMapping);
    }
    
    public List<String> getLibraryNames() {
        return componentClassResolver.getLibraryNames();
    }
    
    public String getLibraryClientId() 
    {
        return libraryMapping.libraryName.replace("/", "-");
    }

    private List<String> filter(final List<String> allNames)
    {
        List<String> logicalNames = new ArrayList<String>();
        final List<LibraryMapping> libraryMappings = getLibraryMappings();
        for (String name : allNames)
        {
            
            if (name.startsWith(libraryMapping.libraryName + "/") && 
                    !(libraryMapping.libraryName.equals("core") && name.endsWith("Test")))
            {
                logicalNames.add(name);
            }
            else
            {
                if (libraryMapping.libraryName.equals(""))
                {
                    boolean isWebappLibrary = true;
                    for (LibraryMapping otherLibraryMapping : libraryMappings) {
                        if (!libraryMapping.equals(otherLibraryMapping) &&
                                name.startsWith(otherLibraryMapping.libraryName + "/"))
                        {
                            isWebappLibrary = false;
                            break;
                        }
                    }
                    if (isWebappLibrary)
                    {
                        logicalNames.add(name);
                    }
                }
            }
        }
        
        return logicalNames;
    }
    
    public Block getComponentsTable()
    {
        logicalNames = filter(componentClassResolver.getComponentNames());
        type = Type.COMPONENT;
        headerName = "Components";
        return classesTable;
    }
    
    public Block getPagesTable()
    {
        logicalNames = filter(componentClassResolver.getPageNames());
        type = Type.PAGE;
        headerName = "Pages";
        return classesTable;
    }

    public Block getMixinsTable()
    {
        logicalNames = filter(componentClassResolver.getMixinNames());
        type = Type.MIXIN;
        headerName = "Mixins";
        return classesTable;
    }
    
    public String getSourceUrl()
    {
        return getInfo() != null ? getInfo().getSourceUrl(getClassName()) : null;
    }
    
    public String getJavaDocUrl() 
    {
        return getInfo() != null ? getInfo().getJavadocUrl(getClassName()) : null;
    }

    private String getClassName()
    {
        return getClassName(logicalName, type, componentClassResolver);
    }
    
    private static String getClassName(String logicalName, Type type, ComponentClassResolver componentClassResolver)
    {
        String className;
        switch (type)
        {
            case PAGE: className = componentClassResolver.resolvePageNameToClassName(logicalName); break;
            case COMPONENT: className = componentClassResolver.resolveComponentTypeToClassName(logicalName); break;
            case MIXIN: className = componentClassResolver.resolveMixinTypeToClassName(logicalName); break;
            default: className = null; // should never happen
        }
        return className;
    }
    
    public String getSimpleLogicalName()
    {
        return logicalName.replace("core/", "");
    }
    
    @Cached(watch = "logicalName")
    public String[] getTags() throws ClassNotFoundException {
        Description description = getDescription();
        return description != null ? description.tags() : EMTPY_STRING_ARRAY;
    }

    @Cached(watch = "logicalName")
    public Description getDescription() throws ClassNotFoundException
    {
        try {
            return Class.forName(getClassName()).getAnnotation(Description.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isClassHasTags() throws ClassNotFoundException
    {
        return getTags().length > 0;
    }
    
    @OnEvent("json")
    Object generateJSONDescription(String libraryName)
    {
        for (LibraryMapping mapping : componentClassResolver.getLibraryMappings())
        {
            if (libraryName.equalsIgnoreCase(mapping.libraryName))
            {
                this.libraryMapping = mapping;
                break;
            }
        }
        JSONObject object = new JSONObject();
        object.put("libraryName", libraryName);
        object.put("rootPackage", libraryMapping.getRootPackage());
        
        final ComponentLibraryInfo info = getInfo();
        if (info != null)
        {
            JSONObject infoJsonObject = new JSONObject();
            putIfNotNull("description", info.getDescription(), infoJsonObject);
            putIfNotNull("homepage", info.getHomepageUrl(), infoJsonObject);
            putIfNotNull("documentationUrl", info.getDocumentationUrl(), infoJsonObject);
            putIfNotNull("javadocUrl", info.getJavadocUrl(), infoJsonObject);
            putIfNotNull("groupId", info.getGroupId(), infoJsonObject);
            putIfNotNull("artifactId", info.getArtifactId(), infoJsonObject);
            putIfNotNull("version", info.getVersion(), infoJsonObject);
            putIfNotNull("sourceBrowseUrl", info.getSourceBrowseUrl(), infoJsonObject);
            putIfNotNull("sourceRootUrl", info.getSourceRootUrl(), infoJsonObject);
            putIfNotNull("issueTrackerUrl", info.getIssueTrackerUrl(), infoJsonObject);
            putIfNotNull("dependencyInfoUrl", info.getDependencyManagementInfoUrl(), infoJsonObject);
            
            if (info.getTags() != null)
            {
                for (String tag : info.getTags())
                {
                    infoJsonObject.accumulate("tags", tag);
                }
            }
            
            object.put("info", infoJsonObject);
            
        }
        
        addClasses("components", filter(componentClassResolver.getComponentNames()), Type.COMPONENT, info, object);
        addClasses("pages", filter(componentClassResolver.getPageNames()), Type.PAGE, info, object);
        addClasses("mixins", filter(componentClassResolver.getMixinNames()), Type.MIXIN, info, object);
        
        return new TextStreamResponse("text/javascript", object.toString());
        
    }

    private void addClasses(final String property, List<String> classes, Type type,
            final ComponentLibraryInfo info, JSONObject object)
    {
        if (classes.size() > 0)
        {
            JSONArray classesJsonArray = new JSONArray();
            for (String logicalName : classes)
            {
                logicalName = logicalName.replace("core/", "");
                final String className = getClassName(logicalName, type, componentClassResolver);
                JSONObject classJsonObject = new JSONObject();
                classJsonObject.put("logicalName", logicalName);
                classJsonObject.put("class", className);
                if (info != null)
                {
                    putIfNotNull("sourceUrl", info.getSourceUrl(className), classJsonObject);
                    putIfNotNull("javadocUrl", info.getJavadocUrl(className), classJsonObject);
                }
                try
                {
                    final Description description = getClass(className);
                    if (description != null)
                    {
                        putIfNotNull("description", description.text(), classJsonObject);
                        if (description.tags().length > 0)
                        {
                            for (String tag : description.tags())
                            {
                                classJsonObject.accumulate("tag", tag);
                            }
                        }
                    }
                }
                catch (ClassNotFoundException e)
                {
                    throw new RuntimeException(e);
                }
                classesJsonArray.put(classJsonObject);
            }
            object.put(property, classesJsonArray);
        }
    }

    private Description getClass(final String className) throws ClassNotFoundException
    {
        return Class.forName(className).getAnnotation(Description.class);
    }
    
    private void putIfNotNull(String propertyName, String value, JSONObject object)
    {
        if (value != null)
        {
            object.put(propertyName, value);
        }
    }
    
    public String getGraphvizValue()
    {
        return componentDependencyGraphvizGenerator.generate(
                getClassName(selectedComponent));
    }
    
    public String getClassName(String logicalName)
    {
        return componentClassResolver.getClassName(logicalName);
    }
    
    public String getComponentClassName()
    {
        return getClassName(selectedComponent);
    }
    
    public List<String> getDependencies()
    {
        final String className = componentClassResolver.getClassName(selectedComponent);
        final List<String> dependencies = new ArrayList<>();
        dependencies.addAll(componentDependencyRegistry.getDependencies(className, DependencyType.INJECT_PAGE));
        dependencies.addAll(componentDependencyRegistry.getDependencies(className, DependencyType.SUPERCLASS));
        dependencies.addAll(componentDependencyRegistry.getDependencies(className, DependencyType.USAGE));
        Collections.sort(dependencies);
        return dependencies;
    }
    
    public List<String> getDependents()
    {
        final String className = componentClassResolver.getClassName(selectedComponent);
        List<String> dependents = new ArrayList<>(
                componentDependencyRegistry.getDependents(className));
        Collections.sort(dependents);
        return dependents;
    }
    
    public String getDisplayLogicalName()
    {
        return componentClassResolver.getLogicalName(dependency);
    }
    
    public Object onSelectComponent(String selectedComponent)
    {
        this.selectedComponent = selectedComponent;
        final String className = componentClassResolver.getClassName(selectedComponent);
        if (!componentDependencyRegistry.contains(className)) 
        {
            
            final ClassLoader classLoader = new ThrowawayClassLoader(getClass().getClassLoader());
            
            try 
            {
                componentDependencyRegistry.register(classLoader.loadClass(className));
            } catch (ClassNotFoundException e) 
            {
                throw new RuntimeException(e);
            }
        }
        return zone.getBody();
    }
    
    public Object getContext()
    {
        return logicalName;
    }
    
    public Object onReset()
    {
        selectedComponent = null;
        return zone.getBody();
    }
    
    public Object onShowEverything()
    {
        showEverything = true;
        return zone.getBody();
    }
    
    public Object onShowRestricted()
    {
        showEverything = false;
        return zone.getBody();
    }
    
    public String getLibraryName()
    {
        return !libraryMapping.libraryName.isEmpty() ? libraryMapping.libraryName : "Webapp's own component library";
    }

}
