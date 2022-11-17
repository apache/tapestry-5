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

/**
 * Service that generates a <a href="https://graphviz.org/doc/info/lang.html">Graphviz DOT description file</a>
 * for a given component's dependency graph or for the whole set of dependencies of all components.
 * @since 5.8.3
 */
public interface ComponentDependencyGraphvizGenerator {

    /**
     * Generates the Graphviz DOT file and returns it as a Strting.
     * @param classNames the component (including page) class names to generate the dependency graph.
     * @return
     */
    String generate(String ... classNames);
    
}
