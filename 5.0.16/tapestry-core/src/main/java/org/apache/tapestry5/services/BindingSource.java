// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Used to acquire bindings for component parameters. The BindingSource service strips off the binding prefix to locate
 * a {@link org.apache.tapestry5.services.BindingFactory}.
 */
@UsesMappedConfiguration(BindingFactory.class)
public interface BindingSource
{
    /**
     * Examines the expression and strips off the leading prefix. The prefix is used to choose the appropriate {@link
     * BindingFactory}, which receives the description, the expression (after the prefix), and the location. If the
     * prefix doesn't exist, or if there's no prefix, then the factory for the default prefix (often "literal") is used
     * (and passed the full prefix).
     * <p/>
     * The binding represents a connection between the container and the component (the component is usually the child
     * of the container, though in a few cases, it is the component itself). In most cases, the expression is evaluated
     * in terms of the resources of the <em>container</em> and the component is ignored.
     *
     * @param description   description of the binding, such as "parameter foo"
     * @param container     typically, the parent of the component
     * @param component     the component whose parameter is to be bound
     * @param defaultPrefix the default prefix used when the expression itself does not have a prefix
     * @param expression    the binding
     * @param location      location assigned to the binding (or null if not known)
     * @return a binding
     */
    Binding newBinding(String description, ComponentResources container, ComponentResources component,
                       String defaultPrefix, String expression, Location location);

    /**
     * A simpler version of {@link #newBinding(String, ComponentResources, ComponentResources, String, String,
     * Location)} that defaults the values for several parameters. This is used in most cases. The default binding
     * prefix will be "prop". Most often, this is used to create a new default binding.
     *
     * @param description   description of the binding, such as "parameter foo"
     * @param container     typically, the parent of the component. This value will be used as the container
     *                      <em>and</em> the component, so whatever type of expression is evaluated, will be evaulated
     *                      in terms of this component
     * @param defaultPrefix the default prefix used when the expression itself does not have a prefix
     * @param expression    the binding
     * @return a binding
     */
    Binding newBinding(String description, ComponentResources container, String defaultPrefix, String expression);
}
