// Copyright 2023 The Apache Software Foundation
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
package org.apache.tapestry5.services.pageload;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.internal.services.ComponentDependencyRegistry;
import org.apache.tapestry5.ioc.annotations.Symbol;

/**
 * Default {@linkplain PageClassloaderContextManager} implementation.
 *
 * @since 5.8.3
 */
public class PageClassloaderContextManagerImpl implements PageClassloaderContextManager
{
    
    private static final String UNKOWN_CONTEXT_NAME = "unknown";
    
    private final ComponentDependencyRegistry componentDependencyRegistry;
    
    private final boolean productionMode;
    
    public PageClassloaderContextManagerImpl(
            final ComponentDependencyRegistry componentDependencyRegistry, 
            @Symbol(SymbolConstants.PRODUCTION_MODE) final boolean productionMode) 
    {
        super();
        this.componentDependencyRegistry = componentDependencyRegistry;
        this.productionMode = productionMode;
    }

    @Override
    public PageClassloaderContext get(
            final String className, 
            final PageClassloaderContext root,
            final Function<ClassLoader, PlasticProxyFactory> plasticProxyFactoryProvider)
    {
        PageClassloaderContext context;
        
        if (productionMode)
        {
            root.addClass(className);
            context = root;
        }
        else
        {
            
            context = root.findByClassName(className);
            
            if (context == null)
            {
                context = processUsingDependencies(className, root, () -> getUnknownContext(root, plasticProxyFactoryProvider));
            }
            
        }
        
        return context;
        
    }

    private PageClassloaderContext getUnknownContext(final PageClassloaderContext root,
            final Function<ClassLoader, PlasticProxyFactory> plasticProxyFactoryProvider) {
        PageClassloaderContext unknownContext = null;
        
        for (PageClassloaderContext child : root.getChildren()) 
        {
            if (child.getName().equals(UNKOWN_CONTEXT_NAME))
            {
                unknownContext = child;
                break;
            }
        }
        
        if (unknownContext == null)
        {
            unknownContext = new PageClassloaderContext(UNKOWN_CONTEXT_NAME, root, 
                    Collections.emptySet(), 
                    plasticProxyFactoryProvider.apply(root.getClassLoader()));
            root.addChildren(unknownContext);
        }
        return unknownContext;
    }

    private PageClassloaderContext processUsingDependencies(String className, PageClassloaderContext root, Supplier<PageClassloaderContext> unknownContextProvider) 
    {
        PageClassloaderContext context = root.findByClassName(className);
        if (context == null)
        {
            
            // Class isn't in a controlled package, so it doesn't get transformed
            // and should go for the root context, which is never thrown out.
            if (!root.getPlasticManager().shouldInterceptClassLoading(className))
            {
                context = root;
            }
            // If we don't have dependency information about this class, it goes
            // into the "unknown" context.
            else if (!componentDependencyRegistry.contains(className))
            {
                context = unknownContextProvider.get();
            }
            // TODO: implement this correctly by creating and merging new contexts
            // as needed.
            else 
            {
                context = unknownContextProvider.get();
            }
            
        }
        context.addClass(className);
        return context;
    }

    @Override
    public Set<String> invalidate(PageClassloaderContext context) 
    {
        Set<String> classNames = new HashSet<>();
        addClassNames(context, classNames);
        context.invalidate();
        return classNames;
    }

    private void addClassNames(PageClassloaderContext context, Set<String> classNames) {
        classNames.addAll(context.getClassNames());
        for (PageClassloaderContext child : context.getChildren()) {
            addClassNames(child, classNames);
        }
    }

    @Override
    public void clear() 
    {
    }

}
