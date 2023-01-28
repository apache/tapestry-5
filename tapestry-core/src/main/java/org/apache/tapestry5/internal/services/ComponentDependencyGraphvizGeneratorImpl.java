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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
        
        for (String className : classNames)
        {
            addDependencies(className, allClasses);
        }
        
        final StringBuilder dependencySection = new StringBuilder();
        
        for (String className : allClasses) 
        {
            final Node node = createNode(componentClassResolver.getLogicalName(className), className);
            dotFile.append(getNodeDefinition(node));
            for (String dependency : node.dependencies)
            {
                dependencySection.append(getNodeDependencyDefinition(node, dependency));
            }
        }
        
        dotFile.append("\n");
        dotFile.append(dependencySection);
        dotFile.append("\n");

        dotFile.append("}");
        
        return dotFile.toString();
    }
    
    private String getNodeDefinition(Node node) 
    {
        return String.format("\t%s [label=\"%s\", tooltip=\"%s\"];\n", node.id, node.label, node.className);
    }
    
    private String getNodeDependencyDefinition(Node node, String dependency) 
    {
        return String.format("\t%s -> %s\n", node.id, escapeNodeId(getNodeLabel(dependency)));
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

    private void addDependencies(String className, Set<String> allClasses) 
    {
        if (!allClasses.contains(className))
        {
            allClasses.add(className);
            for (String dependency : componentDependencyRegistry.getDependencies(className))
            {
                addDependencies(dependency, allClasses);
            }
        }
    }

    private Node createNode(String logicalName, String className) 
    {
        return new Node(logicalName, className, componentDependencyRegistry.getDependencies(className));
    }

    private static final class Node {

        final private String id;
        final private String className;
        final private String label;
        final private Set<String> dependencies = new HashSet<>();
        
        public Node(String logicalName, String className, Collection<String> dependencies) 
        {
            super();
            this.label = getNodeLabel(className, logicalName, true);
            this.id = escapeNodeId(getNodeLabel(className, logicalName, false));
            this.className = className;
            this.dependencies.addAll(dependencies);
        }

    }
}
