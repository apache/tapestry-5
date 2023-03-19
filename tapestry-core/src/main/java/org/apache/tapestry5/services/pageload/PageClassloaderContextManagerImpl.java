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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.internal.services.ComponentDependencyRegistry;
import org.apache.tapestry5.internal.services.InternalComponentInvalidationEventHub;
import org.apache.tapestry5.ioc.annotations.ComponentClasses;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@linkplain PageClassloaderContextManager} implementation.
 *
 * @since 5.8.3
 */
public class PageClassloaderContextManagerImpl implements PageClassloaderContextManager
{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PageClassloaderContextManager.class);
    
    private final ComponentDependencyRegistry componentDependencyRegistry;
    
    private final ComponentClassResolver componentClassResolver;
    
    private final InternalComponentInvalidationEventHub invalidationHub;
    
    private final InvalidationEventHub componentClassesInvalidationEventHub;
    
    private final boolean productionMode;
    
    private final static ThreadLocal<Integer> NESTED_MERGE_COUNT = ThreadLocal.withInitial(() -> 0);
    
    private final static ThreadLocal<Boolean> INVALIDATING_CONTEXT = ThreadLocal.withInitial(() -> false);
    
    private static final AtomicInteger MERGED_COUNTER = new AtomicInteger(1);
    
    private Function<ClassLoader, PlasticProxyFactory> plasticProxyFactoryProvider;
    
    private PageClassloaderContext root;
    
    public PageClassloaderContextManagerImpl(
            final ComponentDependencyRegistry componentDependencyRegistry, 
            final ComponentClassResolver componentClassResolver,
            final InternalComponentInvalidationEventHub invalidationHub,
            final @ComponentClasses InvalidationEventHub componentClassesInvalidationEventHub,
            @Symbol(SymbolConstants.PRODUCTION_MODE) final boolean productionMode) 
    {
        super();
        this.componentDependencyRegistry = componentDependencyRegistry;
        this.componentClassResolver = componentClassResolver;
        this.invalidationHub = invalidationHub;
        this.componentClassesInvalidationEventHub = componentClassesInvalidationEventHub;
        this.productionMode = productionMode;
        invalidationHub.addInvalidationCallback(this::listen);
        NESTED_MERGE_COUNT.set(0);
    }
    
    @Override
    public void initialize(
            final PageClassloaderContext root,
            final Function<ClassLoader, PlasticProxyFactory> plasticProxyFactoryProvider)
    {
        if (this.root != null)
        {
            throw new IllegalStateException("PageClassloaderContextManager.initialize() can only be called once");
        }
        Objects.requireNonNull(root);
        Objects.requireNonNull(plasticProxyFactoryProvider);
        this.root = root;
        this.plasticProxyFactoryProvider = plasticProxyFactoryProvider;
        LOGGER.debug("Root context: {}", root);
    }

    @Override
    public PageClassloaderContext get(final String className)
    {
        PageClassloaderContext context;
        
        if (productionMode)
        {
            root.addClass(className);
            context = root;
        }
        else
        {
            
            final String enclosingClassName = getEnclosingClassName(className);
            context = root.findByClassName(enclosingClassName);
            
            if (context == null)
            {
                Set<String> classesToInvalidate = new HashSet<>();
                
                context = processUsingDependencies(
                        enclosingClassName, 
                        root, 
                        () -> getUnknownContext(root, plasticProxyFactoryProvider),
                        plasticProxyFactoryProvider,
                        classesToInvalidate);
                
                if (!classesToInvalidate.isEmpty())
                {
                    invalidate(classesToInvalidate);
                }

                if (!className.equals(enclosingClassName))
                {
                    context.addClass(className);
                }
                
            }
            
        }
        
        return context;
        
    }

    /**
     * If the given class is an inner class, returns the enclosing class.
     * Otherwise, returns the class name unchanged.
     */
    private String getEnclosingClassName(String className)
    {
        int index = className.indexOf('$');
        return index <= 0 ? className : className.substring(0, index);
    }

    private PageClassloaderContext getUnknownContext(final PageClassloaderContext root,
            final Function<ClassLoader, PlasticProxyFactory> plasticProxyFactoryProvider) {
        PageClassloaderContext unknownContext = null;
        
        for (PageClassloaderContext child : root.getChildren()) 
        {
            if (child.getName().equals(PageClassloaderContext.UNKOWN_CONTEXT_NAME))
            {
                unknownContext = child;
                break;
            }
        }
        
        if (unknownContext == null)
        {
            unknownContext = new PageClassloaderContext(PageClassloaderContext.UNKOWN_CONTEXT_NAME, root, 
                    Collections.emptySet(), 
                    plasticProxyFactoryProvider.apply(root.getClassLoader()));
            root.addChild(unknownContext);
            LOGGER.debug("Unknown context: {}", unknownContext);
        }
        return unknownContext;
    }
    
    private PageClassloaderContext processUsingDependencies(
            String className, 
            PageClassloaderContext root, 
            Supplier<PageClassloaderContext> unknownContextProvider, 
            Function<ClassLoader, PlasticProxyFactory> plasticProxyFactoryProvider, Set<String> classesToInvalidate) 
    {
        return processUsingDependencies(className, root, unknownContextProvider, plasticProxyFactoryProvider, classesToInvalidate, new HashSet<>());
    }


    private PageClassloaderContext processUsingDependencies(
            String className, 
            PageClassloaderContext root, 
            Supplier<PageClassloaderContext> unknownContextProvider, 
            Function<ClassLoader, PlasticProxyFactory> plasticProxyFactoryProvider, Set<String> classesToInvalidate,
            Set<String> alreadyProcessed) 
    {
        PageClassloaderContext context = root.findByClassName(className);
        Set<String> circularDependencies = new HashSet<>(1);
        if (context == null)
        {
            
            // Class isn't in a controlled package, so it doesn't get transformed
            // and should go for the root context, which is never thrown out.
            if (!root.getPlasticManager().shouldInterceptClassLoading(className))
            {
                context = root;
            } else {
                if (
                        !componentDependencyRegistry.contains(className) && 
                        componentDependencyRegistry.getDependents(className).isEmpty())
                {
                    // Make sure you get a fresh version of the class before processing its
                    // dependencies
                    Class<?> clasz;
                    PlasticProxyFactory throwaway = plasticProxyFactoryProvider.apply(root.getClassLoader());
                    try {
                        clasz = throwaway.getClassLoader().loadClass(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    componentDependencyRegistry.register(clasz);
                    alreadyProcessed.remove(className);
                    return processUsingDependencies(className, root, unknownContextProvider, plasticProxyFactoryProvider, classesToInvalidate, alreadyProcessed);
                }
                else 
                {

                    alreadyProcessed.add(className);

                    // Sorting dependencies alphabetically so we have consistent results.
                    List<String> dependencies = new ArrayList<>(componentDependencyRegistry.getDependencies(className));
                    Collections.sort(dependencies);
                    
                    // Process dependencies depth-first
                    for (String dependency : dependencies)
                    {
                        // Avoid infinite recursion loops
                        if (!alreadyProcessed.contains(dependency))
                        {
                            processUsingDependencies(dependency, root, unknownContextProvider, 
                                    plasticProxyFactoryProvider, classesToInvalidate, alreadyProcessed);
                        }
                    }
                    
                    // Collect context dependencies
                    Set<PageClassloaderContext> contextDependencies = new HashSet<>();
                    for (String dependency : dependencies) 
                    {
                        // Direct circular dependency
                        if (componentDependencyRegistry.getDependencies(dependency).contains(className))
                        {
                            circularDependencies.add(dependency);
                        }
                        else
                        {
                            PageClassloaderContext dependencyContext = root.findByClassName(dependency);
                            if (dependencyContext == null)
                            {
                                dependencyContext = processUsingDependencies(dependency, root, unknownContextProvider,
                                        plasticProxyFactoryProvider, classesToInvalidate, alreadyProcessed);

                            }
                            contextDependencies.add(dependencyContext);
                        }
                    }
                    
                    if (contextDependencies.size() == 0)
                    {
                        context = new PageClassloaderContext(
                                getContextName(className), 
                                root, 
                                Collections.singleton(className), 
                                plasticProxyFactoryProvider.apply(root.getClassLoader()));
                    }
                    else 
                    {
                        PageClassloaderContext parentContext;
                        if (contextDependencies.size() == 1)
                        {
                            parentContext = contextDependencies.iterator().next();
                        }
                        else
                        {
                            parentContext = merge(contextDependencies, plasticProxyFactoryProvider, root, classesToInvalidate);
                        }
                        context = new PageClassloaderContext(
                                getContextName(className), 
                                parentContext, 
                                Collections.singleton(className), 
                                plasticProxyFactoryProvider.apply(root.getClassLoader()));
                    }
                    
                    context.getParent().addChild(context);

                    LOGGER.debug("New context: {}", context);
                    
                }
            }
            
        }
        context.addClass(className);
        for (String circularDependency : circularDependencies) 
        {
            context.addClass(circularDependency);
        }
        return context;
    }
    
    private PageClassloaderContext merge(
            Set<PageClassloaderContext> contextDependencies,
            Function<ClassLoader, PlasticProxyFactory> plasticProxyFactoryProvider,
            PageClassloaderContext root, Set<String> classesToInvalidate) 
    {
        
        NESTED_MERGE_COUNT.set(NESTED_MERGE_COUNT.get() + 1);
        
        if (LOGGER.isDebugEnabled())
        {
            
            LOGGER.debug("Nested merge count going up to {}", NESTED_MERGE_COUNT.get());

            String classes;
            StringBuilder builder = new StringBuilder();
            builder.append("Merging the following page classloader contexts into one:\n");
            for (PageClassloaderContext context : contextDependencies) 
            {
                classes = context.getClassNames().stream()
                        .map(this::getContextName)
                        .sorted()
                        .collect(Collectors.joining(", "));
                builder.append(String.format("\t%s (parent %s) (%s)\n", context.getName(), context.getParent().getName(), classes));
            }
            LOGGER.debug(builder.toString().trim());
        }
        
        Set<PageClassloaderContext> allContextsIncludingDescendents = new HashSet<>();
        for (PageClassloaderContext context : contextDependencies) 
        {
            allContextsIncludingDescendents.add(context);
            allContextsIncludingDescendents.addAll(context.getDescendents());
        }

        PageClassloaderContext merged;
        
        // Collect the classes in these dependencies, then invalidate the contexts
        
        Set<PageClassloaderContext> furtherDependencies = new HashSet<>();
        
        Set<String> classNames = new HashSet<>();
        
        for (PageClassloaderContext context : contextDependencies) 
        {
            if (!context.isRoot())
            {
                classNames.addAll(context.getClassNames());
            }
            final PageClassloaderContext parent = context.getParent();
            // We don't want the merged context to have a further dependency on 
            // the root context (it's not mergeable) nor on itself.
            if (!parent.isRoot() && !allContextsIncludingDescendents.contains(parent))
            {
                furtherDependencies.add(parent);
            }
        }
        
        final List<PageClassloaderContext> contextsToInvalidate = contextDependencies.stream()
            .filter(c -> !c.isRoot())
            .collect(Collectors.toList());
        
        if (!contextsToInvalidate.isEmpty())
        {
            classesToInvalidate.addAll(invalidate(contextsToInvalidate.toArray(new PageClassloaderContext[contextsToInvalidate.size()])));
        }
        
        PageClassloaderContext parent;
        
        // No context dependencies, so parent is going to be the root one
        if (furtherDependencies.size() == 0)
        {
            parent = root;
        }
        else 
        {
            // Single shared context dependency, so it's our parent
            if (furtherDependencies.size() == 1)
            {
                parent = furtherDependencies.iterator().next();
            }
            // No single context dependency, so we'll need to recursively merge it
            // so we can have a single parent.
            else
            {
                parent = merge(furtherDependencies, plasticProxyFactoryProvider, root, classesToInvalidate);
                LOGGER.debug("New context: {}", parent);
            }
        }
        
        merged = new PageClassloaderContext(
            "merged " + MERGED_COUNTER.getAndIncrement(),
            parent, 
            classNames, 
            plasticProxyFactoryProvider.apply(parent.getClassLoader()));
        
        parent.addChild(merged);
        
        NESTED_MERGE_COUNT.set(NESTED_MERGE_COUNT.get() - 1);
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug("Nested merge count going down to {}", NESTED_MERGE_COUNT.get());
        }
        
        return merged;
    }

    @Override
    public void clear(String className) 
    {
        final PageClassloaderContext context = root.findByClassName(className);
        if (context != null)
        {
//            invalidationHub.fireInvalidationEvent(new ArrayList<>(invalidate(context)));
            invalidate(context);
        }
    }

    private String getContextName(String className)
    {
        String contextName = componentClassResolver.getLogicalName(className);
        if (contextName == null)
        {
            contextName = className;
        }
        return contextName;
    }

    @Override
    public Set<String> invalidate(PageClassloaderContext ... contexts) 
    {
        Set<String> classNames = new HashSet<>();
        for (PageClassloaderContext context : contexts) {
            addClassNames(context, classNames);
            context.invalidate();
            if (context.getParent() != null)
            {
                context.getParent().removeChildren(context);
            }
        }
        return classNames;
    }
    
    private List<String> listen(List<String> resources)
    {
//        System.out.println("XXXXXX Before PageClassloaderContextManager listen: " + root.toRecursiveString());
        if (INVALIDATING_CONTEXT.get())
        {
            return Collections.emptyList();
        }
        Set<PageClassloaderContext> contextsToInvalidate = new HashSet<>();
        for (String resource : resources) 
        {
            PageClassloaderContext context = root.findByClassName(resource);
            if (context != null && !context.isRoot())
            {
                contextsToInvalidate.add(context);
            }
        }
        
        Set<String> furtherResources = invalidate(contextsToInvalidate.toArray(
                new PageClassloaderContext[contextsToInvalidate.size()]));
        
        // We don't want to invalidate resources more than once
        furtherResources.removeAll(resources);
        
        return new ArrayList<>(furtherResources);
    }

    @Override
    public void invalidateAndFireInvalidationEvents(PageClassloaderContext... contexts) {
        final Set<String> classNames = invalidate(contexts);
        INVALIDATING_CONTEXT.set(true);
        invalidate(classNames);
        INVALIDATING_CONTEXT.set(false);
    }
    
    private void invalidate(Set<String> classesToInvalidate) {
        if (!classesToInvalidate.isEmpty())
        {
            LOGGER.debug("Invalidating classes {}", classesToInvalidate);
            INVALIDATING_CONTEXT.set(true);
            final ArrayList<String> classesToInvalidateAsList = new ArrayList<>(classesToInvalidate);
            invalidationHub.fireInvalidationEvent(classesToInvalidateAsList);
            componentClassesInvalidationEventHub.fireInvalidationEvent(classesToInvalidateAsList);
            INVALIDATING_CONTEXT.set(false);
        }
    }

    private void addClassNames(PageClassloaderContext context, Set<String> classNames) {
        classNames.addAll(context.getClassNames());
        for (PageClassloaderContext child : context.getChildren()) {
            addClassNames(child, classNames);
        }
    }

    @Override
    public PageClassloaderContext getRoot() {
        return root;
    }

    @Override
    public boolean isMerging() 
    {
        return NESTED_MERGE_COUNT.get() > 0;
    }

    @Override
    public void clear() 
    {
    }

}
