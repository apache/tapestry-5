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
package org.apache.tapestry5.internal.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.internal.services.ComponentDependencyRegistry.DependencyType;
import org.apache.tapestry5.services.ComponentClassResolver;

public class ComponentDependencyGraphvizGeneratorImpl implements ComponentDependencyGraphvizGenerator {
    
    final private ComponentClassResolver componentClassResolver;
    
    final private ComponentDependencyRegistry componentDependencyRegistry;

    public ComponentDependencyGraphvizGeneratorImpl(ComponentDependencyRegistry componentDependencyRegistry, 
            ComponentClassResolver componentClassResolver) 
    {
        super();
        this.componentDependencyRegistry = componentDependencyRegistry;
        this.componentClassResolver = componentClassResolver;
    }

    @Override
    public String generate(String... classNames) 
    {
        
        final StringBuilder dotFile = new StringBuilder("digraph {\n\n");

        dotFile.append("\trankdir=LR;\n");
        dotFile.append("\tfontname=\"Helvetica,Arial,sans-serif\";\n");
        dotFile.append("\tsplines=ortho;\n\n");
        dotFile.append("\tnode [fontname=\"Helvetica,Arial,sans-serif\",fontsize=\"10pt\"];\n");
        dotFile.append("\tnode [shape=rect];\n\n");
        
        final Set<String> allClasses = new HashSet<>();
//        final Set<String> dependenciesAlreadyOutput = new HashSet<>();
        
        final Map<String, Node> nodeMap = new HashMap<>();
        for (String className : classNames) 
        {
            createNode(className, nodeMap);
            addDependencies(className, allClasses, nodeMap);
        }
        
        final List<Node> nodes = new ArrayList<>(nodeMap.values());
        Collections.sort(nodes, Comparator.comparing(n -> n.id));
        
        for (Node node : nodes)
        {
            dotFile.append(getNodeDefinition(node));
        }
        
        dotFile.append("\n");
        
        for (Node node : nodes)
        {
            for (Dependency dependency : node.dependencies)
            {
                dotFile.append(getNodeDependencyDefinition(node, dependency.className, dependency.type));
            }
        }
    

        dotFile.append("\n");
        dotFile.append("}");
        
        return dotFile.toString();
    }
    
    private String getNodeDefinition(Node node) 
    {
        return String.format("\t%s [label=\"%s\", tooltip=\"%s\"];\n", node.id, node.label, node.className);
    }
    
    private String getNodeDependencyDefinition(Node node, String dependency, DependencyType dependencyType) 
    {
        String extraDefinition;
        switch (dependencyType)
        {
            case INJECT_PAGE: extraDefinition = " [style=dashed]"; break;
            case SUPERCLASS: extraDefinition = " [arrowhead=empty]"; break;
            default: extraDefinition = "";
        }
        return String.format("\t%s -> %s%s\n", node.id, escapeNodeId(getNodeLabel(dependency)), extraDefinition);
    }

    private String getNodeLabel(String className) 
    {
        final String logicalName = componentClassResolver.getLogicalName(className);
        return getNodeLabel(className, logicalName, false);
    }

    private static String getNodeLabel(String className, final String logicalName, boolean beautify) {
        return logicalName != null ? beautifyLogicalName(logicalName) : (beautify ? beautifyClassName(className) : className);
    }
    
    private static String beautifyLogicalName(String logicalName) {
        return logicalName.startsWith("core/") ? logicalName.replace("core/", "") : logicalName;
    }

    private static String beautifyClassName(String className)
    {
        String name = className.substring(className.lastIndexOf('.') + 1);
        if (className.contains(".base."))
        {
            name += " (base class)";
        }
        else if (className.contains(".mixins."))
        {
            name += " (mixin)";
        }
        return name;
    }

    private static String escapeNodeId(String label) {
        return label.replace('.', '_').replace('/', '_');
    }

    private void addDependencies(String className, Set<String> allClasses, Map<String, Node> nodeMap) 
    {
        if (!allClasses.contains(className))
        {
            createNode(className, nodeMap);
            allClasses.add(className);
            for (DependencyType type : DependencyType.values()) 
            {
                for (String dependency : componentDependencyRegistry.getDependencies(className, type))
                {
                    addDependencies(dependency, allClasses, nodeMap);
                }
            }
        }
    }

    private void createNode(String className, Map<String, Node> nodeMap) 
    {
        if (!nodeMap.containsKey(className)) 
        {
            Collection<Dependency> deps = new HashSet<>();
            for (DependencyType type : DependencyType.values()) 
            {
                final Set<String> dependencies = componentDependencyRegistry.getDependencies(className, type);
                for (String dependency : dependencies) 
                {
                    deps.add(new Dependency(dependency, type));
                }
            }
            nodeMap.put(className, new Node(getNodeLabel(className), className, deps));
        }
    }
    
    private static final class Dependency
    {
        final private String className;
        final private DependencyType type;
        public Dependency(String className, DependencyType type) 
        {
            super();
            this.className = className;
            this.type = type;
        }
        @Override
        public String toString() 
        {
            return "Dependency [className=" + className + ", type=" + type + "]";
        }
    }

    private static final class Node 
    {

        final private String id;
        final private String className;
        final private String label;
        final private Set<Dependency> dependencies = new HashSet<>();
        
        public Node(String logicalName, String className, Collection<Dependency> dependencies) 
        {
            super();
            this.label = getNodeLabel(className, logicalName, true);
            this.id = escapeNodeId(getNodeLabel(className, logicalName, false));
            this.className = className;
            this.dependencies.addAll(dependencies);
        }

        @Override
        public String toString() 
        {
            return "Node [id=" + id + ", className=" + className + ", dependencies=" + dependencies + ", label=" + label + "]";
        }

    }
    
}
