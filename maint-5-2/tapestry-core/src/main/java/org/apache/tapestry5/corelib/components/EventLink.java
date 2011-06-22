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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.corelib.base.AbstractComponentEventLink;
import org.apache.tapestry5.ioc.annotations.Inject;

/**
 * A close relative of {@link org.apache.tapestry5.corelib.components.ActionLink} except in two ways.
 * <p/>
 * First, the event that it triggers is explicitly controlled, rather than always "action".
 * <p/>
 * Second, the event is triggered in its container.
 * <p/>
 * This allows slightly shorter URLs but also allows multiple components within the same container to generate identical
 * URLs for common actions.
 */
public class EventLink extends AbstractComponentEventLink
{
    /**
     * The name of the event to be triggered in the parent component. Defaults to the id of the component. An {@link
     * org.apache.tapestry5.corelib.components.ActionLink} triggers an "action" event on itself, and EventLink component
     * triggers any arbitrary event on <em>its container</em>.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String event;

    @Inject
    private ComponentResources resources;

    String defaultEvent()
    {
        return resources.getId();
    }

    @Override
    protected Link createLink(Object[] eventContext)
    {
        ComponentResources containerResources = resources.getContainerResources();

        return containerResources.createEventLink(event, eventContext);
    }
}
