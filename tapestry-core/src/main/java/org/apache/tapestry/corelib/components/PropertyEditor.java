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

import org.apache.tapestry.*;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.services.*;

import java.util.Locale;

/**
 * Used to edit a single property of a bean. This is used primarily by {@link BeanEditForm}. Unlike
 * BeanEditForm, the object to be edited must already exist and the {@linkplain BeanModel model}
 * must be passed in explicitly.
 */
public class PropertyEditor
{
    /**
     * Configures and stores a {@link PropertyEditContext} into the {@link Environment}.
     */
    static class SetupEnvironment implements ComponentAction<PropertyEditor>
    {
        private static final long serialVersionUID = 5337049721509981997L;

        private final String _property;

        public SetupEnvironment(String property)
        {
            _property = property;
        }

        public void execute(PropertyEditor component)
        {
            component.setupEnvironment(_property);
        }
    }

    static class CleanupEnvironment implements ComponentAction<PropertyEditor>
    {
        private static final long serialVersionUID = 7878694042753046523L;

        public void execute(PropertyEditor component)
        {
            component.cleanupEnvironment();
        }
    }

    /**
     * The object to be edited by the BeanEditor. This will be read when the component renders and
     * updated when the form for the component is submitted. Typically, the container will listen
     * for a "prepare" event, in order to ensure that a non-null value is ready to be read or
     * updated.
     */
    @Parameter(required = true)
    private Object _object;

    /**
     * Where to search for local overrides of property editing blocks as block parameters. Further,
     * the container of the overrides is used as the source for overridden validation messages. This
     * is normally the component itself, but when the component is used within a BeanEditForm, it
     * will be the BeanEditForm's block parameter that will be searched.
     */
    @Parameter(value = "componentResources")
    private ComponentResources _overrides;

    /**
     * Identifies the property to be edited by the editor.
     */
    @Parameter(required = true)
    private String _property;

    /**
     * The model that identifies the parameters to be edited, their order, and every other aspect.
     */
    @Parameter(required = true)
    private BeanModel _model;

    @Inject
    private TranslatorDefaultSource _translatorDefaultSource;

    @Inject
    private FieldValidatorDefaultSource _fieldValidatorDefaultSource;

    @Inject
    private Environment _environment;

    @Inject
    private BeanBlockSource _beanBlockSource;

    @Inject
    private Messages _messages;

    @Inject
    private Locale _locale;

    @Inject
    private ComponentResources _resources;

    @Environmental
    private FormSupport _formSupport;

    private PropertyModel _propertyModel;

    /**
     * Creates a {@link PropertyEditContext} and pushes it onto the {@link Environment} stack.
     */
    void setupEnvironment(final String propertyName)
    {
        _propertyModel = _model.get(propertyName);

        PropertyEditContext context = new PropertyEditContext()
        {
            public Messages getContainerMessages()
            {
                return _resources.getContainerMessages();
            }

            public String getLabel()
            {
                return _propertyModel.getLabel();
            }

            public String getPropertyId()
            {
                return _propertyModel.getId();
            }

            public Class getPropertyType()
            {
                return _propertyModel.getPropertyType();
            }

            public Object getPropertyValue()
            {
                return _propertyModel.getConduit().get(_object);
            }

            public Translator getTranslator()
            {
                return _translatorDefaultSource.find(_propertyModel.getPropertyType());
            }

            public FieldValidator getValidator(Field field)
            {
                return _fieldValidatorDefaultSource.createDefaultValidator(field, propertyName,
                                                                           _overrides.getContainerMessages(), _locale,
                                                                           _propertyModel.getPropertyType(),
                                                                           _propertyModel.getConduit());
            }

            public void setPropertyValue(Object value)
            {
                _propertyModel.getConduit().set(_object, value);
            }
        };

        _environment.push(PropertyEditContext.class, context);
    }

    /**
     * Called at the end of the form render (or at the end of the form submission) to clean up the
     * {@link Environment} stack.
     */
    void cleanupEnvironment()
    {
        _environment.pop(PropertyEditContext.class);
    }

    /**
     * Record into the Form what's needed to process the property.
     */
    void setupRender()
    {
        // Sets up the PropertyEditContext for the duration of the render of this component
        // (which will include the duration of the editor block).

        _formSupport.storeAndExecute(this, new SetupEnvironment(_property));
    }

    /**
     * Records into the Form the cleanup logic for the property.
     */
    void cleanupRender()
    {
        // Removes the PropertyEditContext after this component (including the editor block)
        // has rendered.

        _formSupport.storeAndExecute(this, new CleanupEnvironment());
    }

    /**
     * Returns a Block for rendering the property. The Block will be able to access the
     * {@link PropertyEditContext} via the {@link Environmental} annotation.
     */
    Block beginRender()
    {
        Block override = _overrides.getBlockParameter(_propertyModel.getId());

        if (override != null)
        {
            return override;
        }

        String dataType = _propertyModel.getDataType();

        try
        {
            return _beanBlockSource.getEditBlock(dataType);
        }
        catch (RuntimeException ex)
        {
            String message = _messages.format("block-error", _propertyModel.getPropertyName(), dataType, _object, ex);

            throw new TapestryException(message, _resources.getLocation(), ex);
        }

    }

    /**
     * Returns false, to prevent the rendering of the body of the component. PropertyEditor should
     * not have a body.
     */
    boolean beforeRenderBody()
    {
        return false;
    }

    /**
     * Used for testing.
     */
    void inject(ComponentResources resources, ComponentResources overrides, PropertyModel propertyModel,
                BeanBlockSource beanBlockSource, Messages messages, Object object)
    {
        _resources = resources;
        _overrides = overrides;
        _propertyModel = propertyModel;
        _beanBlockSource = beanBlockSource;
        _messages = messages;
        _object = object;
    }
}
