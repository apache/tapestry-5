// Copyright 2022, 2023, 2024 The Apache Software Foundation
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
package org.apache.tapestry5.internal.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Mixin;
import org.apache.tapestry5.annotations.MixinClasses;
import org.apache.tapestry5.annotations.Mixins;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.commons.util.UnknownValueException;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.ThrowawayClassLoader;
import org.apache.tapestry5.internal.parser.ComponentTemplate;
import org.apache.tapestry5.internal.parser.StartComponentToken;
import org.apache.tapestry5.internal.parser.TemplateToken;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.ioc.Orderable;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.PerthreadManager;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.EmbeddedComponentModel;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.model.ParameterModel;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.PlasticManager;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.pageload.PageClassLoaderContextManager;
import org.apache.tapestry5.services.templates.ComponentTemplateLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class ComponentDependencyRegistryImpl implements ComponentDependencyRegistry 
{
    
    private static final List<String> EMPTY_LIST = Collections.emptyList();

    final private PageClassLoaderContextManager pageClassLoaderContextManager;
    
    private static final String META_ATTRIBUTE = "injectedComponentDependencies";
    
    private static final String META_ATTRIBUTE_SEPARATOR = ",";
    
    private static final String NO_DEPENDENCY = "NONE";
    
    // Key is a component, values are the components that depend on it.
    final private Map<String, Set<Dependency>> map;
    
    // Cache to check which classes were already processed or not.
    final private Set<String> alreadyProcessed;
    
    final private File storedDependencies;
    
    final private static ThreadLocal<Integer> INVALIDATIONS_DISABLED = ThreadLocal.withInitial(() -> 0);
    
    final private PlasticManager plasticManager;
    
    final private ComponentClassResolver resolver;
    
    final private TemplateParser templateParser;
    
    final private Map<String, Boolean> isPageCache = new WeakHashMap<>();
    
    final private ComponentTemplateLocator componentTemplateLocator;
    
    final private boolean storedDependencyInformationPresent;
    
    private boolean enableEnsureClassIsAlreadyProcessed = true;
    
    public ComponentDependencyRegistryImpl(
            final PageClassLoaderContextManager pageClassLoaderContextManager,
            final PlasticManager plasticManager,
            final ComponentClassResolver componentClassResolver,
            final TemplateParser templateParser,
            final ComponentTemplateLocator componentTemplateLocator,
            final @Symbol(SymbolConstants.COMPONENT_DEPENDENCY_FILE) String componentDependencyFile,
            final @Symbol(SymbolConstants.PRODUCTION_MODE) boolean productionMode)
    {
        this.pageClassLoaderContextManager = pageClassLoaderContextManager;
        map = new HashMap<>();
        alreadyProcessed = new HashSet<>();
        this.plasticManager = plasticManager;
        this.resolver = componentClassResolver;
        this.templateParser = templateParser;
        this.componentTemplateLocator = componentTemplateLocator;
        
        if (!productionMode)
        {
        
            Logger logger = LoggerFactory.getLogger(ComponentDependencyRegistry.class);
            
            storedDependencies = new File(componentDependencyFile);
            final boolean fileExists = storedDependencies.exists();
            
            logger.info("Component dependencies file: {} Found? {}", 
                    storedDependencies.getAbsolutePath(), fileExists);
            
            if (fileExists)
            {
                try (FileReader fileReader = new FileReader(storedDependencies);
                        BufferedReader reader = new BufferedReader(fileReader))
                {
                    StringBuilder builder = new StringBuilder();
                    String line = reader.readLine();
                    while (line != null)
                    {
                        builder.append(line);
                        line = reader.readLine();
                    }
                    JSONArray jsonArray = new JSONArray(builder.toString());
                    for (int i = 0; i < jsonArray.size(); i++)
                    {
                        final JSONObject jsonObject = jsonArray.getJSONObject(i);
                        final String className = jsonObject.getString("class");
                        final String type = jsonObject.getString("type");
                        if (!type.equals(NO_DEPENDENCY))
                        {
                            final DependencyType dependencyType = DependencyType.valueOf(type);
                            final String dependency = jsonObject.getString("dependency");
                            add(className, dependency, dependencyType);
                            alreadyProcessed.add(dependency);
                        }
                        alreadyProcessed.add(className);
                    }
                } catch (IOException e) 
                {
                    throw new TapestryException("Exception trying to read " + storedDependencies.getAbsolutePath(), e);
                }
                
            }
            
        }
        else
        {
            storedDependencies = null;
        }
        
        storedDependencyInformationPresent = !map.isEmpty();
        
    }
    
    public void setupThreadCleanup(final PerthreadManager perthreadManager)
    {
        perthreadManager.addThreadCleanupCallback(() -> {
            INVALIDATIONS_DISABLED.set(0);
        });
    }

    @Override
    public void register(Class<?> component) 
    {
        register(component, component.getClassLoader());
    }
    
    @Override
    public void register(Class<?> component, ClassLoader classLoader) 
    {
        
        final String className = component.getName();
        final Set<Class<?>> furtherDependencies = new HashSet<>();
        Consumer<Class<?>> processClass = furtherDependencies::add;
        Consumer<String> processClassName = s -> {
            try {
                furtherDependencies.add(classLoader.loadClass(s));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        };
        
        // Components declared in the template
        registerTemplate(component, processClassName);
        
        // Dependencies from injecting or component-declaring annotations: 
        // @InjectPage, @InjectComponent
        for (Field field : component.getDeclaredFields())
        {
            
            // Component injection annotation
            if (field.isAnnotationPresent(InjectComponent.class))
            {
                final Class<?> dependency = field.getType();
                add(component, dependency, DependencyType.USAGE);
                processClass.accept(dependency);
            }
            
            // Page injection annotation
            if (field.isAnnotationPresent(InjectPage.class))
            {
                final Class<?> dependency = field.getType();
                add(component, dependency, DependencyType.INJECT_PAGE);
                processClass.accept(dependency);
            }
            
            // @Component
            registerComponentInstance(field, processClassName);
            
            // Mixins, class level: @Mixin
            registerMixin(field, processClassName);
            
            // Mixins applied to embedded component instances through @MixinClasses or @Mixins
            registerComponentInstanceMixins(field, processClass, processClassName);
        }

        // Superclass
        Class<?> superclass = component.getSuperclass();
        if (isTransformed(superclass))
        {
            processClass.accept(superclass);
            add(component, superclass, DependencyType.SUPERCLASS);
        }
        
        alreadyProcessed.add(className);
        
        for (Class<?> dependency : furtherDependencies) 
        {
            // Avoid infinite recursion
            final String dependencyClassName = dependency.getName();
            if (!alreadyProcessed.contains(dependencyClassName)
                    && plasticManager.shouldInterceptClassLoading(dependency.getName()))
            {
                register(dependency, classLoader);
            }
        }
        
    }

    /**
     * Notice only the main template (i.e. not the locale- or axis-specific ones)
     * are checked here. They hopefully will be covered when the ComponentModel-based
     * component dependency processing is done.
     * @param component
     * @param processClassName 
     */
    private void registerTemplate(Class<?> component, Consumer<String> processClassName) 
    {
        // TODO: implement caching of template dependency information, probably
        // by listening separately to ComponentTemplateSource to invalidate caches
        // just when template changes.
        
        final String className = component.getName();
        ComponentModel mock = new ComponentModelMock(component, isPage(className));
        final Resource templateResource = componentTemplateLocator.locateTemplate(mock, Locale.getDefault());
        String dependency;
        if (templateResource != null && templateResource.exists())
        {
            final ComponentTemplate template = templateParser.parseTemplate(templateResource);
            final List<TemplateToken> tokens = new LinkedList<>();

            tokens.addAll(template.getTokens());
            for (String id : template.getExtensionPointIds())
            {
                tokens.addAll(template.getExtensionPointTokens(id));
            }
            
            for (TemplateToken token : tokens)
            {
                if (token instanceof StartComponentToken) 
                {
                    StartComponentToken componentToken = (StartComponentToken) token;
                    String logicalName = componentToken.getComponentType();
                    if (logicalName != null)
                    {
                        try
                        {
                            dependency = resolver.resolveComponentTypeToClassName(logicalName);
                            add(className, dependency, DependencyType.USAGE);
                            processClassName.accept(dependency);
                        }
                        catch (UnknownValueException e)
                        {
                            // Logical name doesn't match an existing component. Ignore
                        }
                    }
                    for (String mixin : TapestryInternalUtils.splitAtCommas(componentToken.getMixins()))
                    {
                        try
                        {
                            if (mixin.contains("::"))
                            {
                                mixin = mixin.substring(0, mixin.indexOf("::"));
                            }
                            dependency = resolver.resolveMixinTypeToClassName(mixin);
                            add(className, dependency, DependencyType.USAGE);
                            processClassName.accept(dependency);
                        }
                        catch (UnknownValueException e)
                        {
                            // Mixin name doesn't match an existing mixin. Ignore
                        }

                    }
                }
            }
        }
    }
    
    private boolean isPage(final String className) 
    {
        Boolean result = isPageCache.get(className);
        if (result == null)
        {
            result = resolver.isPage(className);
            isPageCache.put(className, result);
        }
        return result;
    }

    private void registerComponentInstance(Field field, Consumer<String> processClassName)
    {
        if (field.isAnnotationPresent(org.apache.tapestry5.annotations.Component.class))
        {
            org.apache.tapestry5.annotations.Component component = 
                    field.getAnnotation(org.apache.tapestry5.annotations.Component.class);

            final String typeFromAnnotation = component.type().trim();
            String dependency;
            if (typeFromAnnotation.isEmpty())
            {
                dependency = field.getType().getName();
            }
            else
            {
                dependency = resolver.resolveComponentTypeToClassName(typeFromAnnotation);
            }
            add(field.getDeclaringClass().getName(), dependency, DependencyType.USAGE);
            processClassName.accept(dependency);
        }
    }

    private void registerMixin(Field field, Consumer<String> processClassName) {
        if (field.isAnnotationPresent(Mixin.class))
        {
            // Logic adapted from MixinWorker
            String mixinType = field.getAnnotation(Mixin.class).value();
            String mixinClassName = InternalUtils.isBlank(mixinType) ? 
                    getFieldTypeClassName(field) : 
                    resolver.resolveMixinTypeToClassName(mixinType);
            
            add(getDeclaringClassName(field), mixinClassName, DependencyType.USAGE);
            processClassName.accept(mixinClassName);
        }
    }

    private String getDeclaringClassName(Field field) {
        return field.getDeclaringClass().getName();
    }

    private String getFieldTypeClassName(Field field) {
        return field.getType().getName();
    }

    private void registerComponentInstanceMixins(Field field, Consumer<Class<?>> processClass, Consumer<String> processClassName) 
    {
        
        if (field.isAnnotationPresent(org.apache.tapestry5.annotations.Component.class))
        {
            
            MixinClasses mixinClasses = field.getAnnotation(MixinClasses.class);
            if (mixinClasses != null)
            {
                for (Class<?> dependency : mixinClasses.value()) 
                {
                    add(field.getDeclaringClass(), dependency, DependencyType.USAGE);
                    processClass.accept(dependency);
                }
            }
            
            Mixins mixins = field.getAnnotation(Mixins.class);
            if (mixins != null)
            {
                for (String mixin : mixins.value())
                {
                    // Logic adapted from MixinsWorker
                    Orderable<String> typeAndOrder = TapestryInternalUtils.mixinTypeAndOrder(mixin);
                    final String dependency = resolver.resolveMixinTypeToClassName(typeAndOrder.getTarget());
                    add(getDeclaringClassName(field), dependency, DependencyType.USAGE);
                    processClassName.accept(dependency);
                }
            }
            
        }
                
    }

    @Override
    public void register(ComponentPageElement componentPageElement) 
    {
        final String componentClassName = getClassName(componentPageElement);
        
        if (!alreadyProcessed.contains(componentClassName)) 
        {
            synchronized (map) 
            {
                
                // Components in the tree (i.e. declared in the template
                for (String id : componentPageElement.getEmbeddedElementIds()) 
                {
                    final ComponentPageElement child = componentPageElement.getEmbeddedElement(id);
                    add(componentPageElement, child, DependencyType.USAGE);
                    register(child);
                }
                
                // Mixins, class level
                final ComponentResources componentResources = componentPageElement.getComponentResources();
                final ComponentModel componentModel = componentResources.getComponentModel();
                for (String mixinClassName : componentModel.getMixinClassNames()) 
                {
                    add(componentClassName, mixinClassName, DependencyType.USAGE);
                }
                
                // Mixins applied to embedded component instances
                final List<String> embeddedComponentIds = componentModel.getEmbeddedComponentIds();
                for (String id : embeddedComponentIds)
                {
                    final EmbeddedComponentModel embeddedComponentModel = componentResources
                            .getComponentModel()
                            .getEmbeddedComponentModel(id);
                    final List<String> mixinClassNames = embeddedComponentModel
                            .getMixinClassNames();
                    for (String mixinClassName : mixinClassNames) {
                        add(componentClassName, mixinClassName, DependencyType.USAGE);
                    }
                }
                
                // Superclass
                final Component component = componentPageElement.getComponent();
                Class<?> parent = component.getClass().getSuperclass();
                if (parent != null && !Object.class.equals(parent))
                {
                    add(componentClassName, parent.getName(), DependencyType.SUPERCLASS);
                }
                
                // Dependencies from injecting annotations: 
                // @InjectPage, @InjectComponent, @InjectComponent
                final String metaDependencies = component.getComponentResources().getComponentModel().getMeta(META_ATTRIBUTE);
                if (metaDependencies != null)
                {
                    for (String dependency : metaDependencies.split(META_ATTRIBUTE_SEPARATOR)) 
                    {
                        add(componentClassName, dependency, 
                                isPage(dependency) ? DependencyType.INJECT_PAGE : DependencyType.USAGE);
                    }
                }
                
                alreadyProcessed.add(componentClassName);
                
            }            
            
        }
        
    }
    
    @Override
    public void register(PlasticField plasticField, MutableComponentModel componentModel) 
    {
        if (plasticField.hasAnnotation(InjectPage.class) || 
                plasticField.hasAnnotation(InjectComponent.class) || 
                plasticField.hasAnnotation(org.apache.tapestry5.annotations.Component.class))
        {
            String dependencies = componentModel.getMeta(META_ATTRIBUTE);
            final String dependency = plasticField.getTypeName();
            if (dependencies == null)
            {
                dependencies = dependency;
            }
            else
            {
                if (!dependencies.contains(dependency))
                {
                    dependencies = dependencies + META_ATTRIBUTE_SEPARATOR + dependency;
                }
            }
            componentModel.setMeta(META_ATTRIBUTE, dependencies);
        }
    }
    
    private String getClassName(ComponentPageElement component) 
    {
        return component.getComponentResources().getComponentModel().getComponentClassName();
    }

    @Override
    public void clear(String className) 
    {
        synchronized (map) 
        {
            alreadyProcessed.remove(className);
            map.remove(className);
            final Collection<Set<Dependency>> allDependentSets = map.values();
            for (Set<Dependency> dependents : allDependentSets) 
            {
                if (dependents != null) 
                {
                    final Iterator<Dependency> iterator = dependents.iterator();
                    while (iterator.hasNext())
                    {
                        if (className.equals(iterator.next().className))
                        {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void clear(ComponentPageElement component) 
    {
        clear(getClassName(component));
    }

    @Override
    public void clear() {
        map.clear();
        alreadyProcessed.clear();
    }

    @Override
    public Set<String> getDependents(String className) 
    {
        
        ensureClassIsAlreadyProcessed(className);
        
        final Set<Dependency> dependents = map.get(className);
        return dependents != null 
                ? dependents.stream().map(d -> d.className).collect(Collectors.toSet()) 
                : Collections.emptySet();
    }

    @Override
    public Set<String> getDependencies(String className, DependencyType type) 
    {
        
        ensureClassIsAlreadyProcessed(className);
        
        Set<String> dependencies = Collections.emptySet();
        if (alreadyProcessed.contains(className))
        {
            dependencies = map.entrySet().stream()
                .filter(e -> contains(e.getValue(), className, type))
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
        }
        
        return dependencies;
    }

    @Override
    public Set<String> getAllNonPageDependencies(String className) 
    {
        final Set<String> dependencies = new HashSet<>();
        getAllNonPageDependencies(className, dependencies);
        // Just in case, since it's possible to have circular dependencies.
        dependencies.remove(className);
        return Collections.unmodifiableSet(dependencies);
    }

    private void getAllNonPageDependencies(String className, Set<String> dependencies) 
    {
        Set<String> theseDependencies = new HashSet<>();
        theseDependencies.addAll(getDependencies(className, DependencyType.USAGE));
        theseDependencies.addAll(getDependencies(className, DependencyType.SUPERCLASS));
        theseDependencies.removeAll(dependencies);
        dependencies.addAll(theseDependencies);
        for (String dependency : theseDependencies) 
        {
            getAllNonPageDependencies(dependency, dependencies);
        }
    }

    
    private boolean contains(Set<Dependency> dependencies, String className, DependencyType type) 
    {
        boolean contains = false;
        for (Dependency dependency : dependencies) 
        {
            if (dependency.type.equals(type) && dependency.className.equals(className))
            {
                contains = true;
                break;
            }
        }
        return contains;
    }

    private void add(ComponentPageElement component, ComponentPageElement dependency, DependencyType type) 
    {
        add(getClassName(component), getClassName(dependency), type);
    }
    
    // Just for unit tests
    void add(String component, String dependency, DependencyType type, boolean markAsAlreadyProcessed)
    {
        if (markAsAlreadyProcessed)
        {
            alreadyProcessed.add(component);
        }
        if (dependency != null)
        {
            add(component, dependency, type);
        }
    }
    
    private void add(Class<?> component, Class<?> dependency, DependencyType type) 
    {
        if (plasticManager.shouldInterceptClassLoading(dependency.getName()))
        {
            add(component.getName(), dependency.getName(), type);
        }
    }
    
    private void add(String component, String dependency, DependencyType type) 
    {
        Objects.requireNonNull(component, "Parameter component cannot be null");
        Objects.requireNonNull(dependency, "Parameter dependency cannot be null");
        Objects.requireNonNull(dependency, "Parameter type cannot be null");
        synchronized (map) 
        {
            if (!component.equals(dependency))
            {
                Set<Dependency> dependents = map.get(dependency);
                if (dependents == null) 
                {
                    dependents = new HashSet<>();
                    map.put(dependency, dependents);
                }
                dependents.add(new Dependency(component, type));
            }
        }
    }
    
    @Override
    public void listen(InvalidationEventHub invalidationEventHub) 
    {
        invalidationEventHub.addInvalidationCallback(this::listen);
    }
    
    // Protected just for testing
    List<String> listen(List<String> resources)
    {
        List<String> furtherDependents = EMPTY_LIST;
        if (resources.isEmpty())
        {
            clear();
            furtherDependents = EMPTY_LIST;
        }
        else if (INVALIDATIONS_DISABLED.get() > 0)
        {
            furtherDependents = Collections.emptyList();
        }
        // Don't invalidate component dependency information when 
        // PageClassloaderContextManager is merging contexts
        // TODO: is this still needed since the inception of INVALIDATIONS_ENABLED? 
        else if (!pageClassLoaderContextManager.isMerging())
        {
            furtherDependents = new ArrayList<>();
            for (String resource : resources) 
            {
                
                final Set<String> dependents = getDependents(resource);
                for (String furtherDependent : dependents) 
                {
                    if (!resources.contains(furtherDependent) && !furtherDependents.contains(furtherDependent))
                    {
                        furtherDependents.add(furtherDependent);
                    }
                }
                
                clear(resource);
                
            }
        }
        return furtherDependents;
    }

    @Override
    public void writeFile() 
    {
        synchronized (this) 
        {
            try (FileWriter fileWriter = new FileWriter(storedDependencies);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter))
            {
                Set<String> classNames = new HashSet<>(alreadyProcessed.size());
                classNames.addAll(map.keySet());
                classNames.addAll(alreadyProcessed);
                JSONArray jsonArray = new JSONArray();
                for (String className : classNames)
                {
                    boolean hasDependencies = false;
                    for (DependencyType dependencyType : DependencyType.values())
                    {
                        final Set<String> dependencies = getDependencies(className, dependencyType);
                        for (String dependency : dependencies)
                        {
                            JSONObject object = new JSONObject();
                            object.put("class", className);
                            object.put("type", dependencyType.name());
                            object.put("dependency", dependency);
                            jsonArray.add(object);
                            hasDependencies = true;
                        }
                    }
                    // Add a fake dependency so classes without dependencies
                    // nor classes depending on it are properly stored and 
                    // retrieved, thus avoiding these classes getting into the 
                    // unknown page classloader context.
                    if (!hasDependencies)
                    {
                        if (getDependents(className).isEmpty()) {
                            JSONObject object = new JSONObject();
                            object.put("class", className);
                            object.put("type", NO_DEPENDENCY);
                            jsonArray.add(object);
                        }
                    }
                }
                bufferedWriter.write(jsonArray.toString());
            }
            catch (IOException e) 
            {
                throw new TapestryException("Exception trying to write " + storedDependencies.getAbsolutePath(), e);
            }
            
            Logger logger = LoggerFactory.getLogger(ComponentDependencyRegistry.class);
            
            logger.info("Component dependencies written to {}", 
                    storedDependencies.getAbsolutePath());
        } 
    }

    @Override
    public boolean contains(String className) 
    {
        return alreadyProcessed.contains(className);
    }

    @Override
    public Set<String> getClassNames() 
    {
        return Collections.unmodifiableSet(new HashSet<>(alreadyProcessed));
    }

    @Override
    public Set<String> getRootClasses() {
        return alreadyProcessed.stream()
                .filter(c -> getDependencies(c, DependencyType.USAGE).isEmpty() &&
                        getDependencies(c, DependencyType.INJECT_PAGE).isEmpty() &&
                        getDependencies(c, DependencyType.SUPERCLASS).isEmpty())
                .collect(Collectors.toSet());
    }
    
    private boolean isTransformed(Class<?> clasz)
    {
        return plasticManager.shouldInterceptClassLoading(clasz.getName());
    }

    @Override
    public boolean isStoredDependencyInformationPresent() 
    {
        return storedDependencyInformationPresent;
    }

    @Override
    public void disableInvalidations() 
    {
        INVALIDATIONS_DISABLED.set(INVALIDATIONS_DISABLED.get() + 1);
    }

    @Override
    public void enableInvalidations() 
    {
        INVALIDATIONS_DISABLED.set(INVALIDATIONS_DISABLED.get() - 1);
        if (INVALIDATIONS_DISABLED.get() < 0)
        {
            INVALIDATIONS_DISABLED.set(0);
        }
    }
    
    // Only for unit tests
    void setEnableEnsureClassIsAlreadyProcessed(boolean enableEnsureClassIsAlreadyProcessed) {
        this.enableEnsureClassIsAlreadyProcessed = enableEnsureClassIsAlreadyProcessed;
    }

    private void ensureClassIsAlreadyProcessed(String className) {
        if (enableEnsureClassIsAlreadyProcessed && !contains(className))
        {
            ThrowawayClassLoader classLoader = new ThrowawayClassLoader(getClass().getClassLoader());
            try 
            {
                register(classLoader.loadClass(className));
            } catch (ClassNotFoundException e) 
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Only really implemented method is {@link ComponentModel#getBaseResource()}
     */
    private class ComponentModelMock implements ComponentModel 
    {
        
        final private Resource baseResource;
        final private boolean isPage;
        final private String componentClassName;
        
        public ComponentModelMock(Class<?> component, boolean isPage)
        {
            componentClassName = component.getName();
            String templateLocation = componentClassName.replace('.', '/');
            baseResource = new ClasspathResource(templateLocation);
            
            this.isPage = isPage;
        }

        @Override
        public Resource getBaseResource() 
        {
            return baseResource;
        }

        @Override
        public String getLibraryName() 
        {
            return null;
        }

        @Override
        public boolean isPage() 
        {
            return isPage;
        }

        @Override
        public String getComponentClassName() 
        {
            return componentClassName;
        }

        @Override
        public List<String> getEmbeddedComponentIds() 
        {
            return null;
        }

        @Override
        public EmbeddedComponentModel getEmbeddedComponentModel(String componentId) 
        {
            return null;
        }

        @Override
        public String getFieldPersistenceStrategy(String fieldName) 
        {
            return null;
        }

        @Override
        public Logger getLogger() 
        {
            return null;
        }

        @Override
        public List<String> getMixinClassNames() 
        {
            return null;
        }

        @Override
        public ParameterModel getParameterModel(String parameterName) 
        {
            return null;
        }

        @Override
        public boolean isFormalParameter(String parameterName) 
        {
            return false;
        }

        @Override
        public List<String> getParameterNames() 
        {
            return null;
        }

        @Override
        public List<String> getDeclaredParameterNames() 
        {
            return null;
        }

        @Override
        public List<String> getPersistentFieldNames() 
        {
            return null;
        }

        @Override
        public boolean isRootClass() 
        {
            return false;
        }

        @Override
        public boolean getSupportsInformalParameters() 
        {
            return false;
        }

        @Override
        public ComponentModel getParentModel() 
        {
            return null;
        }

        @Override
        public boolean isMixinAfter() 
        {
            return false;
        }

        @Override
        public String getMeta(String key) 
        {
            return null;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Set<Class> getHandledRenderPhases() 
        {
            return null;
        }

        @Override
        public boolean handlesEvent(String eventType) 
        {
            return false;
        }

        @Override
        public String[] getOrderForMixin(String mixinClassName) 
        {
            return null;
        }

        @Override
        public boolean handleActivationEventContext() 
        {
            return false;
        }

    }
    
    private static final class Dependency
    {
        private final String className;
        private final DependencyType type;
        
        public Dependency(String className, DependencyType dependencyType) 
        {
            super();
            this.className = className;
            this.type = dependencyType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, type);
        }

        @Override
        public boolean equals(Object obj) 
        {
            if (this == obj) 
            {
                return true;
            }
            if (!(obj instanceof Dependency)) 
            {
                return false;
            }
            Dependency other = (Dependency) obj;
            return Objects.equals(className, other.className) && type == other.type;
        }

        @Override
        public String toString() 
        {
            return "Dependency [className=" + className + ", dependencyType=" + type + "]";
        }
        
    }
    
}
