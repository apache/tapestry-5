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
import java.util.List;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.UnknownActivationContextCheck;
import org.apache.tapestry5.annotations.WhitelistAccessOnly;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentLibraryInfo;

/**
 * Page used to describe the component libraries being used in the application.
 * Notice: the implementation of this page was done to avoid creating components, so the
 * Tapestry 5 Core Library didn't get polluted with internal-only components.
 */
@UnknownActivationContextCheck(false)
@WhitelistAccessOnly
public class ComponentLibraries
{
    
    private static enum Type { PAGE, COMPONENT, MIXIN }

    @Inject
    private ComponentClassResolver componentClassResolver;

    @Property
    private String libraryName;

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
    
    @Cached(watch="libraryName")
    public ComponentLibraryInfo getInfo()
    {
        return componentClassResolver.getComponentLibraryInfo(libraryName);
    }
    
    public List<String> getLibraryNames() {
        return componentClassResolver.getLibraryNames();
    }
    
    public String getLibraryClientId() 
    {
        return libraryName.replace("/", "-");
    }

    private List<String> filter(final List<String> allNames)
    {
        List<String> logicalNames = new ArrayList<String>();
        for (String name : allNames)
        {
            
            if (name.startsWith(libraryName + "/") && !(libraryName.equals("core") && name.endsWith("Test")))
            {
                logicalNames.add(name);
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
        final String className;
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

}
