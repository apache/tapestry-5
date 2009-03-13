// Copyright 2006, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.model;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.ioc.Locatable;

import java.util.List;

/**
 * The model for a component embedded within another component, as defined by the {@link
 * org.apache.tapestry5.annotations.Component} annotation.
 */
public interface EmbeddedComponentModel extends Locatable
{
    /**
     * A unique id for the embedded component.
     */
    String getId();

    /**
     * The type of the component, which may be blank.
     */
    String getComponentType();

    /**
     * The class name of the component, as derived from the field to which the {@link Component} annotation is applied.
     * This value is only used when the componentType property is blank.
     */
    String getComponentClassName();

    /**
     * A sorted list of the names of all bound parameters.
     */
    List<String> getParameterNames();

    /**
     * The value for each parameter, which will be interpreted as a binding expression.
     */
    String getParameterValue(String parameterName);

    /**
     * Returns the fully qualified class names of all mixins added to this component, sorted alphabetically.
     */
    List<String> getMixinClassNames();

    /**
     * If true, then the component should inherit informal parameters from its container.
     */
    boolean getInheritInformalParameters();

    /**
     * Returns the list of published parameters of this component (usually an empty list).
     *
     * @return list of parameter names to publish
     * @see org.apache.tapestry5.annotations.Component#publishParameters()
     * @since 5.1.0.0
     */
    List<String> getPublishedParameters();
}
