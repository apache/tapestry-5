// Copyright 2022 The Apache Software Foundation
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Mixin;
import org.apache.tapestry5.annotations.MixinClasses;
import org.apache.tapestry5.annotations.Mixins;
import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.internal.parser.ComponentTemplate;
import org.apache.tapestry5.internal.parser.StartComponentToken;
import org.apache.tapestry5.internal.parser.TemplateToken;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.ioc.Orderable;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
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
import org.apache.tapestry5.services.pageload.PageClassloaderContextManager;
import org.apache.tapestry5.services.templates.ComponentTemplateLocator;
import org.slf4j.Logger;

public class ComponentDependencyRegistryImpl implements ComponentDependencyRegistry 
{
    
    final private PageClassloaderContextManager pageClassloaderContextManager;
    
    private static final String META_ATTRIBUTE = "injectedComponentDependencies";
    
    private static final String META_ATTRIBUTE_SEPARATOR = ",";
    
    // Key is a component, values are the components that depend on it.
    final private Map<String, Set<String>> map;
    
    // Cache to check which classes were already processed or not.
    final private Set<String> alreadyProcessed;
    
    final private File storedDependencies;
    
    final private static ThreadLocal<Boolean> INVALIDATIONS_ENABLED = ThreadLocal.withInitial(() -> Boolean.TRUE);
    
    final private PlasticManager plasticManager;
    
    final private ComponentClassResolver resolver;
    
    final private TemplateParser templateParser;
    
    @SuppressWarnings("deprecation")
    final private ComponentTemplateLocator componentTemplateLocator;
    
    final private boolean storedDependencyInformationPresent;
    
