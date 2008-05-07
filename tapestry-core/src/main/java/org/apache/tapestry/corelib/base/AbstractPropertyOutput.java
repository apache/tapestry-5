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

package org.apache.tapestry.corelib.base;

import org.apache.tapestry.Block;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.PropertyConduit;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.BeanBlockSource;
import org.apache.tapestry.services.Environment;
import org.apache.tapestry.services.PropertyOutputContext;

/**
 * Base class for components that output a property value using a {@link PropertyModel}. There's a relationship between
 * such a component and its container, as the container may provide messages in its message catalog needed by the {@link
 * Block}s that render the values. In addition, the component may be passed Block parameters that are output overrides
 * for specified properties.
 * <p/>
 * Subclasses will implement a <code>beginRender()</code> method that invokes {@link #renderPropertyValue(MarkupWriter,
 * String)}.
 *
 * @see BeanBlockSource
 */
public abstract class AbstractPropertyOutput
{
    /**
     * Model for property displayed by the cell.
     */
    @Parameter(required = true)
    private PropertyModel model;

    /**
     * Resources used to search for block parameter overrides (this is normally the enclosing Grid component's
     * resources).
     */
    @Parameter(required = true)
    private ComponentResources overrides;

    /**
     * Identifies the object being rendered. The component will extract a property from the object and render its value
     * (or delegate to a {@link org.apache.tapestry.Block} that will do so).
     */
    @Parameter(required = true)
    private Object object;

    @Inject
    private BeanBlockSource beanBlockSource;

    @Inject
    private Environment environment;

    private boolean mustPopEnvironment;

    protected PropertyModel getPropertyModel()
    {
        return model;
    }

    /**
     * Invoked from subclasses to do the rendering. The subclass controls the naming convention for locating an
     * overriding Block parameter (it is the name of the property possibly suffixed with a value).
     */
    protected Object renderPropertyValue(MarkupWriter writer, String overrideBlockId)
    {
        Block override = overrides.getBlockParameter(overrideBlockId);

        if (override != null) return override;

        String datatype = model.getDataType();

        if (beanBlockSource.hasDisplayBlock(datatype))
        {
            PropertyOutputContext context = new PropertyOutputContext()
            {
                public Messages getMessages()
                {
                    return getOverrideMessages();
                }

                public Object getPropertyValue()
                {
                    return readPropertyForObject();
                }

                public String getPropertyId()
                {
                    return model.getId();
                }

                public String getPropertyName()
                {
                    return model.getPropertyName();
                }
            };

            environment.push(PropertyOutputContext.class, context);
            mustPopEnvironment = true;

            return beanBlockSource.getDisplayBlock(datatype);
        }

        Object value = readPropertyForObject();

        if (value == null)
        {
            writer.writeRaw("&nbsp;");
            return false;
        }

        writer.write(value.toString());

        // Don't render anything else

        return false;
    }

    Object readPropertyForObject()
    {
        PropertyConduit conduit = model.getConduit();

        try
        {
            return conduit == null ? null : conduit.get(object);
        }
        catch (final NullPointerException ex)
        {
            throw new NullPointerException(BaseMessages.nullValueInPath(model.getPropertyName()));
        }
    }

    private Messages getOverrideMessages()
    {
        return overrides.getContainerMessages();
    }

    /**
     * Returns false; there's no template and this prevents the body from rendering.
     */
    boolean beforeRenderTemplate()
    {
        return false;
    }

    void afterRender()
    {
        if (mustPopEnvironment)
        {
            environment.pop(PropertyOutputContext.class);
            mustPopEnvironment = false;
        }
    }

    // Used for testing.
    void inject(final PropertyModel model, final Object object)
    {
        this.model = model;
        this.object = object;
    }
}
