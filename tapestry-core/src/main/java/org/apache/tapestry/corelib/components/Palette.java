// Copyright 2007 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newList;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newSet;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry.Asset;
import org.apache.tapestry.Binding;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.OptionGroupModel;
import org.apache.tapestry.OptionModel;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.Renderable;
import org.apache.tapestry.SelectModel;
import org.apache.tapestry.SelectModelVisitor;
import org.apache.tapestry.ValueEncoder;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.Path;
import org.apache.tapestry.corelib.base.AbstractField;
import org.apache.tapestry.internal.util.SelectModelRenderer;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.services.FormSupport;
import org.apache.tapestry.services.Request;

/**
 * Multiple selection component. Generates a UI consisting of two &lt;select&gt; elements configured
 * for multiple selection; the one on the left is the list of "available" elements, the one on the
 * right is "selected". Elements can be moved between the lists by clicking a button, or double
 * clicking an option (and eventually, via drag and drop).
 * <p>
 * The items in the available list are kept ordered as per {@link SelectModel} order. When items are
 * moved from the selected list to the available list, they items are inserted back into their
 * proper positions.
 * <p>
 * The Palette may operate in normal or re-orderable mode, controlled by the reorder parameter.
 * <p>
 * In normal mode, the items in the selected list are kept in the same "natural" order as the items
 * in the available list.
 * <p>
 * In re-order mode, items moved to the selected list are simply added to the bottom of the list. In
 * addition, two extra buttons appear to move items up and down within the selected list.
 * <p>
 * Much of the look and feel is driven by CSS, the default Tapestry CSS is used to set up the
 * columns, etc. By default, the &lt;select&gt; element's widths are driven by the length of the
 * longest &lt;option&gt;, and it is common to override this to a fixed value:
 * 
 * <pre>
 * &lt;style&gt;
 * DIV.t-palette SELECT { width: 300px; }
 * &lt;/style&gt;
 * </pre>
 * 
 * <p>
 * This ensures that the two columns are the same width, and that the column widths don't change as
 * items move back and forth.
 * <p>
 * Option groups within the {@link SelectModel} will be rendered, but are not supported by the many
 * browsers, and are not fully handled on the client side.
 */
public class Palette extends AbstractField
{
    // These all started as anonymous inner classes, and were refactored out to here.
    // I was chasing down one of those perplexing bytecode errors.

    private final class AvailableRenderer implements Renderable
    {
        public void render(MarkupWriter writer)
        {
            writer.element(
                    "select",
                    "id",
                    getClientId() + ":avail",
                    "multiple",
                    "multiple",
                    "size",
                    getSize(),
                    "name",
                    getElementName() + ":avail");

            writeDisabled(writer, isDisabled());

            for (Runnable r : _availableOptions)
                r.run();

            writer.end();
        }
    }

    private final class OptionGroupEnd implements Runnable
    {
        private final OptionGroupModel _model;

        private OptionGroupEnd(OptionGroupModel model)
        {
            _model = model;
        }

        public void run()
        {
            _renderer.endOptionGroup(_model);
        }
    }

    private final class OptionGroupStart implements Runnable
    {
        private final OptionGroupModel _model;

        private OptionGroupStart(OptionGroupModel model)
        {
            _model = model;
        }

        public void run()
        {
            _renderer.beginOptionGroup(_model);
        }
    }

    private final class RenderOption implements Runnable
    {
        private final OptionModel _model;

        private RenderOption(OptionModel model)
        {
            _model = model;
        }

        public void run()
        {
            _renderer.option(_model);
        }
    }

    private final class SelectedRenderer implements Renderable
    {
        public void render(MarkupWriter writer)
        {
            writer.element(
                    "select",
                    "id",
                    getClientId(),
                    "multiple",
                    "multiple",
                    "size",
                    getSize(),
                    "name",
                    getElementName());

            writeDisabled(writer, isDisabled());

            for (Object value : getSelected())
            {
                OptionModel model = _valueToOptionModel.get(value);

                _renderer.option(model);
            }

            writer.end();
        }
    }

    /** List of Runnable commands to render the available options. */
    private List<Runnable> _availableOptions;

    /**
     * The image to use for the deselect button (the default is a left pointing arrow).
     */
    @Parameter(value = "asset:deselect.png")
    private Asset _deselect;

    /**
     * Encoder used to translate between server-side objects and client-side strings.
     */
    @Parameter(required = true)
    private ValueEncoder<Object> _encoder;

    /**
     * Model used to define the values and labels used when rendering.
     */
    @Parameter(required = true)
    private SelectModel _model;

    /**
     * The image to use for the move down button (the default is a downward pointing arrow).
     */
    @Parameter(value = "asset:move_down.png")
    private Asset _moveDown;

    /**
     * The image to use for the move up button (the default is an upward pointing arrow).
     */
    @Parameter(value = "asset:move_up.png")
    private Asset _moveUp;

    @Inject
    @Path("palette.js")
    private Asset _paletteLibrary;

    /** Used to include scripting code in the rendered page. */
    @Environmental
    private PageRenderSupport _renderSupport;

    /** Needed to access query parameters when processing form submission. */
    @Inject
    private Request _request;

