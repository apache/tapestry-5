// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.internal;

import org.apache.tapestry5.ComponentAction;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ComponentSource;

import java.io.Serializable;

/**
 * A wrapper around a component id and a {@link org.apache.tapestry5.ComponentAction}.
 *
 * @see org.apache.tapestry5.corelib.components.FormFragment
 */
public class WrappedComponentAction implements Serializable
{
    private final String componentId;

    private final ComponentAction action;

    public WrappedComponentAction(Component component, ComponentAction action)
    {
        this(component.getComponentResources().getCompleteId(), action);
    }

    /**
     * @param componentId the component's complete id, suitable for use with {@link org.apache.tapestry5.services.ComponentSource#getComponent(String)}.
     * @param action      the action associated with the component
     */
    public WrappedComponentAction(String componentId, ComponentAction action)
    {
        this.componentId = componentId;
        this.action = action;
    }

    /**
     * Retrieves the component from the source and executes the action.
     *
     * @param source used to re-acquire the component
     */
    public void execute(ComponentSource source)
    {
        Component component = source.getComponent(componentId);

        action.execute(component);
    }
}
