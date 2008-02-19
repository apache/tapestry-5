// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry.*;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Mixin;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.corelib.mixins.DiscardBody;
import org.apache.tapestry.corelib.mixins.RenderDisabled;
import org.apache.tapestry.corelib.mixins.RenderInformals;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.ComponentDefaultProvider;

/**
 * A radio button (i.e., &lt;input type="radio"&gt;). Radio buttons <strong>must</strong> operate within a {@link
 * RadioContainer} (normally, the {@link RadioGroup} component).
 * <p/>
 * If the value parameter is not bound, then the default value is a property of the container component whose name
 * matches the Radio component's id.
 */
public class Radio implements Field
{
    @Environmental
    private RadioContainer _container;

    /**
     * The user presentable label for the field. If not provided, a reasonable label is generated from the component's
     * id, first by looking for a message key named "id-label" (substituting the component's actual id), then by
     * converting the actual id to a presentable string (for example, "userId" to "User Id").
     */
    @Parameter(defaultPrefix = TapestryConstants.LITERAL_BINDING_PREFIX)
    private String _label;

    /**
     * The value associated with this radio button. This is used to determine which radio button will be selected when
     * the page is rendered, and also becomes the value assigned when the form is submitted.
     */
    @Parameter(required = true, principal = true)
    private Object _value;

    @Inject
    private ComponentDefaultProvider _defaultProvider;

    @Inject
    private ComponentResources _resources;

    @SuppressWarnings("unused")
    @Mixin
    private RenderInformals _renderInformals;

    @SuppressWarnings("unused")
    @Mixin
    private RenderDisabled _renderDisabled;

    @SuppressWarnings("unused")
    @Mixin
    private DiscardBody _discardBody;

    @Inject
    private PageRenderSupport _pageRenderSupport;

    private String _clientId;

    private String _controlName;

    /**
     * If true, then the field will render out with a disabled attribute (to turn off client-side behavior). Further, a
     * disabled field ignores any value in the request when the form is submitted.
     */
    @Parameter("false")
    private boolean _disabled;

    String defaultLabel()
    {
        return _defaultProvider.defaultLabel(_resources);
    }

    Binding defaultValue()
    {
        return _defaultProvider.defaultBinding("value", _resources);
    }

    /**
     * Returns the control name provided by the containing {@link org.apache.tapestry.RadioContainer}.
     */
    public String getControlName()
    {
        return _controlName;
    }

    public String getLabel()
    {
        return _label;
    }

    /**
     * Returns true if this component has been expressly disabled (via its disabled parameter), or if the {@link
     * RadioContainer container} has been disabled.
     */
    public boolean isDisabled()
    {
        return _disabled || _container.isDisabled();
    }

    public String getClientId()
    {
        return _clientId;
    }

    void beginRender(MarkupWriter writer)
    {
        String value = _container.toClient(_value);

        _clientId = _pageRenderSupport.allocateClientId(_resources.getId());
        _controlName = _container.getElementName();

        writer.element("input", "type", "radio", "id", _clientId, "name", _controlName, "value", value);

        if (_container.isSelected(_value)) writer.attributes("checked", "checked");
    }

    void afterRender(MarkupWriter writer)
    {
        writer.end();
    }

    /**
     * Returns false; the RadioComponent component does not support declarative field validation.
     */
    public boolean isRequired()
    {
        return false;
    }
}