    private SelectModelRenderer _renderer;

    /**
     * The image to use for the select button (the default is a right pointing arrow).
     */
    @Parameter(value = "asset:select.png")
    private Asset _select;

    /**
     * The list of selected values from the {@link SelectModel}. This will be updated when the form
     * is submitted. If the value for the parameter is null, a new list will be created, otherwise
     * the existing list will be cleared. If unbound, defaults to a property of the container
     * matching this component's id.
     */
    @Parameter(required = true)
    private List<Object> _selected;

    /**
     * If true, then additional buttons are provided on the client-side to allow for re-ordering of
     * the values.
     */
    @Parameter("false")
    private boolean _reorder;

    /**
     * Used during rendering to identify the options corresponding to selected values (from the
     * selected parameter), in the order they should be displayed on the page.
     */
    private List<OptionModel> _selectedOptions;

    private Map<Object, OptionModel> _valueToOptionModel;

    /**
     * Number of rows to display.
     */
    @Parameter(value = "10")
    private int _size;

    /**
     * Defaults the selected parameter to a container property whose name matches this component's
     * id.
     */
    final Binding defaultSelected()
    {
        return createDefaultParameterBinding("selected");
    }

    public Renderable getAvailableRenderer()
    {
        return new AvailableRenderer();
    }

    public Asset getDeselect()
    {
        return _deselect;
    }

    public Asset getMoveDown()
    {
        return _moveDown;
    }

    public Asset getMoveUp()
    {
        return _moveUp;
    }

    public Asset getSelect()
    {
        return _select;
    }

    public Renderable getSelectedRenderer()
    {
        return new SelectedRenderer();
    }

    @Override
    protected void processSubmission(FormSupport formSupport, String elementName)
    {
        String values = _request.getParameter(elementName + ":values");

        // Use a couple of local variables to cut down on access via bindings

        List<Object> selected = _selected;

        if (selected == null)
            selected = newList();
        else
            selected.clear();

        ValueEncoder encoder = _encoder;

        if (InternalUtils.isNonBlank(values))
        {
            for (String value : values.split(";"))
            {
                Object objectValue = encoder.toValue(value);

                selected.add(objectValue);
            }
        }

        _selected = selected;
    }

    private void writeDisabled(MarkupWriter writer, boolean disabled)
    {
        if (disabled) writer.attributes("disabled", "disabled");
    }

    void beginRender(MarkupWriter writer)
    {
        String sep = "";
        StringBuilder selectedValues = new StringBuilder();

        for (OptionModel selected : _selectedOptions)
        {

            Object value = selected.getValue();
            String clientValue = _encoder.toClient(value);

            selectedValues.append(sep);
            selectedValues.append(clientValue);

            sep = ";";
        }

        StringBuilder naturalOrder = new StringBuilder();
        sep = "";
        for (String value : _naturalOrder)
        {
            naturalOrder.append(sep);
            naturalOrder.append(value);
            sep = ";";
        }

        String clientId = getClientId();

        _renderSupport.addScriptLink(_paletteLibrary);

        _renderSupport.addScript(
                "new Tapestry.Palette('%s', %s, '%s');",
                clientId,
                _reorder,
                naturalOrder);

        writer.element(
                "input",
                "type",
                "hidden",
                "id",
                clientId + ":values",
                "name",
                getElementName() + ":values",
                "value",
                selectedValues);
        writer.end();
    }

    /** Prevent the body from rendering. */
    boolean beforeRenderBody()
    {
        return false;
    }

    /** The natural order of elements, in terms of their client ids. */
    private List<String> _naturalOrder;

    @SuppressWarnings("unchecked")
    void setupRender(MarkupWriter writer)
    {
        _valueToOptionModel = newMap();
        _availableOptions = newList();
        _selectedOptions = newList();
        _naturalOrder = newList();
        _renderer = new SelectModelRenderer(writer, _encoder);

        final Set selectedSet = newSet(getSelected());

        SelectModelVisitor visitor = new SelectModelVisitor()
        {
            public void beginOptionGroup(OptionGroupModel groupModel)
            {
                _availableOptions.add(new OptionGroupStart(groupModel));
            }

            public void endOptionGroup(OptionGroupModel groupModel)
            {
                _availableOptions.add(new OptionGroupEnd(groupModel));
            }

            public void option(OptionModel optionModel)
            {
                Object value = optionModel.getValue();

                boolean isSelected = selectedSet.contains(value);

                String clientValue = toClient(value);

                _naturalOrder.add(clientValue);

                if (isSelected)
                {
                    _selectedOptions.add(optionModel);
                    _valueToOptionModel.put(value, optionModel);
                    return;
                }

                _availableOptions.add(new RenderOption(optionModel));
            }

        };

        _model.visit(visitor);
    }

    // Avoids a strange Javassist bytecode error, c'est lavie!
    int getSize()
    {
        return _size;
    }

    String toClient(Object value)
    {
        return _encoder.toClient(value);
    }

    List<Object> getSelected()
    {
        if (_selected == null) return Collections.emptyList();

        return _selected;
    }

    public boolean getReorder()
    {
        return _reorder;
    }
}
