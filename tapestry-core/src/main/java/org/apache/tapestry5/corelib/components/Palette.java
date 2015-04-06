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
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.internal.util.SelectModelRenderer;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.compatibility.DeprecationWarning;

import java.util.Collection;

/**
 * Multiple selection component. Generates a UI consisting of two &lt;select&gt; elements configured for multiple
 * selection; the one on the left is the list of "available" elements, the one on the right is "selected". Elements can
 * be moved between the lists by clicking a button, or double clicking an option (and eventually, via drag and drop).
 *
 * The items in the available list are kept ordered as per {@link SelectModel} order. When items are moved from the
 * selected list to the available list, they items are inserted back into their proper positions.
 *
 * The Palette may operate in normal or re-orderable mode, controlled by the reorder parameter.
 *
 * In normal mode, the items in the selected list are kept in the same "natural" order as the items in the available
 * list.
 *
 * In re-order mode, items moved to the selected list are simply added to the bottom of the list. In addition, two extra
 * buttons appear to move items up and down within the selected list.
 *
 * Much of the look and feel is driven by CSS, the default Tapestry CSS is used to set up the columns, etc. By default,
 * the &lt;select&gt; element's widths are 200px, and it is common to override this to a specific value:
 *
 * <pre>
 * &lt;style&gt;
 *   DIV.palette SELECT { width: 300px; }
 * &lt;/style&gt;
 * </pre>
 *
 * You'll want to ensure that both &lt;select&gt; in each column is the same width, otherwise the display will update
 * poorly as options are moved from one column to the other.
 *
 * Option groups within the {@link SelectModel} will be rendered, but are not supported by many browsers, and are not
 * fully handled on the client side.
 *
 * For an alternative component that can be used for similar purposes, see
 * {@link Checklist}.
 * Starting in 5.4, the selected parameter may be any kind of collection, but is typically a List if the Palette is configured for re-ordering,
 * and a Set if order does not matter (though it is common to use a List in the latter case as well). Also, starting in 5.4,
 * the Palette is compatible with the {@link org.apache.tapestry5.validator.Required} validator (on both client and server-side), and
 * triggers new events that allows the application to veto a proposed changed to the selection (see the {@code t5/core/events} module).
 *
 * @tapestrydoc
 * @see Form
 * @see Select
 */
@Import(stylesheet = "Palette.css")
public class Palette extends AbstractField
{
    /**
     * The image to use for the deselect button (the default is a left pointing arrow).
     */
    @Parameter
    private Asset deselect;

    /**
     * A ValueEncoder used to convert server-side objects (provided from the
     * "source" parameter) into unique client-side strings (typically IDs) and
     * back. Note: this component does NOT support ValueEncoders configured to
     * be provided automatically by Tapestry.
     */
    @Parameter(required = true, allowNull = false)
    private ValueEncoder<Object> encoder;

    /**
     * Model used to define the values and labels used when rendering.
     */
    @Parameter(required = true, allowNull = false)
    private SelectModel model;

    /**
     * Allows the title text for the available column (on the left) to be modified. As this is a Block, it can contain
     * conditionals and components. The default is the text "Available".
     */
    @Property(write = false)
    @Parameter(required = true, allowNull = false, value = "message:core-palette-available-label", defaultPrefix = BindingConstants.LITERAL)
    private Block availableLabel;

    /**
     * Allows the title text for the selected column (on the right) to be modified. As this is a Block, it can contain
     * conditionals and components. The default is the text "Available".
     */
    @Property(write = false)
    @Parameter(required = true, allowNull = false, value = "message:core-palette-selected-label", defaultPrefix = BindingConstants.LITERAL)
    private Block selectedLabel;

    /**
     * The image to use for the move down button (the default is a downward pointing arrow).
     */
    @Parameter
    private Asset moveDown;

    /**
     * The image to use for the move up button (the default is an upward pointing arrow).
     */
    @Parameter
    private Asset moveUp;

    /**
     * The image to use for the select button (the default is a right pointing arrow).
     */
    @Parameter
    private Asset select;

