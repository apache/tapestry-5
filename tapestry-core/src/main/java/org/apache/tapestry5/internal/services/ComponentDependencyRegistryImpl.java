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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.commons.services.InvalidationEventHub;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.model.ComponentModel;
import org.apache.tapestry5.model.EmbeddedComponentModel;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.runtime.Component;

public class ComponentDependencyRegistryImpl implements ComponentDependencyRegistry 
{
    
    private static final String META_ATTRIBUTE = "injectedComponentDependencies";
    
    private static final String META_ATTRIBUTE_SEPARATOR = ",";
    
    // Key is a component, values are the components that depend on it.
    final private Map<String, Set<String>> map;
    
    // Cache to check which classes were already processed or not.
    final private Set<String> alreadyProcessed;
    
    final private File storedDependencies;

    public ComponentDependencyRegistryImpl()
    {
        map = new HashMap<>();
        alreadyProcessed = new HashSet<>();
        
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
                    map.put(className, new HashSet<>(dependencies));
                    alreadyProcessed.add(className);
                }
            } catch (IOException e) 
            {
                throw new TapestryException("Exception trying to read " + ComponentDependencyRegistry.FILENAME, e);
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
    
    // Protected just for testing
    void add(String component, String dependency) 
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
        if (resources.isEmpty())
        {
            clear();
            furtherDependents = Collections.emptyList();
        }
        else
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
    
}