    public ComponentDependencyRegistryImpl(
            final PageClassloaderContextManager pageClassloaderContextManager,
            final PlasticManager plasticManager,
            final ComponentClassResolver componentClassResolver,
            final TemplateParser templateParser,
            final ComponentTemplateLocator componentTemplateLocator)
    {
        this.pageClassloaderContextManager = pageClassloaderContextManager;
        map = new HashMap<>();
        alreadyProcessed = new HashSet<>();
        this.plasticManager = plasticManager;
        this.resolver = componentClassResolver;
        this.templateParser = templateParser;
        this.componentTemplateLocator = componentTemplateLocator;
        
        storedDependencies = new File(FILENAME);
        if (storedDependencies.exists())
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
                JSONObject jsonObject = new JSONObject(builder.toString());
                for (String className : jsonObject.keySet())
                {
                    final Set<String> dependencies = jsonObject.getJSONArray(className)
                            .stream()
                            .map(o -> (String) o)
                            .collect(Collectors.toSet());
                    for (String dependency : dependencies) 
                    {
                        add(className, dependency);
                        alreadyProcessed.add(dependency);
                    }
                    alreadyProcessed.add(className);
                }
            } catch (IOException e) 
            {
                throw new TapestryException("Exception trying to read " + ComponentDependencyRegistry.FILENAME, e);
            }
        }
        
        storedDependencyInformationPresent = !map.isEmpty();
        
    }
    
    @Override
    public void register(Class<?> component) 
    {
        
        final Set<Class<?>> furtherDependencies = new HashSet<>();
        Consumer<Class<?>> processClass = furtherDependencies::add;
        Consumer<String> processClassName = s -> {
            try {
                furtherDependencies.add(component.getClassLoader().loadClass(s));
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
            
            // Component and page injection annotation
            if (field.isAnnotationPresent(InjectPage.class) || 
                    field.isAnnotationPresent(InjectComponent.class))
            {
                final Class<?> dependency = field.getType();
                add(component, dependency);
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
        Class superclass = component.getSuperclass();
        if (isTransformed(superclass))
        {
            processClass.accept(superclass);
            add(component, superclass);
        }
        
        alreadyProcessed.add(component.getName());
        
        for (Class<?> dependency : furtherDependencies) 
        {
            // Avoid infinite recursion
            final String dependencyClassName = dependency.getName();
            if (!alreadyProcessed.contains(dependencyClassName)
                    && !plasticManager.shouldInterceptClassLoading(dependency.getName()))
            {
                register(dependency);
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
    @SuppressWarnings("deprecation")
    private void registerTemplate(Class<?> component, Consumer<String> processClassName) 
    {
        // TODO: implement caching of template dependency information, probably
        // by listening separaterly to ComponentTemplateSource to invalidate caches
        // just when template changes.
        
        ComponentModel mock = new ComponentModelMock(component, resolver.isPage(component.getName()));
        final Resource templateResource = componentTemplateLocator.locateTemplate(mock, Locale.getDefault());
        String dependency;
        if (templateResource != null)
        {
            final String className = component.getName();
            final ComponentTemplate template = templateParser.parseTemplate(templateResource);
            for (TemplateToken token:  template.getTokens())
            {
                if (token instanceof StartComponentToken) 
                {
                    StartComponentToken componentToken = (StartComponentToken) token;
                    String logicalName = componentToken.getComponentType();
                    if (logicalName != null)
                    {
                        dependency = resolver.resolveComponentTypeToClassName(logicalName);
                        add(className, dependency);
                        processClassName.accept(dependency);
                    }
                    for (String mixin : TapestryInternalUtils.splitAtCommas(componentToken.getMixins()))
                    {
                        dependency = resolver.resolveMixinTypeToClassName(mixin);
                        add(className, dependency);
                        processClassName.accept(dependency);
                    }
                }
            }
        }
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
            add(field.getDeclaringClass().getName(), dependency);
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
            
            add(getDeclaringClassName(field), mixinClassName);
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
                for (Class dependency : mixinClasses.value()) 
                {
                    add(field.getDeclaringClass(), dependency);
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
                    add(getDeclaringClassName(field), dependency);
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
                    add(componentPageElement, child);
                    register(child);
                }
                
                // Mixins, class level
                final ComponentResources componentResources = componentPageElement.getComponentResources();
                final ComponentModel componentModel = componentResources.getComponentModel();
                for (String mixinClassName : componentModel.getMixinClassNames()) 
                {
                    add(componentClassName, mixinClassName);
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
                        add(componentClassName, mixinClassName);
                    }
                }
                
                // Superclass
                final Component component = componentPageElement.getComponent();
                Class<?> parent = component.getClass().getSuperclass();
                if (parent != null && !Object.class.equals(parent))
                {
                    add(componentClassName, parent.getName());
                }
                
                // Dependencies from injecting annotations: 
                // @InjectPage, @InjectComponent, @InjectComponent
                final String metaDependencies = component.getComponentResources().getComponentModel().getMeta(META_ATTRIBUTE);
                if (metaDependencies != null)
                {
                    for (String dependency : metaDependencies.split(META_ATTRIBUTE_SEPARATOR)) 
                    {
                        add(componentClassName, dependency);
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
            final Collection<Set<String>> allDependentSets = map.values();
            for (Set<String> dependents : allDependentSets) 
            {
                if (dependents != null) 
                {
                    dependents.remove(className);
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
        final Set<String> dependents = map.get(className);
        return dependents != null ? dependents : Collections.emptySet();
    }

    @Override
    public Set<String> getDependencies(String className) 
    {
        return map.entrySet().stream()
                .filter(e -> e.getValue().contains(className))
                .map(e -> e.getKey())
                .collect(Collectors.toSet());
    }

    private void add(ComponentPageElement component, ComponentPageElement dependency) 
    {
        add(getClassName(component), getClassName(dependency));
    }
    
    // Just for unit tests
    void add(String component, String dependency, boolean markAsAlreadyProcessed)
    {
        if (markAsAlreadyProcessed)
        {
            alreadyProcessed.add(component);
        }
        if (dependency != null)
        {
            add(component, dependency);
        }
    }
    
    private void add(Class component, Class dependency) 
    {
        if (plasticManager.shouldInterceptClassLoading(dependency.getName()))
        {
            add(component.getName(), dependency.getName());
        }
    }
    
    private void add(String component, String dependency) 
    {
        Objects.requireNonNull(component, "Parameter component cannot be null");
        Objects.requireNonNull(dependency, "Parameter dependency cannot be null");
        synchronized (map) 
        {
            Set<String> dependents = map.get(dependency);
            if (dependents == null) 
            {
                dependents = new HashSet<>();
                map.put(dependency, dependents);
            }
            dependents.add(component);
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
        List<String> furtherDependents;
        if (!INVALIDATIONS_ENABLED.get())
        {
            furtherDependents = Collections.emptyList();
        }
        else if (resources.isEmpty())
        {
            clear();
            furtherDependents = Collections.emptyList();
        }
        // Don't invalidate component dependency information when 
        // PageClassloaderContextManager is merging contexts
        else if (!pageClassloaderContextManager.isMerging())
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
        else
        {
            furtherDependents = Collections.emptyList();
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
                JSONObject jsonObject = new JSONObject();
                for (String className : map.keySet())
                {
                    final Set<String> dependencies = getDependencies(className);
                    jsonObject.put(className, JSONArray.from(dependencies));
                }
                bufferedWriter.write(jsonObject.toString());
            }
            catch (IOException e) 
            {
                throw new TapestryException("Exception trying to read " + ComponentDependencyRegistry.FILENAME, e);
            }
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
                .filter(c -> getDependencies(c).isEmpty())
                .collect(Collectors.toSet());
    }
    
    private boolean isTransformed(Class clasz)
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
        INVALIDATIONS_ENABLED.set(false);
    }

    @Override
    public void enableInvalidations() 
    {
        INVALIDATIONS_ENABLED.set(true);
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

    
}