    /**
     * The list of selected values from the {@link org.apache.tapestry5.SelectModel}. This will be updated when the form
     * is submitted. If the value for the parameter is null, a new list will be created, otherwise the existing list
     * will be cleared. If unbound, defaults to a property of the container matching this component's id.
     *
     * Prior to Tapestry 5.4, this allowed null, and a list would be created when the form was submitted. Starting
     * with 5.4, the selected list may not be null, and it need not be a list (it may be, for example, a set).
     */
    @Parameter(required = true, autoconnect = true, allowNull = false)
    private Collection<Object> selected;

    /**
     * If true, then additional buttons are provided on the client-side to allow for re-ordering of the values.
     * This is only useful when the selected parameter is bound to a {@code List}, rather than a {@code Set} or other
     * unordered collection.
     */
    @Parameter("false")
    @Property(write = false)
    private boolean reorder;


    /**
     * Number of rows to display.
     */
    @Property(write = false)
    @Parameter(value = BindingConstants.SYMBOL + ":" + ComponentParameterConstants.PALETTE_ROWS_SIZE)
    private int size;

    /**
     * The object that will perform input validation. The validate binding prefix is generally used to provide
     * this object in a declarative fashion.
     *
     * @since 5.2.0
     */
    @Parameter(defaultPrefix = BindingConstants.VALIDATE)
    @SuppressWarnings("unchecked")
    private FieldValidator<Object> validate;

    @Inject
    @Symbol(SymbolConstants.COMPACT_JSON)
    private boolean compactJSON;

    @Inject
    private DeprecationWarning deprecationWarning;

    void pageLoaded() {
        deprecationWarning.ignoredComponentParameters(resources, "select", "moveUp", "moveDown", "deselect");
    }


    public final Renderable mainRenderer = new Renderable()
    {
        public void render(MarkupWriter writer)
        {
            SelectModelRenderer visitor = new SelectModelRenderer(writer, encoder, false);

            model.visit(visitor);
        }
    };

    public String getInitialJSON()
    {
        JSONArray array = new JSONArray();

        for (Object o : selected)
        {
            String value = encoder.toClient(o);
            array.put(value);
        }

        return array.toString(compactJSON);
    }


    @Override
    protected void processSubmission(String controlName)
    {
        String parameterValue = request.getParameter(controlName);

        JSONArray values = new JSONArray(parameterValue);

        // Use a couple of local variables to cut down on access via bindings

        Collection<Object> selected = this.selected;

        selected.clear();

        ValueEncoder encoder = this.encoder;

        // TODO: Validation error if the model does not contain a value.

        int count = values.length();
        for (int i = 0; i < count; i++)
        {
            String value = values.getString(i);

            Object objectValue = encoder.toValue(value);

            selected.add(objectValue);
        }

        putPropertyNameIntoBeanValidationContext("selected");

        try
        {
            fieldValidationSupport.validate(selected, resources, validate);

            this.selected = selected;
        } catch (final ValidationException e)
        {
            validationTracker.recordError(this, e.getMessage());
        }

        removePropertyNameFromBeanValidationContext();
    }

    void beginRender()
    {
        String clientId = getClientId();

        // The client side just need to know the id of the selected (right column) select;
        // it can take it from there.
        javaScriptSupport.require("t5/core/palette").with(clientId);
    }

    /**
     * Prevent the body from rendering.
     */
    boolean beforeRenderBody()
    {
        return false;
    }

    /**
     * Computes a default value for the "validate" parameter using
     * {@link org.apache.tapestry5.services.FieldValidatorDefaultSource}.
     */
    Binding defaultValidate()
    {
        return this.defaultProvider.defaultValidatorBinding("selected", this.resources);
    }

    String toClient(Object value)
    {
        return encoder.toClient(value);
    }


    @Override
    public boolean isRequired()
    {
        return validate.isRequired();
    }

    public String getDisabledValue()
    {
        return disabled ? "disabled" : null;
    }

    void onBeginRenderFromSelected(MarkupWriter writer)
    {
        validate.render(writer);
    }
}
