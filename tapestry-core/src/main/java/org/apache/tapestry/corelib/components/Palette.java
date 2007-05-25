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
import org.apache.tapestry.ValueEncoder;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.Path;
import org.apache.tapestry.corelib.base.AbstractField;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.services.FormSupport;
import org.apache.tapestry.services.Request;

/**
 * Multiple selection component. Generates a UI consisting of two &lt;select&gt; elements configured
 * for multiple selection; the one on the left is the list of "available" elements, the one on the
 * right is "selected". Elements can be moved between the lists by clicking a button, or double
 * clicking an option (and eventually, via drag and drop).
 * <p>
 * Much of the look and feel is driven by CSS, the default Tapestry CSS is used to set up the
 * columns, etc. By default, the &lt;select&gt; element's widths are driven by the length of the
 * longest &lt;option&gt;, and it is common to override this:
 * 
 * <pre>
 * &lt;style&gt;
 * DIV.t-palette SELECT { width: 300px; }
 * &lt;/style&gt;
 * </pre>
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

            for (Renderable r : _availableOptions)
                r.render(writer);

            writer.end();
        }
    }

    private final class OptionGroupEnd implements Renderable
    {
        public void render(MarkupWriter writer)
        {
            writer.end();
        }
    }

    private final class OptionGroupStart implements Renderable
    {
        private final OptionGroupModel _model;

        private OptionGroupStart(OptionGroupModel model)
        {
            _model = model;
        }

        public void render(MarkupWriter writer)
        {
            writer.element("optgroup", "label", _model.getLabel());
            writeDisabled(writer, _model.isDisabled());

            writeAttributes(writer, _model.getAttributes());
        }
    }

    private final class RenderOption implements Renderable
    {
        private final OptionModel _model;

        private RenderOption(OptionModel model)
        {
            _model = model;
        }

        public void render(MarkupWriter writer)
        {
            renderOption(writer, _model);
        }
    }

    private final class SelectedRenderer implements Renderable
    {
        public void render(MarkupWriter writer)
        {
            // TODO: Support disabled parameter

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

                renderOption(writer, model);
            }

            writer.end();
        }
    }

    private List<Renderable> _availableOptions;

    /**
     * The image to use for the deselect button (the default is a left pointing arrow).
     */
    @Parameter(value = "asset:deselect.png")
    private Asset _deselect;

    @Parameter(required = true)
    private ValueEncoder<Object> _encoder;

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

    @Environmental
    private PageRenderSupport _renderSupport;

    @Inject
    private Request _request;

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

    private List<OptionModel> _selectedOptions;

    private Map<Object, OptionModel> _valueToOptionModel;

    /**
     * Number of rows to display.
     */
    @Parameter(value = "10")
    private int _size;

    final Binding defaultSelected()
    {
        return createDefaultParameterBinding("value");
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

        String clientId = getClientId();

        _renderSupport.addScriptLink(_paletteLibrary);

        _renderSupport.addScript("new Tapestry.Palette('%s');", clientId);

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

    boolean beforeRenderBody()
    {
        return false;
    }

    void renderOption(MarkupWriter writer, OptionModel model)
    {
        String clientValue = _encoder.toClient(model.getValue());

        writer.element("option", "value", clientValue);

        writeDisabled(writer, model.isDisabled());

        writeAttributes(writer, model.getAttributes());

        writer.write(model.getLabel());
        writer.end();
    }

    @SuppressWarnings("unchecked")
    void setupRender()
    {
        _valueToOptionModel = newMap();
        _availableOptions = newList();
        _selectedOptions = newList();

        Set selectedSet = newSet(getSelected());

        SelectModel model = _model;

        if (model.getOptionGroups() != null)
        {
            for (final OptionGroupModel groupModel : model.getOptionGroups())
            {
                _availableOptions.add(new OptionGroupStart(groupModel));

                prerender(groupModel.getOptions(), selectedSet);

                _availableOptions.add(new OptionGroupEnd());
            }
        }

        prerender(_model.getOptions(), selectedSet);
    }

    private void prerender(List<OptionModel> options, Set<Object> selectedSet)
    {
        if (options == null) return;

        for (final OptionModel model : options)
        {
            Object value = model.getValue();

            boolean isSelected = selectedSet.contains(value);

            if (isSelected)
            {
                _selectedOptions.add(model);
                _valueToOptionModel.put(value, model);
                continue;
            }

            _availableOptions.add(new RenderOption(model));
        }
    }

    private void writeAttributes(MarkupWriter writer, Map<String, String> attributes)
    {
        if (attributes == null) return;

        for (Map.Entry<String, String> e : attributes.entrySet())
            writer.attributes(e.getKey(), e.getValue());
    }

    // Avoids a strange Javassist bytecode error, c'est lavie!
    int getSize()
    {
        return _size;
    }

    List<Object> getSelected()
    {
        if (_selected == null) return Collections.emptyList();

        return _selected;
    }
}
