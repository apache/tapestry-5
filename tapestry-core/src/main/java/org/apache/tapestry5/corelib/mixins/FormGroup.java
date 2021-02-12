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

import org.apache.tapestry5.Field;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ValidationDecorator;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.HeartbeatDeferred;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Applied to a {@link org.apache.tapestry5.Field}, this provides the outer layers of markup to correctly
 * render text fields, selects, and textareas using Bootstrap:
 * an outer {@code <div class="field-group">} containing a {@code <label class="control-label">} and the field itself.
 * Actually, the class attribute of the div is defined by the  
 * {@link SymbolConstants#FORM_GROUP_WRAPPER_CSS_CLASS} and
 * the class attribute of label is defined by the {@link SymbolConstants#FORM_GROUP_LABEL_CSS_CLASS}.
 * <code>field-group</code> and <code>control-label</code> are the default values. 
 * As with the {@link org.apache.tapestry5.corelib.components.Label} component, the {@code for} attribute is set (after the field itself
 * renders).
 *
 *
 * You can also use the {@link SymbolConstants#FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_NAME} symbol
 * to optionally wrap the input field in an element and {@link SymbolConstants#FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_CSS_CLASS}
 * to give it a CSS class. This is useful for Bootstrap form-horizontal forms.
 * Setting {@link SymbolConstants#FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_NAME} to <code>div</code>,
 * {@link SymbolConstants#FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_CSS_CLASS} to <code>col-sm-10</code>
 * and {@link SymbolConstants#FORM_GROUP_LABEL_CSS_CLASS} to <code>col-sm-2</code>
 * will generate labels 2 columns wide and form fields 10 columns wide.
 *
 *
 * This component is not appropriate for radio buttons or checkboxes as they use a different class on the outermost element
 * ("radio" or "checkbox") and next the element inside the {@code <label>}.
 *
 *
 * @tapestrydoc
 * @since 5.4
 * @see SymbolConstants#FORM_GROUP_WRAPPER_CSS_CLASS
 * @see SymbolConstants#FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_NAME
 * @see SymbolConstants#FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_CSS_CLASS
 * @see SymbolConstants#FORM_GROUP_LABEL_CSS_CLASS
 * @see SymbolConstants#FORM_FIELD_CSS_CLASS
 */
public class FormGroup
{
    @InjectContainer
    private Field field;
    
    @Inject
    @Symbol(SymbolConstants.FORM_GROUP_LABEL_CSS_CLASS)
    private String labelCssClass;
    
    @Inject
    @Symbol(SymbolConstants.FORM_GROUP_WRAPPER_CSS_CLASS)
    private String divCssClass;
    
    @Inject
    @Symbol(SymbolConstants.FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_NAME)
    private String fieldWrapperElementName;

    @Inject
    @Symbol(SymbolConstants.FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_CSS_CLASS)
    private String fieldWrapperElementCssClass;

    private Element label;
    
    private Element fieldWrapper;

    @Environmental
    private ValidationDecorator decorator;
    
    @Inject
    private JavaScriptSupport javaScriptSupport;

    void beginRender(MarkupWriter writer)
    {
        writer.element("div", "class", 
                !("form-group".equals(divCssClass)) ? ("form-group" + " " + divCssClass) : divCssClass);

        decorator.beforeLabel(field);

        label = writer.element("label", "class", labelCssClass);
        writer.end();

        fillInLabelAttributes();

        decorator.afterLabel(field);
        
        if (fieldWrapperElementName.length() > 0) {
            fieldWrapper = writer.element(fieldWrapperElementName);
            if (fieldWrapperElementCssClass.length() > 0) {
                fieldWrapper.attribute("class", fieldWrapperElementCssClass);
            }
        }
        
    }

    @HeartbeatDeferred
    void fillInLabelAttributes()
    {
        label.attribute("for", field.getClientId());
        label.text(field.getLabel());
    }

    void afterRender(MarkupWriter writer)
    {
        if (fieldWrapper != null) 
        {
            writer.end(); // field wrapper
        }
        
        // TAP5-2662
        final Element inputElement = writer.getDocument().getElementById(field.getClientId());
        if (inputElement != null) 
        {
            final String clientId = field.getClientId();
            final String labelId = javaScriptSupport.allocateClientId(clientId + "-label");
            label.attribute("id", labelId);
            inputElement.attribute("aria-labelledby", labelId);
        }
        
        writer.end(); // div.form-group
    }
}
