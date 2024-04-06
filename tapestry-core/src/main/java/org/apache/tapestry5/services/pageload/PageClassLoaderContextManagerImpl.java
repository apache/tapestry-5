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
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.internal.services.ComponentDependencyRegistry;
import org.apache.tapestry5.internal.services.ComponentDependencyRegistry.DependencyType;
import org.apache.tapestry5.internal.services.InternalComponentInvalidationEventHub;
import org.apache.tapestry5.ioc.annotations.ComponentClasses;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.plastic.PlasticUtils;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@linkplain PageClassLoaderContextManager} implementation.
 *
 * @since 5.8.3
 */
public class PageClassLoaderContextManagerImpl implements PageClassLoaderContextManager
{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PageClassLoaderContextManager.class);
    
    private final ComponentDependencyRegistry componentDependencyRegistry;
    
    private final ComponentClassResolver componentClassResolver;
    
    private final InternalComponentInvalidationEventHub invalidationHub;
    
    private final InvalidationEventHub componentClassesInvalidationEventHub;
    
    private final boolean multipleClassLoaders;
    
    private final boolean productionMode;
    
    private final static ThreadLocal<Integer> NESTED_MERGE_COUNT = ThreadLocal.withInitial(() -> 0);
    
    private final static ThreadLocal<Boolean> INVALIDATING_CONTEXT = ThreadLocal.withInitial(() -> false);
    
    private static final AtomicInteger MERGED_COUNTER = new AtomicInteger(1);
    
    private Function<ClassLoader, PlasticProxyFactory> plasticProxyFactoryProvider;
    
    private PageClassLoaderContext root;
    
    public PageClassLoaderContextManagerImpl(
            final ComponentDependencyRegistry componentDependencyRegistry, 
            final ComponentClassResolver componentClassResolver,
            final InternalComponentInvalidationEventHub invalidationHub,
            final @ComponentClasses InvalidationEventHub componentClassesInvalidationEventHub,
            final @Symbol(SymbolConstants.PRODUCTION_MODE) boolean productionMode,
            final @Symbol(SymbolConstants.MULTIPLE_CLASSLOADERS) boolean multipleClassLoaders) 
    {
        super();
        this.componentDependencyRegistry = componentDependencyRegistry;
        this.componentClassResolver = componentClassResolver;
        this.invalidationHub = invalidationHub;
        this.componentClassesInvalidationEventHub = componentClassesInvalidationEventHub;
        this.multipleClassLoaders = multipleClassLoaders;
        this.productionMode = productionMode;
        invalidationHub.addInvalidationCallback(this::listen);
        NESTED_MERGE_COUNT.set(0);
    }
    
    @Override
    public void invalidateUnknownContext()
    {
        synchronized (this) {
            markAsNotInvalidatingContext();
            for (PageClassLoaderContext context : root.getChildren())
            {
                if (context.isUnknown())
                {
                    invalidateAndFireInvalidationEvents(context);
                    break;
                }
            }
        }
    }
    
    @Override
    public void initialize(
            final PageClassLoaderContext root,
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
        LOGGER.info("Root context: {}", root);
    }

    @Override
    public synchronized PageClassLoaderContext get(final String className)
    {
        PageClassLoaderContext context;
        
        final String enclosingClassName = PlasticUtils.getEnclosingClassName(className);
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
                loadClass(className, context);
            }
            
        }
        
        return context;
        
    }

    private PageClassLoaderContext getUnknownContext(final PageClassLoaderContext root,
            final Function<ClassLoader, PlasticProxyFactory> plasticProxyFactoryProvider) 
    {
        
        PageClassLoaderContext unknownContext = null;
        
        for (PageClassLoaderContext child : root.getChildren()) 
        {
            if (child.getName().equals(PageClassLoaderContext.UNKOWN_CONTEXT_NAME))
            {
                unknownContext = child;
                break;
            }
        }
        
        if (unknownContext == null)
        {
            unknownContext = new PageClassLoaderContext(PageClassLoaderContext.UNKOWN_CONTEXT_NAME, root, 
                    Collections.emptySet(), 
                    plasticProxyFactoryProvider.apply(root.getClassLoader()),
                    this::get);
            root.addChild(unknownContext);
            if (multipleClassLoaders)
            {
                LOGGER.debug("Unknown context: {}", unknownContext);
            }
        }
        return unknownContext;
    }
    
    private PageClassLoaderContext processUsingDependencies(
            String className, 
            PageClassLoaderContext root, 
            Supplier<PageClassLoaderContext> unknownContextProvider, 
            Function<ClassLoader, PlasticProxyFactory> plasticProxyFactoryProvider, Set<String> classesToInvalidate) 
    {
        return processUsingDependencies(className, root, unknownContextProvider, plasticProxyFactoryProvider, classesToInvalidate, new HashSet<>());
    }

    private PageClassLoaderContext processUsingDependencies(
            String className, 
            PageClassLoaderContext root, 
            Supplier<PageClassLoaderContext> unknownContextProvider, 
            Function<ClassLoader, PlasticProxyFactory> plasticProxyFactoryProvider, 
            Set<String> classesToInvalidate,
            Set<String> alreadyProcessed) 
    {
        return processUsingDependencies(className, root, unknownContextProvider, 
                plasticProxyFactoryProvider, classesToInvalidate, alreadyProcessed, true);
    }


    private PageClassLoaderContext processUsingDependencies(
            String className, 
            PageClassLoaderContext root, 
            Supplier<PageClassLoaderContext> unknownContextProvider, 
            Function<ClassLoader, PlasticProxyFactory> plasticProxyFactoryProvider, 
            Set<String> classesToInvalidate,
            Set<String> alreadyProcessed,
            boolean processCircularDependencies) 
    {
        PageClassLoaderContext context = root.findByClassName(className);
        if (context == null)
        {
            
            LOGGER.debug("Processing class {}", className);
            
            // Class isn't in a controlled package, so it doesn't get transformed
            // and should go for the root context, which is never thrown out.
            if (!root.getPlasticManager().shouldInterceptClassLoading(className))
            {
                context = root;
            } else {
                if (!productionMode && (
                        !componentDependencyRegistry.contains(className) ||
                        !multipleClassLoaders))
                {
                    context = unknownContextProvider.get();
                }
                else 
                {

                    alreadyProcessed.add(className);
                    
                    // Sorting dependencies alphabetically so we have consistent results.
                    List<String> dependencies = new ArrayList<>(getDependenciesWithoutPages(className));
                    Collections.sort(dependencies);
                    
                    // Process dependencies depth-first
                    for (String dependency : dependencies)
                    {
                        // Avoid infinite recursion loops
                        if (!alreadyProcessed.contains(dependency)/* && 
                                !circularDependencies.contains(dependency)*/)
                        {
                            processUsingDependencies(dependency, root, unknownContextProvider, 
                                    plasticProxyFactoryProvider, classesToInvalidate, alreadyProcessed, false);
                        }
                    }
                    
                    // Collect context dependencies
                    Set<PageClassLoaderContext> contextDependencies = new HashSet<>();
                    for (String dependency : dependencies) 
                    {
                        PageClassLoaderContext dependencyContext = root.findByClassName(dependency);
                        if (dependencyContext == null)
                        {
                            dependencyContext = processUsingDependencies(dependency, root, unknownContextProvider,
                                    plasticProxyFactoryProvider, classesToInvalidate, alreadyProcessed);

                        }
                        if (!dependencyContext.isRoot())
                        {
                            contextDependencies.add(dependencyContext);
                        }
                    }
                    
                    if (!multipleClassLoaders)
                    {
                        context = root;
                    }
                    else if (contextDependencies.size() == 0)
                    {
                        context = new PageClassLoaderContext(
                                getContextName(className), 
                                root, 
                                Collections.singleton(className), 
                                plasticProxyFactoryProvider.apply(root.getClassLoader()),
                                this::get);
                    }
                    else 
                    {
                        PageClassLoaderContext parentContext;
                        if (contextDependencies.size() == 1)
                        {
                            parentContext = contextDependencies.iterator().next();
                        }
                        else
                        {
                            parentContext = merge(contextDependencies, plasticProxyFactoryProvider, root, classesToInvalidate);
                        }
                        context = new PageClassLoaderContext(
                                getContextName(className), 
                                parentContext, 
                                Collections.singleton(className), 
                                plasticProxyFactoryProvider.apply(parentContext.getClassLoader()),
                                this::get);
                    }

                    if (multipleClassLoaders)
                    {
                        context.getParent().addChild(context);
                    }
                    
                    // Ensure non-page class is initialized in the correct context and classloader.
                    // Pages get their own context and classloader, so this initialization
                    // is both non-needed and a cause for an NPE if it happens.
                    if (!componentClassResolver.isPage(className)
                            || componentDependencyRegistry.getDependencies(className, DependencyType.USAGE).isEmpty())
                    {
                        loadClass(className, context);
                    }

                    if (multipleClassLoaders)
                    {
                        LOGGER.debug("New context: {}", context);
                    }
                    
                }
            }
            
        }
        context.addClass(className);
        
        return context;
    }

    private Set<String> getDependenciesWithoutPages(String className) 
    {
        Set<String> dependencies = new HashSet<>();
        dependencies.addAll(componentDependencyRegistry.getDependencies(className, DependencyType.USAGE));
        dependencies.addAll(componentDependencyRegistry.getDependencies(className, DependencyType.SUPERCLASS));
        return Collections.unmodifiableSet(dependencies);
    }

    private Class<?> loadClass(String className, PageClassLoaderContext context) 
    {
        try 
        {
            final ClassLoader classLoader = context.getPlasticManager().getClassLoader();
            return classLoader.loadClass(className);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private PageClassLoaderContext merge(
            Set<PageClassLoaderContext> contextDependencies,
            Function<ClassLoader, PlasticProxyFactory> plasticProxyFactoryProvider,
            PageClassLoaderContext root, Set<String> classesToInvalidate) 
    {
        
        NESTED_MERGE_COUNT.set(NESTED_MERGE_COUNT.get() + 1);
        
        if (LOGGER.isDebugEnabled())
        {
            
            LOGGER.debug("Nested merge count going up to {}", NESTED_MERGE_COUNT.get());

            String classes;
            StringBuilder builder = new StringBuilder();
            builder.append("Merging the following page classloader contexts into one:\n");
            for (PageClassLoaderContext context : contextDependencies) 
            {
                classes = context.getClassNames().stream()
                        .map(this::getContextName)
                        .sorted()
                        .collect(Collectors.joining(", "));
                builder.append(String.format("\t%s (parent %s) (%s)\n", context.getName(), context.getParent().getName(), classes));
            }
            LOGGER.debug(builder.toString().trim());
        }
        
        Set<PageClassLoaderContext> allContextsIncludingDescendents = new HashSet<>();
        for (PageClassLoaderContext context : contextDependencies) 
        {
            allContextsIncludingDescendents.add(context);
            allContextsIncludingDescendents.addAll(context.getDescendents());
        }

        PageClassLoaderContext merged;
        
        // Collect the classes in these dependencies, then invalidate the contexts
        
        Set<PageClassLoaderContext> furtherDependencies = new HashSet<>();
        
        Set<String> classNames = new HashSet<>();
        
        for (PageClassLoaderContext context : contextDependencies) 
        {
            if (!context.isRoot())
            {
                classNames.addAll(context.getClassNames());
            }
            final PageClassLoaderContext parent = context.getParent();
            // We don't want the merged context to have a further dependency on 
            // the root context (it's not mergeable) nor on itself.
            if (!parent.isRoot() && 
                    !allContextsIncludingDescendents.contains(parent))
            {
                furtherDependencies.add(parent);
            }
        }
        
        final List<PageClassLoaderContext> contextsToInvalidate = contextDependencies.stream()
            .filter(c -> !c.isRoot())
            .collect(Collectors.toList());
        
        if (!contextsToInvalidate.isEmpty())
        {
            classesToInvalidate.addAll(invalidate(contextsToInvalidate.toArray(new PageClassLoaderContext[contextsToInvalidate.size()])));
        }
        
        PageClassLoaderContext parent;
        
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
        
        merged = new PageClassLoaderContext(
            "merged " + MERGED_COUNTER.getAndIncrement(),
            parent, 
            classNames, 
            plasticProxyFactoryProvider.apply(parent.getClassLoader()),
            this::get);
        
        parent.addChild(merged);
        
//        for (String className : classNames) 
//        {
//            loadClass(className, merged);
//        }
        
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
        final PageClassLoaderContext context = root.findByClassName(className);
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
    public Set<String> invalidate(PageClassLoaderContext ... contexts) 
    {
        Set<String> classNames = new HashSet<>();
        for (PageClassLoaderContext context : contexts) {
            addClassNames(context, classNames);
            context.invalidate();
            if (context.getParent() != null)
            {
                context.getParent().removeChild(context);
            }
        }
        return classNames;
    }
    
    private List<String> listen(List<String> resources)
    {

        List<String> returnValue;
        
        if (!multipleClassLoaders)
        {
            for (PageClassLoaderContext context : root.getChildren()) 
            {
                context.invalidate();
            }
            returnValue = Collections.emptyList();
        }
        else if (INVALIDATING_CONTEXT.get())
        {
            returnValue = Collections.emptyList();
        }
        else
        {
        
            Set<PageClassLoaderContext> contextsToInvalidate = new HashSet<>();
            for (String resource : resources) 
            {
                PageClassLoaderContext context = root.findByClassName(resource);
                if (context != null && !context.isRoot())
                {
                    contextsToInvalidate.add(context);
                }
            }
            
            Set<String> furtherResources = invalidate(contextsToInvalidate.toArray(
                    new PageClassLoaderContext[contextsToInvalidate.size()]));
            
            // We don't want to invalidate resources more than once
            furtherResources.removeAll(resources);
            
            returnValue = new ArrayList<>(furtherResources);
        }
        
        return returnValue;
            
    }

    @SuppressWarnings("unchecked")
    @Override
    public void invalidateAndFireInvalidationEvents(PageClassLoaderContext... contexts) {
        markAsInvalidatingContext();
        if (multipleClassLoaders)
        {
            final Set<String> classNames = invalidate(contexts);
            invalidate(classNames);
        }
        else
        {
            invalidate(Collections.EMPTY_SET);            
        }
        markAsNotInvalidatingContext();
    }

    private void markAsNotInvalidatingContext() {
        INVALIDATING_CONTEXT.set(false);
    }

    private void markAsInvalidatingContext() {
        INVALIDATING_CONTEXT.set(true);
    }
    
    private void invalidate(Set<String> classesToInvalidate) {
        if (!classesToInvalidate.isEmpty())
        {
            LOGGER.debug("Invalidating classes {}", classesToInvalidate);
            markAsInvalidatingContext();
            final List<String> classesToInvalidateAsList = new ArrayList<>(classesToInvalidate);
            
            componentDependencyRegistry.disableInvalidations();
            
            try 
            {
                // TODO: do we really need both invalidation hubs to be invoked here?
                invalidationHub.fireInvalidationEvent(classesToInvalidateAsList);
                componentClassesInvalidationEventHub.fireInvalidationEvent(classesToInvalidateAsList);
                markAsNotInvalidatingContext();
            }
            finally
            {
                componentDependencyRegistry.enableInvalidations();
            }
            
        }
    }

    private void addClassNames(PageClassLoaderContext context, Set<String> classNames) {
        classNames.addAll(context.getClassNames());
        for (PageClassLoaderContext child : context.getChildren()) {
            addClassNames(child, classNames);
        }
    }

    @Override
    public PageClassLoaderContext getRoot() {
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

    @Override
    public Class<?> getClassInstance(Class<?> clasz, String pageName) 
    {
        final String className = clasz.getName();
        PageClassLoaderContext context = root.findByClassName(className);
        if (context == null)
        {
            context = get(className);
        }
        try 
        {
            clasz = context.getProxyFactory().getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) 
        {
            throw new TapestryException(e.getMessage(), e);
        }
        return clasz;
    }
    
    @Override
    public void preload() 
    {
        
        final PageClassLoaderContext context = new PageClassLoaderContext(PageClassLoaderContext.UNKOWN_CONTEXT_NAME, root, 
                Collections.emptySet(), 
                plasticProxyFactoryProvider.apply(root.getClassLoader()),
                this::get);
        
        final List<String> pageNames = componentClassResolver.getPageNames();
        final List<String> classNames = new ArrayList<>(pageNames.size());
        
        long start = System.currentTimeMillis();
        
        LOGGER.info("Preloading dependency information for {} pages", pageNames.size());
        
        for (String page : pageNames)
        {
            try 
            {
                final String className = componentClassResolver.resolvePageNameToClassName(page);
                componentDependencyRegistry.register(context.getClassLoader().loadClass(className));
                classNames.add(className);
            } catch (ClassNotFoundException e) 
            {
                throw new RuntimeException(e);
            }
            catch (Exception e)
            {
                LOGGER.warn("Exception while preloading page " + page, e);
            }
        }
        
        long finish = System.currentTimeMillis();
        
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info(String.format("Dependency information gathered in %.3f ms", (finish - start) / 1000.0));
        }
        
        context.invalidate();
        
        LOGGER.info("Starting preloading page classloader contexts");
        
        start = System.currentTimeMillis();
        
        for (int i = 0; i < 10; i++)
        {
            for (String className : classNames) 
            {
                get(className);
            }
        }
        
        finish = System.currentTimeMillis();

        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info(String.format("Preloading of page classloadercontexts finished in %.3f ms", (finish - start) / 1000.0));
        }

    }
    
}
