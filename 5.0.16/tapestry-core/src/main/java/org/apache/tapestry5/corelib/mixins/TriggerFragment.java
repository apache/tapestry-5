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

package org.apache.tapestry5.corelib.mixins;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.Heartbeat;

/**
 * A mixin that can be applied to a {@link org.apache.tapestry5.corelib.components.Checkbox} or {@link
 * org.apache.tapestry5.corelib.components.Radio} component that will link the input field and a {@link
 * org.apache.tapestry5.corelib.components.FormFragment}, making the field control the client-side visibility of the
 * FormFragment.
 */
public class TriggerFragment
{
    @InjectContainer
    private Field container;

    /**
     * The {@link org.apache.tapestry5.corelib.components.FormFragment} instance to make dynamically visible or hidden.
     */
    @Parameter(required = true, defaultPrefix = BindingConstants.COMPONENT, allowNull = false)
    private ClientElement fragment;

    @Environmental
    private RenderSupport renderSupport;

    @Environmental
    private Heartbeat heartbeat;

    void beginRender()
    {
        Runnable r = new Runnable()
        {
            public void run()
            {
                JSONArray spec = new JSONArray();
                spec.put(container.getClientId());
                spec.put(fragment.getClientId());

                renderSupport.addInit("linkTriggerToFormFragment", spec);
            }
        };

        // Defer generating the script to ensure that the FormFragment has rendered
        // and generated its client id.

        heartbeat.defer(r);
    }
}
