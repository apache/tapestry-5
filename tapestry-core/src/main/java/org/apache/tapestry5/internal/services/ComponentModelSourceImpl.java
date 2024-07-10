// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import java.util.Set;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.services.ComponentDependencyRegistry.DependencyType;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.services.ComponentClassResolver;

public class ComponentModelSourceImpl implements ComponentModelSource
{
    private final ComponentClassResolver resolver;

    private final ComponentInstantiatorSource source;
    
    private final ComponentDependencyRegistry componentDependencyRegistry;
    
    private final PageSource pageSource;
    
    private final boolean multipleClassLoaders;

    public ComponentModelSourceImpl(ComponentClassResolver resolver, ComponentInstantiatorSource source,
            ComponentDependencyRegistry componentDependencyRegistry,
            PageSource pageSource,
            @Symbol(SymbolConstants.PRODUCTION_MODE) boolean productionMode,
            @Symbol(SymbolConstants.MULTIPLE_CLASSLOADERS) boolean multipleClassLoaders)
    {
        this.resolver = resolver;
        this.source = source;
        this.componentDependencyRegistry = componentDependencyRegistry;
        this.pageSource = pageSource;
        this.multipleClassLoaders = !productionMode && multipleClassLoaders;
    }

    public ComponentModel getModel(String componentClassName)
    {
        if (multipleClassLoaders && isPage(componentClassName))
        {
            
            final Set<String> superclasses = componentDependencyRegistry.getDependencies(
                    componentClassName, DependencyType.SUPERCLASS);
            
            if (!superclasses.isEmpty())
            {
                final String superclass = superclasses.iterator().next();
                if (isPage(superclass))
                {
                    getModel(superclass);
                    try
                    {
                        pageSource.getPage(resolver.getLogicalName(componentClassName));
                    }
                    catch (IllegalStateException e)
                    {
                        // This can be thrown in PageSourceImpl in case an
                        // infinite method call recursion is detected. In
                        // that case, the page instance is already created,
                        // so the objective of the line above is already
                        // fulfilled and we can safely ignore the exception
                    }
                }
            }
        }
        return source.getInstantiator(componentClassName).getModel();
    }

    public ComponentModel getPageModel(String pageName)
    {
        return getModel(resolver.resolvePageNameToClassName(pageName));
    }
    
    private boolean isPage(String componentClassName)
    {
        return componentClassName.contains(".pages.");
    }
}
