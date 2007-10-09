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

import org.apache.tapestry.Block;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.BeanBlockSource;
import org.apache.tapestry.services.Environment;
import org.apache.tapestry.services.PropertyDisplayContext;

/**
 * Part of {@link Grid} that renders a single data cell. GridCell is used inside a pair of loops;
 * the outer loop for each row, the inner loop for each property of the row.
 */
public class GridCell
{
    /** Model for property displayed by the cell. */
    @Parameter(required = true)
    private PropertyModel _model;

    /**
     * Resources used to search for block parameter overrides (this is normally the enclosing Grid
     * component's resources).
     */
    @Parameter(required = true)
    private ComponentResources _resources;

    /**
     * Identifies the object being rendered. The GridCell will extract a property from the row and
     * render its value (or delegate to a {@link Block} that will do so).
     */
    @Parameter(required = true)
    private Object _row;

    @Inject
    private ComponentResources _gridCellResources;

    @Inject
    private BeanBlockSource _beanBlockSource;

    @Inject
    private Environment _environment;

    private boolean _mustPopEnvironment;

    Object beginRender(MarkupWriter writer)
    {
        Block override = _resources.getBlockParameter(_model.getId() + "Cell");

        if (override != null) return override;

        String datatype = _model.getDataType();

        if (_beanBlockSource.hasDisplayBlock(datatype))
        {
            PropertyDisplayContext context = new PropertyDisplayContext()
            {
                public Messages getContainerMessages()
                {
                    return GridCell.this.getContainerMessages();
                }

                public Object getPropertyValue()
                {
                    return readPropertyForRow();
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

            _environment.push(PropertyDisplayContext.class, context);
            _mustPopEnvironment = true;

            return _beanBlockSource.getDisplayBlock(datatype);
        }

        Block block = _gridCellResources.findBlock(datatype);

        if (block != null) return block;

        Object value = _model.getConduit().get(_row);

        if (value == null)
        {
            writer.writeRaw("&nbsp;");
            return false;
        }

        writer.write(value.toString());

        // Don't render anything else

        return false;
    }

    private Object readPropertyForRow()
    {
        return _model.getConduit().get(_row);
    }

    private Messages getContainerMessages()
    {
        return _resources.getMessages();
    }

    /*
     * When rendering a Block instead of a literal value, the template will start to render but its
     * is effectively just some whitespace and we want to skip it entirely.
     */
    boolean beforeRenderTemplate()
    {
        return false;
    }

    void afterRender()
    {
        if (_mustPopEnvironment)
        {
            _environment.pop(PropertyDisplayContext.class);
            _mustPopEnvironment = false;
        }
    }
}
