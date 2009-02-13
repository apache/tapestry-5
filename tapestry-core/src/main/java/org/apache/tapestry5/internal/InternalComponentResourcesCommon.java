// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.internal.structure.ComponentPageElement;
import org.apache.tapestry5.runtime.Component;

import java.util.Map;

/**
 * Operations shared by {@link InternalComponentResources} and {@link ComponentPageElement}. Typically, these means
 * methods of InternalComponentResources that are delegated to the component page element.
 */
public interface InternalComponentResourcesCommon
{
    /**
     * Returns true if the component has finished loading. Initially, this value will be false.
     *
     * @see org.apache.tapestry5.runtime.PageLifecycleListener#containingPageDidLoad()
     */
    boolean isLoaded();

    /**
     * Used during construction of the page to identify the binding for a particular parameter.
     * <p/>
     */
    void bindParameter(String parameterName, Binding binding);

    /**
     * Returns the binding for the given parameter name, or null.
     *
     * @param parameterName name of component parameter
     * @return binding if bound, or null
     * @since 5.1.0.0
     */
    Binding getBinding(String parameterName);

    /**
     * Returns the mixin instance for the fully qualfied mixin class name.
     *
     * @param mixinClassName fully qualified class name
     * @return IllegalArgumentException if no such mixin is associated with the core component
     */
    Component getMixinByClassName(String mixinClassName);

    /**
     * Constructs a map linking informal parameters to the corresponding bindings.
     *
     * @return map, possible empty
     */
    Map<String, Binding> getInformalParameterBindings();


}
