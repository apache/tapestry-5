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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Link;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.corelib.base.AbstractComponentActionLink;
import org.apache.tapestry.ioc.annotations.Inject;

/**
 * A close relative of {@link org.apache.tapestry.corelib.components.ActionLink} except in two ways.
 * <p/>
 * First, the event that it triggers is explicitly controlled, rather than always "action".
 * <p/>
 * Second, the event is triggered in its container.
 * <p/>
 * This allows slightly shorter URLs but also allows multiple components within the same container to generate identical
 * URLs for common actions.
 */
public class EventLink extends AbstractComponentActionLink
{
    /**
     * The name of the event to be triggered in the parent component.  An {@link org.apache.tapestry.corelib.components.ActionLink}
     * triggers an "action" event on itself, and EventLink component triggers any arbitrary event on <em>its
     * container</em>.
     */
    @Parameter(required = true, defaultPrefix = TapestryConstants.LITERAL_BINDING_PREFIX)
    private String _event;

    @Inject
    private ComponentResources _resources;

    protected Link createLink(Object[] eventContext)
    {
        ComponentResources containerResources = _resources.getContainerResources();

        return containerResources.createActionLink(_event, false, eventContext);
    }
}
