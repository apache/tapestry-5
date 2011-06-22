// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.base;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.PropertyConduit;
import org.apache.tapestry5.PropertyOverrides;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.beaneditor.PropertyModel;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.BeanBlockSource;
import org.apache.tapestry5.services.Core;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.PropertyOutputContext;

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
    @Parameter(required = true, allowNull = false)
    private PropertyModel model;

    /**
     * Used to search for block parameter overrides (this is normally the enclosing Grid component's resources).
     */
    @Parameter(required = true, allowNull = false)
    private PropertyOverrides overrides;

    /**
     * Identifies the object being rendered. The component will extract a property from the object and render its value
     * (or delegate to a {@link org.apache.tapestry5.Block} that will do so).
     */
    @Parameter(required = true)
    private Object object;

    /**
     * Source for property display blocks. This defaults to the default implementation of {@link
     * org.apache.tapestry5.services.BeanBlockSource}.
     */
    @Parameter(required = true, allowNull = false)
    private BeanBlockSource beanBlockSource;

    @Inject
    @Core
    private BeanBlockSource defaultBeanBlockSource;

    @Inject
    private Environment environment;

    private boolean mustPopEnvironment;

    BeanBlockSource defaultBeanBlockSource()
    {
        return defaultBeanBlockSource;
    }

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
        Block override = overrides.getOverrideBlock(overrideBlockId);

        if (override != null) return override;

        String datatype = model.getDataType();

        if (beanBlockSource.hasDisplayBlock(datatype))
        {
            PropertyOutputContext context = new PropertyOutputContext()
            {
                public Messages getMessages()
                {
                    return overrides.getOverrideMessages();
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

        String text = value == null ? "" : value.toString();

        if (InternalUtils.isNonBlank(text))
        {
            writer.write(text);
        }

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
        catch (NullPointerException ex)
        {
            throw new NullPointerException(BaseMessages.nullValueInPath(model.getPropertyName()));
        }
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
