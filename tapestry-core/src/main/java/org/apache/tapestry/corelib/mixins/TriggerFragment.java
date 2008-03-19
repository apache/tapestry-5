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

package org.apache.tapestry.corelib.mixins;

import org.apache.tapestry.ClientElement;
import org.apache.tapestry.Field;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.InjectContainer;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.services.Heartbeat;

/**
 * A mixin that can be applied to a {@link org.apache.tapestry.corelib.components.Checkbox} or {@link
 * org.apache.tapestry.corelib.components.Radio} component that will link the input field and a {@link
 * org.apache.tapestry.corelib.components.FormFragment}, making the field control the client-side visibility of the
 * FormFragment.
 */
public class TriggerFragment
{
    @InjectContainer
    private Field _container;

    /**
     * The {@link org.apache.tapestry.corelib.components.FormFragment} instance to make dynamically visible or hidden.
     */
    @Parameter(required = true, defaultPrefix = "component")
    private ClientElement _fragment;

    @Environmental
    private PageRenderSupport _pageRenderSupport;

    @Environmental
    private Heartbeat _heartbeat;

    void beginRender()
    {
        Runnable r = new Runnable()
        {
            public void run()
            {
                _pageRenderSupport.addScript("Tapestry.linkTriggerToFormFragment('%s', '%s');",
                                             _container.getClientId(),
                                             _fragment.getClientId());
            }
        };

        // Defer generating the script to ensure that the FormFragment has rendered
        // and generated its client id.

        _heartbeat.defer(r);
    }
}
