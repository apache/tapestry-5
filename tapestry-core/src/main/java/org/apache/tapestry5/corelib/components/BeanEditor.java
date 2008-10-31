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

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentAction;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.PropertyOverrides;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.beaneditor.BeanModel;
import org.apache.tapestry5.corelib.internal.InternalMessages;
import org.apache.tapestry5.internal.beaneditor.BeanModelUtils;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.services.BeanEditContext;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.FormSupport;

import java.lang.annotation.Annotation;

/**
 * A component that generates a user interface for editing the properties of a bean. This is the central component of
 * the {@link BeanEditForm}, and utilizes a {@link PropertyEditor} for much of its functionality. This component places
 * a {@link BeanEditContext} into the environment.
 */
@SupportsInformalParameters
public class BeanEditor
{
    public static class Prepare implements ComponentAction<BeanEditor>
    {
        private static final long serialVersionUID = 6273600092955522585L;

        public void execute(BeanEditor component)
        {
            component.doPrepare();
        }

        @Override
        public String toString()
        {
            return "BeanEditor.Prepare";
        }
    }

    static class CleanupEnvironment implements ComponentAction<BeanEditor>
    {
        private static final long serialVersionUID = 6867226962459227016L;

        public void execute(BeanEditor component)
        {
            component.cleanupEnvironment();
        }

        @Override
        public String toString()
        {
            return "BeanEditor.CleanupEnvironment";
        }
    }

    private static final ComponentAction<BeanEditor> CLEANUP_ENVIRONMENT = new CleanupEnvironment();

    /**
     * The object to be edited by the BeanEditor. This will be read when the component renders and updated when the form
     * for the component is submitted. Typically, the container will listen for a "prepare" event, in order to ensure
     * that a non-null value is ready to be read or updated.
     */
    @Parameter(autoconnect = true)
    private Object object;

    /**
     * A comma-separated list of property names to be retained from the {@link org.apache.tapestry5.beaneditor.BeanModel}.
     * Only these properties will be retained, and the properties will also be reordered. The names are
     * case-insensitive.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String include;

    /**
     * A comma-separated list of property names to be removed from the {@link org.apache.tapestry5.beaneditor.BeanModel}.
     * The names are case-insensitive.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String exclude;

    /**
     * A comma-separated list of property names indicating the order in which the properties should be presented. The
     * names are case insensitive. Any properties not indicated in the list will be appended to the end of the display
     * order.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String reorder;

    /**
     * A comma-separated list of property names to be added to the {@link org.apache.tapestry5.beaneditor.BeanModel}.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String add;


    /**
     * The model that identifies the parameters to be edited, their order, and every other aspect. If not specified, a
     * default bean model will be created from the type of the object bound to the object parameter.
     */
    @Parameter
    @Property(write = false)
    private BeanModel model;

    /**
     * Where to search for local overrides of property editing blocks as block parameters. Further, the container of the
     * overrides is used as the source for overridden validation messages. This is normally the BeanEditor component
     * itself, but when the component is used within a BeanEditForm, it will be the BeanEditForm's resources that will
     * be searched.
     */
    @Parameter(value = "this", allowNull = false)
    @Property(write = false)
    private PropertyOverrides overrides;

    @Inject
    private BeanModelSource modelSource;

    @Inject
    private ComponentResources resources;

    @Inject
    private Environment environment;

    @Environmental
    private FormSupport formSupport;

    // Value that change with each change to the current property:

    @Property
    private String propertyName;

    /**
     * To support nested BeanEditors, we need to cache the object value inside {@link #doPrepare()}. See TAPESTRY-2460.
     */
    private Object cachedObject;

    // Needed for testing as well

    public Object getObject()
    {
        return cachedObject;
    }

    void setupRender()
    {
        formSupport.storeAndExecute(this, new Prepare());
    }

    void cleanupRender()
    {
        formSupport.storeAndExecute(this, CLEANUP_ENVIRONMENT);
    }

    /**
     * Used to initialize the model if necessary, to instantiate the object being edited if necessary, and to push the
     * BeanEditContext into the environment.
     */
    void doPrepare()
    {
        if (model == null)
        {
            Class type = resources.getBoundType("object");
            model = modelSource.createEditModel(type, overrides.getOverrideMessages());
        }

        BeanModelUtils.modify(model, add, include, exclude, reorder);

        // The only problem here is that if the bound property is backed by a persistent field, it
        // is assigned (and stored to the session, and propagated around the cluster) first,
        // before values are assigned.

        if (object == null)
        {
            try
            {
                object = model.newInstance();
            }
            catch (Exception ex)
            {
                String message = InternalMessages.failureInstantiatingObject(model.getBeanType(),
                                                                             resources.getCompleteId(),
                                                                             ex);
                throw new TapestryException(message, resources.getLocation(), ex);
            }
        }

        BeanEditContext context = new BeanEditContext()
        {
            public Class<?> getBeanClass()
            {
                return model.getBeanType();
            }

            public <T extends Annotation> T getAnnotation(Class<T> type)
            {
                return getBeanClass().getAnnotation(type);
            }
        };


        cachedObject = object;

        environment.push(BeanEditContext.class, context);
    }

    void cleanupEnvironment()
    {
        environment.pop(BeanEditContext.class);
    }

    // For testing
    void inject(ComponentResources resources, PropertyOverrides overrides, BeanModelSource source,
                Environment environment)
    {
        this.resources = resources;
        this.overrides = overrides;
        this.environment = environment;
        modelSource = source;
    }
}
