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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.HeartbeatDeferred;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;

/**
 * Provides a client-side element to contain validation errors; this renders as a {@code <p class="help-block">}.
 * Must be enclosed by a
 * {@link org.apache.tapestry5.corelib.components.Form} component and assumes the field and the Error component
 * are enclosed by a {@code <div class="[form group CSS class]">}, where {@code [form group CSS class]}
 * is defined by the value of the {@link SymbolConstants#FORM_GROUP_WRAPPER_CSS_CLASS}
 * configuration symbol ({@code tapestry.form-group-wrapper-css-class}).
 *
 * It is acceptable to include multiple Errors components for a single field; this is sometimes necessary
 * when creating a responsive layout - which should probably ensure that only one of the Errors is
 * visible at any time.
 *
 * Errors is optional, and Tapestry's client-side logic will do a reasonable job of placing a help block
 * dynamically when a validation error must be presented; this component is intended for use when the default logic
 * doesn't place the help block in the right spot.
 *
 * @tapestrydoc
 * @since 5.2.0
 */
@SupportsInformalParameters
public class Error
{
    /**
     * The for parameter is used to identify the {@link Field} to present errors of.
     */
    @Parameter(name = "for", required = true, allowNull = false, defaultPrefix = BindingConstants.COMPONENT)
    private Field field;

    @Inject
    private ComponentResources resources;
    
    @Inject
    @Symbol(SymbolConstants.ERROR_CSS_CLASS)
    private String cssClass;

    boolean beginRender(final MarkupWriter writer)
    {
        // Initially invisible; will be shown on client if an error exists.
        Element element = writer.element("p", "class", 
                !("help-block".equals(cssClass)) ? ("help-block " + cssClass) : cssClass + " invisible",
                        "role", "alert");
        
        resources.renderInformalParameters(writer);

        // Wait until the end of the heartbeat to ensure the Field has had a chance to render.
        updateElement(element);

        writer.end();

        return false;
    }

    @HeartbeatDeferred
    private void updateElement(final Element element)
    {
        // The field may add an id attribute because of this call.
        element.attribute("data-error-block-for", field.getClientId());
        String id = field.getClientId() + "-help-block";
        element.attribute("id", id);
        Element input = element.getDocument().getElementById(field.getClientId());
        if (input != null) {
            input.attribute("aria-describedby", id);
        }
    }
}
