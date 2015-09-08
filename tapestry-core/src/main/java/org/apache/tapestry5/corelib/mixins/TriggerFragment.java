// Copyright 2008, 2010, 2011, 2012 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.HeartbeatDeferred;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.corelib.components.FormFragment;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * A mixin that can be applied to a {@link org.apache.tapestry5.corelib.components.Checkbox} or
 * {@link org.apache.tapestry5.corelib.components.Radio} component that will link the input field and a
 * {@link org.apache.tapestry5.corelib.components.FormFragment}, making the field control the client-side visibility of
 * the FormFragment. See a full example with {@link FormFragment}'s documentation.
 *
 * @tapestrydoc
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

    /**
     * If true then the client-side logic is inverted; the fragment is made visible when the checkbox is NOT checked.
     * The default is false (the fragment is visible when the checkbox IS checked).
     *
     * @since 5.2.0
     */
    @Parameter
    private boolean invert;

    @Environmental
    private JavaScriptSupport javascriptSupport;

    @HeartbeatDeferred
    void beginRender()
    {
        String fragmentId = fragment.getClientId();
        if (fragmentId == null)
        {
            throw new IllegalStateException("The fragment has returned a null client-side ID");
        }
        JSONObject spec = new JSONObject(
                "triggerId", container.getClientId(),
                "fragmentId", fragmentId);

        if (invert)
        {
            spec.put("invert", true);
        }

        javascriptSupport.require("t5/core/form-fragment").invoke("linkTrigger").with(spec);
    }
}
