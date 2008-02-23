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
    private PropertyModel _model;

    /**
     * Resources used to search for block parameter overrides (this is normally the enclosing Grid component's
     * resources).
     */
    @Parameter(required = true)
    private ComponentResources _overrides;

    /**
     * Identifies the object being rendered. The component will extract a property from the object and render its value
     * (or delegate to a {@link Block} that will do so).
     */
    @Parameter(required = true)
    private Object _object;

    @Inject
    private BeanBlockSource _beanBlockSource;

    @Inject
    private Environment _environment;

    private boolean _mustPopEnvironment;

    protected PropertyModel getPropertyModel()
    {
        return _model;
    }

    /**
     * Invoked from subclasses to do the rendering. The subclass controls the naming convention for locating an
     * overriding Block parameter (it is the name of the property possibly suffixed with a value).
     */
    protected Object renderPropertyValue(MarkupWriter writer, String overrideBlockId)
    {
        Block override = _overrides.getBlockParameter(overrideBlockId);

        if (override != null) return override;

        String datatype = _model.getDataType();

        if (_beanBlockSource.hasDisplayBlock(datatype))
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
                    return _model.getId();
                }

                public String getPropertyName()
                {
                    return _model.getPropertyName();
                }
            };

            _environment.push(PropertyOutputContext.class, context);
            _mustPopEnvironment = true;

            return _beanBlockSource.getDisplayBlock(datatype);
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
        PropertyConduit conduit = _model.getConduit();

        try
        {
            return conduit == null ? null : conduit.get(_object);
        }
        catch (final NullPointerException ex)
        {
            throw new NullPointerException(BaseMessages.nullValueInPath(_model.getPropertyName()));
        }
    }

    private Messages getOverrideMessages()
    {
        return _overrides.getContainerMessages();
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
        if (_mustPopEnvironment)
        {
            _environment.pop(PropertyOutputContext.class);
            _mustPopEnvironment = false;
        }
    }

    // Used for testing.
    void inject(final PropertyModel model, final Object object)
    {
        _model = model;
        _object = object;
    }
}
