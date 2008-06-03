// Copyright 2006 The Apache Software Foundation
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

/**
 * Creates a binding of a particular type.  This is usually invoked from the {@link
 * org.apache.tapestry5.services.BindingSource} service.
 */
public interface BindingFactory
{
    /**
     * Creates a new binding instance.
     * <p/>
     * The binding represents a connection between the container and the component (the component is usually the child
     * of the component, though in a few cases, it is the component itself). In most cases, the expression is evaluated
     * in terms of the resources of the <em>container</em> and the component is ignored.
     *
     * @param description of the binding, such as, "parameter foo"
     * @param container   the component, as represented by its resources, for which a binding is to be created.
     * @param component   the component whose parameter is to be bound by the resulting binding (rarely used)
     * @param expression
     * @param location    from which the binding was generate, or null if not known
     * @return the new binding instance
     */
    Binding newBinding(String description, ComponentResources container, ComponentResources component,
                       String expression, Location location);
}
