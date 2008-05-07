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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.Binding;
import org.apache.tapestry.ComponentAction;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.TapestryConstants;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.Property;
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.beaneditor.BeanModel;
import org.apache.tapestry.corelib.internal.InternalMessages;
import org.apache.tapestry.internal.beaneditor.BeanModelUtils;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.services.BeanModelSource;
import org.apache.tapestry.services.ComponentDefaultProvider;
import org.apache.tapestry.services.FormSupport;

/**
 * A component that generates a user interface for editing the properties of a bean. This is the central component of
 * the {@link BeanEditForm}, and utilizes a {@link PropertyEditor} for much of its functionality.
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
    }

    /**
     * The object to be edited by the BeanEditor. This will be read when the component renders and updated when the form
     * for the component is submitted. Typically, the container will listen for a "prepare" event, in order to ensure
     * that a non-null value is ready to be read or updated.
     */
    @Parameter
    private Object object;

    /**
     * A comma-separated list of property names to be retained from the {@link org.apache.tapestry.beaneditor.BeanModel}.
     * Only these properties will be retained, and the properties will also be reordered. The names are
     * case-insensitive.
     */
    @SuppressWarnings("unused")
    @Parameter(defaultPrefix = TapestryConstants.LITERAL_BINDING_PREFIX)
    private String include;

    /**
     * A comma-separated list of property names to be removed from the {@link org.apache.tapestry.beaneditor.BeanModel}.
     * The names are case-insensitive.
     */
    @Parameter(defaultPrefix = TapestryConstants.LITERAL_BINDING_PREFIX)
    private String exclude;

    /**
     * A comma-separated list of property names indicating the order in which the properties should be presented. The
     * names are case insensitive. Any properties not indicated in the list will be appended to the end of the display
     * order.
     */
    @Parameter(defaultPrefix = TapestryConstants.LITERAL_BINDING_PREFIX)
    private String reorder;

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
    @Parameter(value = "componentResources")
    @Property(write = false)
    private ComponentResources overrides;

    @Inject
    private BeanModelSource modelSource;

    @Inject
    private ComponentDefaultProvider defaultProvider;

    @Inject
    private ComponentResources resources;

    @Environmental
    private FormSupport formSupport;

    // Value that change with each change to the current property:

    @Property
    private String propertyName;

    /**
     * Defaults the object parameter to a property of the container matching the BeanEditForm's id.
     */
    Binding defaultObject()
    {
        return defaultProvider.defaultBinding("object", resources);
    }

    // Needed for testing as well

    public Object getObject()
    {
        return object;
    }

    void setupRender()
    {
        formSupport.storeAndExecute(this, new Prepare());
    }

    void doPrepare()
    {
        if (model == null)
        {
            Class type = resources.getBoundType("object");
            model = modelSource.create(type, true, overrides.getContainerResources());
        }

        BeanModelUtils.modify(model, null, include, exclude, reorder);

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

    }

    // For testing
    void inject(ComponentResources resources, ComponentResources overrides, BeanModelSource source)
    {
        this.resources = resources;
        this.overrides = overrides;
        modelSource = source;
    }
}
