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

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.HeartbeatDeferred;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Generates a &lt;label&gt; element for a particular field. It writes the CSS class "control-label".
 *
 * A Label will render its body, if it has one. However, in most cases it will not have a body, and will render its
 * {@linkplain org.apache.tapestry5.Field#getLabel() field's label} as its body. Remember, however, that it is the
 * field label that will be used in any error messages. The Label component allows for client- and server-side
 * validation error decorations.
 *
 * @tapestrydoc
 */
@SupportsInformalParameters
public class Label
{
    /**
     * The for parameter is used to identify the {@link Field} linked to this label (it is named this way because it
     * results in the for attribute of the label element).
     */
    @Parameter(name = "for", required = true, allowNull = false, defaultPrefix = BindingConstants.COMPONENT)
    private Field field;

    /**
     * Used to explicitly set the client-side id of the element for this component. Normally this is not
     * bound (or null) and {@link org.apache.tapestry5.services.javascript.JavaScriptSupport#allocateClientId(org.apache.tapestry5.ComponentResources)}
     * is used to generate a unique client-id based on the component's id. In some cases, when creating client-side
     * behaviors, it is useful to explicitly set a unique id for an element using this parameter.
     * 
     * Certain values, such as "submit", "method", "reset", etc., will cause client-side conflicts and are not allowed; using such will
     * cause a runtime exception.
     * @since 5.6.0
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String clientId;

    @Environmental
    private ValidationDecorator decorator;

    @Inject
    private ComponentResources resources;

    @Inject
    private JavaScriptSupport javaScriptSupport;

    @Inject
    @Symbol(TapestryHttpSymbolConstants.PRODUCTION_MODE)
    private boolean productionMode;

    /**
     * If true, then the body of the label element (in the template) is ignored. This is used when a designer places a
     * value inside the &lt;label&gt; element for WYSIWYG purposes, but it should be replaced with a different
     * (probably, localized) value at runtime. The default is false, so a body will be used if present and the field's
     * label will only be used if the body is empty or blank.
     */
    @Parameter
    private boolean ignoreBody;

    private Element labelElement;

    private String string;

    private String string2;

    boolean beginRender(MarkupWriter writer)
    {
        decorator.beforeLabel(field);

        labelElement = writer.element("label", "class", "control-label");

        resources.renderInformalParameters(writer);

        // Since we don't know if the field has rendered yet, we need to defer writing the for and id
        // attributes until we know the field has rendered (and set its clientId property). That's
        // exactly what Heartbeat is for.

        updateAttributes();

        return !ignoreBody;
    }

    @HeartbeatDeferred
    private void updateAttributes()
    {
        String fieldId = field.getClientId();

        if (!productionMode && fieldId == null)
        {
            // TAP5-2500
            String warningText = "The Label component " + resources.getCompleteId()
              + " is linked to a Field that failed to return a clientId. The 'for' attibute will not be rendered.";
            javaScriptSupport.require("t5/core/console").invoke("warn").with(warningText);
        }
        
        String id = clientId != null ? clientId : javaScriptSupport.allocateClientId(fieldId + "-label");
        labelElement.attribute("id", id);
        labelElement.forceAttributes("for", fieldId);
        
        if (fieldId != null)
        {
            Element input = labelElement.getDocument().getElementById(field.getClientId());
            if (input != null) 
            {
                input.attribute("aria-labelledby", id);
            }
        }
        
        decorator.insideLabel(field, labelElement);
    }

    void afterRender(MarkupWriter writer)
    {
        // If the Label element has a body that renders some non-blank output, that takes precedence
        // over the label string provided by the field.

        boolean bodyIsBlank = InternalUtils.isBlank(labelElement.getChildMarkup());

        if (bodyIsBlank)
            writer.write(field.getLabel());

        writer.end(); // label

        decorator.afterLabel(field);
    }
}
