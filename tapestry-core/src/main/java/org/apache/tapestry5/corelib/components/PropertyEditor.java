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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.*;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.services.*;

import java.lang.annotation.Annotation;
import java.util.Locale;

/**
 * Used to edit a single property of a bean. This is used primarily by {@link BeanEditForm}. Unlike BeanEditForm, the
 * object to be edited must already exist and the {@linkplain BeanModel model} must be passed in explicitly.
 */
public class PropertyEditor
{
    /**
     * Configures and stores a {@link PropertyEditContext} into the {@link Environment}.
     */
    static class SetupEnvironment implements ComponentAction<PropertyEditor>
    {
        private static final long serialVersionUID = 5337049721509981997L;

        private final String property;

        public SetupEnvironment(String property)
        {
            this.property = property;
        }

        public void execute(PropertyEditor component)
        {
            component.setupEnvironment(property);
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
     * The object to be edited by the BeanEditor. This will be read when the component renders and updated when the form
     * for the component is submitted. Typically, the container will listen for a "prepare" event, in order to ensure
     * that a non-null value is ready to be read or updated.
     */
    @Parameter(required = true)
    private Object object;

    /**
     * Where to search for local overrides of property editing blocks as block parameters. This is normally the
     * containing component of the PropertyEditor, but when the component is used within a BeanEditor, it will be the
     * BeanEditForm's block parameters that will be searched.
     */
    @Parameter(value = "componentResources")
    private ComponentResources overrides;

    /**
     * Identifies the property to be edited by the editor.
     */
    @Parameter(required = true)
    private String property;

    /**
     * The model that identifies the parameters to be edited, their order, and every other aspect.
     */
    @Parameter(required = true)
    private BeanModel model;

    @Inject
    private FieldValidatorDefaultSource fieldValidatorDefaultSource;

    @Inject
    private Environment environment;

    @Inject
    private BeanBlockSource beanBlockSource;

    @Inject
    private Messages messages;

    @Inject
    private Locale locale;

    @Inject
    private ComponentResources resources;

    @Environmental
    private FormSupport formSupport;

    private PropertyModel propertyModel;

    @Inject
    private TranslatorSource translatorSource;

    /**
     * Creates a {@link org.apache.tapestry5.services.PropertyEditContext} and pushes it onto the {@link
     * org.apache.tapestry5.services.Environment} stack.
     */
    void setupEnvironment(final String propertyName)
    {
        propertyModel = model.get(propertyName);

        PropertyEditContext context = new PropertyEditContext()
        {
            public Messages getContainerMessages()
            {
                return overrides.getContainerMessages();
            }

            public String getLabel()
            {
                return propertyModel.getLabel();
            }

            public String getPropertyId()
            {
                return propertyModel.getId();
            }

            public Class getPropertyType()
            {
                return propertyModel.getPropertyType();
            }

            public Object getPropertyValue()
            {
                return propertyModel.getConduit().get(object);
            }

            public Translator getTranslator()
            {
                return translatorSource.getByType(propertyModel.getPropertyType());
            }

            public FieldValidator getValidator(Field field)
            {
                return fieldValidatorDefaultSource.createDefaultValidator(field, propertyName,
                                                                          overrides.getContainerMessages(), locale,
                                                                          propertyModel.getPropertyType(),
                                                                          propertyModel.getConduit());
            }

            public void setPropertyValue(Object value)
            {
                propertyModel.getConduit().set(object, value);
            }

            public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
            {
                return propertyModel.getAnnotation(annotationClass);
            }
        };

        environment.push(PropertyEditContext.class, context);
    }

    /**
     * Called at the end of the form render (or at the end of the form submission) to clean up the {@link Environment}
     * stack.
     */
    void cleanupEnvironment()
    {
        environment.pop(PropertyEditContext.class);
    }

    /**
     * Record into the Form what's needed to process the property.
     */
    void setupRender()
    {
        // Sets up the PropertyEditContext for the duration of the render of this component
        // (which will include the duration of the editor block).

        formSupport.storeAndExecute(this, new SetupEnvironment(property));
    }

    /**
     * Records into the Form the cleanup logic for the property.
     */
    void cleanupRender()
    {
        // Removes the PropertyEditContext after this component (including the editor block)
        // has rendered.

        formSupport.storeAndExecute(this, new CleanupEnvironment());
    }

    /**
     * Returns a Block for rendering the property. The Block will be able to access the {@link PropertyEditContext} via
     * the {@link Environmental} annotation.
     */
    Block beginRender()
    {
        Block override = overrides.getBlockParameter(propertyModel.getId());

        if (override != null)
        {
            return override;
        }

        String dataType = propertyModel.getDataType();

        try
        {
            return beanBlockSource.getEditBlock(dataType);
        }
        catch (RuntimeException ex)
        {
            String message = messages.format("block-error", propertyModel.getPropertyName(), dataType, object, ex);

            throw new TapestryException(message, resources.getLocation(), ex);
        }

    }

    /**
     * Returns false, to prevent the rendering of the body of the component. PropertyEditor should not have a body.
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
        this.resources = resources;
        this.overrides = overrides;
        this.propertyModel = propertyModel;
        this.beanBlockSource = beanBlockSource;
        this.messages = messages;
        this.object = object;
    }
}
