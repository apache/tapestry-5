// Copyright 2023, 2024 The Apache Software Foundation
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
import java.util.Comparator;
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
import org.apache.tapestry5.internal.ThrowawayClassLoader;
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
    
    private final static ThreadLocal<AtomicInteger> CONTEXTS_CREATED = ThreadLocal.withInitial(AtomicInteger::new);
    
    private Function<ClassLoader, PlasticProxyFactory> plasticProxyFactoryProvider;
    
    private PageClassLoaderContext root;
    
    private boolean preloadingContexts;

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
        this.multipleClassLoaders = multipleClassLoaders && !productionMode;
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
        
        // Class isn't in a controlled package, so it doesn't get transformed
        // and should go for the root context, which is never thrown out.
        if (!root.getPlasticManager().shouldInterceptClassLoading(className))
        {
            context = root;
        }
        else if (productionMode || !multipleClassLoaders)
        {
            context = getUnknownContext(root, plasticProxyFactoryProvider);
        }
        else
        {
            
            // Multiple classloader mode.
            final String enclosingClassName = getAdjustedClassName(className);
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
                    loadClass(enclosingClassName, context);
                }
                
            }
            
        }
        
        return context;
        
    }

    private String getAdjustedClassName(final String className) 
    {
        return PlasticUtils.getEnclosingClassName(className)
                .replaceAll("\\[\\]", "");
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
            alreadyProcessed.add(className);
            
            // Sorting dependencies by type/alphabetically so we have consistent 
            // context trees between runs of the same webapp
            List<String> allNonPageDependencies = new ArrayList<>(
                    componentDependencyRegistry.getAllNonPageDependencies(className));
            Collections.sort(allNonPageDependencies, ClassNameComparator.INSTANCE);
            
            List<String> dependencies = new ArrayList<>(getDependenciesWithoutPages(className));
            Collections.sort(dependencies, ClassNameComparator.INSTANCE);
            
            // Process dependencies depth-first
            do
            {
                
                // Very unlikely to have infinite loops, but lets
                // avoid them anyway.
                int passes = 0;
                
                int contextsCreatedInThisPass = -1;
                
                while (contextsCreatedInThisPass < CONTEXTS_CREATED.get().get() && passes < 1000)
                {
                    
                    contextsCreatedInThisPass = CONTEXTS_CREATED.get().get();
            
                    for (String dependency : allNonPageDependencies)
                    {
                        // Avoid infinite recursion loops
                        if (!alreadyProcessed.contains(dependency)  
                                || root.findByClassName(dependency) == null)
                        {
                            processUsingDependencies(dependency, root, unknownContextProvider, 
                                    plasticProxyFactoryProvider, classesToInvalidate, alreadyProcessed, false);
                        }
                    }
                    
                }
                
            }
            while (!allNeededContextsAvailable(allNonPageDependencies));
            
            // Collect context dependencies
            Set<PageClassLoaderContext> contextDependencies = new HashSet<>();
            for (String dependency : allNonPageDependencies) 
            {
                PageClassLoaderContext dependencyContext = root.findByClassName(dependency);
                // Avoid infinite recursion loops
                if (multipleClassLoaders || !alreadyProcessed.contains(dependency))
                {
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
            }
            
            if (contextDependencies.size() == 0)
            {
                context = new PageClassLoaderContext(
                        getContextName(className), 
                        root, 
                        Collections.singleton(className), 
                        plasticProxyFactoryProvider.apply(root.getClassLoader()),
                        this::get);
                CONTEXTS_CREATED.get().incrementAndGet();
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
                CONTEXTS_CREATED.get().incrementAndGet();
            }

            LOGGER.debug("New context: {}", context);
            
        }
        context.addClass(className);
        context.getParent().addChild(context);
        
        return context;
    }

    private boolean allNeededContextsAvailable(List<String> dependencies) 
    {
        boolean available = true;
        for (String dependency : dependencies)
        {
            if (root.findByClassName(dependency) == null)
            {
                available = false;
                break;
            }
        }
        return available;
    }

    private Set<String> getDependenciesWithoutPages(String className) 
    {
        Set<String> dependencies = new HashSet<>();
        dependencies.addAll(componentDependencyRegistry.getDependencies(className, DependencyType.USAGE));
        dependencies.addAll(componentDependencyRegistry.getDependencies(className, DependencyType.SUPERCLASS));
        dependencies.remove(className); // Just in case
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

        final Set<String> classesToReprocess = multipleClassLoaders ? new HashSet<>() : Collections.emptySet();
        Set<PageClassLoaderContext> allContextsIncludingDescendents = new HashSet<>();
        
        for (PageClassLoaderContext context : contextDependencies) 
        {
            final Set<PageClassLoaderContext> descendents = context.getDescendents();
            allContextsIncludingDescendents.add(context);
            allContextsIncludingDescendents.addAll(descendents);
            for (PageClassLoaderContext descendent : descendents) 
            {
                addClassNames(descendent, classesToReprocess);
            }
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
        
        CONTEXTS_CREATED.get().incrementAndGet();
        
        parent.addChild(merged);
        
        // Recreating contexts for classes that got invalidated but
        // aren't part of the new merged context (i.e. the classes
        // in contexts are are descendent of the merged contexts).
//        if (!classesToReprocess.isEmpty())
//        {
//            final List<String> sorted = new ArrayList<>(classesToReprocess);
//            for (String className : sorted) 
//            {
//                get(className);
//            }
//        }
        
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
        if (!classesToInvalidate.isEmpty() && !preloadingContexts)
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
        
        final ClassLoader classLoader = new ThrowawayClassLoader(PageClassLoaderContext.class.getClassLoader());
        
        final List<String> pageNames = componentClassResolver.getPageNames();
        final List<String> classNames = new ArrayList<>(pageNames.size());
        
        long start = System.currentTimeMillis();
        
        LOGGER.info("Preloading dependency information for {} pages", pageNames.size());
        
        for (String page : pageNames)
        {
            try 
            {
                final String className = componentClassResolver.resolvePageNameToClassName(page);
                componentDependencyRegistry.register(classLoader.loadClass(className));
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
            LOGGER.info(String.format("Dependency information for %d pages gathered in %.3f s", 
                    pageNames.size(),  (finish - start) / 1000.0));
        }
        
        preloadContexts();
        
    }
    
    @Override
    public void preloadContexts() 
    {
        long start;
        long finish;
        LOGGER.info("Starting preloading page classloader contexts");
        
        start = System.currentTimeMillis();
        
        final List<String> classNames = new ArrayList<>(componentDependencyRegistry.getClassNames());
        classNames.sort(ClassNameComparator.INSTANCE);
        
        int runs = 0;
        preloadingContexts = true;
        
        try 
        {
            // The run counter check is to just avoid possible infinite loops,
            // although that's very unlikely.
            int contexts = -1;
            while (runs < 5000 && contexts < CONTEXTS_CREATED.get().get())
            {
                runs++;
                contexts = CONTEXTS_CREATED.get().get();
                for (String className : classNames) 
                {
                    get(className);
                }
            }
        }
        finally
        {
            preloadingContexts = false;
        }
        
        finish = System.currentTimeMillis();

        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info(String.format("Preloading of page classloader contexts finished in %.3f s (%d passes)", (finish - start) / 1000.0, runs));
        }
    }
    
    /**
     * Sorts base classes before mixins, mixins before components and components
     * before pages. If both classes belong to the same type, order alphabetically.
     */
    private static final class ClassNameComparator implements Comparator<String> 
    {
        
        private static final Comparator<String> INSTANCE = new ClassNameComparator();

        @Override
        public int compare(String o1, String o2) 
        {
            int value1 = getValue(o1);
            int value2 = getValue(o2);
            int comparison = value1 - value2;
            if (comparison == 0)
            {
                comparison = o1.compareTo(o2);
            }
            return comparison;
        }
        
        private int getValue(String className)
        {
            int value;
            if (className.contains(".base."))
            {
                value = 0;
            }
            else if (className.contains(".mixins."))
            {
                value = 1;
            }
            else if (className.contains(".components."))
            {
                value = 2;
            }
            else
            {
                value = 3;
            }
            return value;
        }
        
    }
    
}
