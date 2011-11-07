// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.base.AbstractField;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A list of checkboxes, allowing selection of multiple items in a list.
 * <p/>
 * For an alternative component that can be used for similar purposes, see
 * {@link Palette}.
 *
 * @tapestrydoc
 * @see Form
 * @see Palette
 * @since 5.3
 */
public class Checklist extends AbstractField
{

    /**
     * Model used to define the values and labels used when rendering the
     * checklist.
     */
    @Parameter(required = true)
    private SelectModel model;

    /**
     * The list of selected values from the
     * {@link org.apache.tapestry5.SelectModel}. This will be updated when the
     * form is submitted. If the value for the parameter is null, a new list
     * will be created, otherwise the existing list will be cleared. If unbound,
     * defaults to a property of the container matching this component's id.
     */
    @Parameter(required = true, autoconnect = true)
    private List<Object> selected;

    /**
     * A ValueEncoder used to convert server-side objects (provided from the
     * "source" parameter) into unique client-side strings (typically IDs) and
     * back. Note: this component does NOT support ValueEncoders configured to
     * be provided automatically by Tapestry.
     */
    @Parameter(required = true, allowNull = false)
    private ValueEncoder<Object> encoder;

    /**
     * The object that will perform input validation. The validate binding prefix is generally used to provide
     * this object in a declarative fashion.
     */
    @Parameter(defaultPrefix = BindingConstants.VALIDATE)
    @SuppressWarnings("unchecked")
    private FieldValidator<Object> validate;

    @Inject
    private Request request;

    @Inject
    private FieldValidationSupport fieldValidationSupport;

    @Environmental
    private ValidationTracker tracker;

    @Inject
    private ComponentResources componentResources;

    @Inject
    private ComponentDefaultProvider defaultProvider;

    @Inject
    private JavaScriptSupport javaScriptSupport;

    @Property
    private List<Renderable> availableOptions;

    private MarkupWriter markupWriter;

    private final class RenderRadio implements Renderable
    {
        private final OptionModel model;

        private RenderRadio(final OptionModel model)
        {
            this.model = model;
        }

        public void render(MarkupWriter writer)
        {
            final String clientId = javaScriptSupport.allocateClientId(componentResources);

            final String clientValue = encoder.toClient(model.getValue());

            final Element checkbox = writer.element("input", "type", "checkbox", "id", clientId, "name", getControlName(), "value", clientValue);

            if (getSelected().contains(model.getValue()))
            {
                checkbox.attribute("checked", "checked");
            }
            writer.end();

            writer.element("label", "for", clientId);
            writer.write(model.getLabel());
            writer.end();
        }
    }

    void setupRender(final MarkupWriter writer)
    {
        markupWriter = writer;

        availableOptions = CollectionFactory.newList();

        final SelectModelVisitor visitor = new SelectModelVisitor()
        {
            public void beginOptionGroup(final OptionGroupModel groupModel)
            {
            }

            public void option(final OptionModel optionModel)
            {
                availableOptions.add(new RenderRadio(optionModel));
            }

            public void endOptionGroup(final OptionGroupModel groupModel)
            {
            }

        };

        model.visit(visitor);
    }

    @Override
    protected void processSubmission(final String controlName)
    {

        final String[] parameters = request.getParameters(controlName);

        List<Object> selected = this.selected;

        if (selected == null)
        {
            selected = CollectionFactory.newList();
        } else
        {
            selected.clear();
        }

        if (parameters != null)
        {
            for (final String value : parameters)
            {
                final Object objectValue = encoder.toValue(value);

                selected.add(objectValue);
            }

        }

        putPropertyNameIntoBeanValidationContext("selected");

        try
        {
            this.fieldValidationSupport.validate(selected, this.componentResources, this.validate);

            this.selected = selected;
        } catch (final ValidationException e)
        {
            this.tracker.recordError(this, e.getMessage());
        }

        removePropertyNameFromBeanValidationContext();
    }

    Set<Object> getSelected()
    {
        if (selected == null)
        {
            return Collections.emptySet();
        }

        return CollectionFactory.newSet(selected);
    }

    /**
     * Computes a default value for the "validate" parameter using
     * {@link org.apache.tapestry5.services.FieldValidatorDefaultSource}.
     */

    Binding defaultValidate()
    {
        return this.defaultProvider.defaultValidatorBinding("selected", this.componentResources);
    }

    @Override
    public boolean isRequired()
    {
        return validate.isRequired();
    }
}

