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
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.beaneditor.PropertyModel;
import org.apache.tapestry.internal.TapestryInternalUtils;

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

    Object beginRender(MarkupWriter writer)
    {
        Block override = _resources.getBlockParameter(_model.getId() + "Cell");

        if (override != null)
            return override;

        Block builtin = _gridCellResources.findBlock(_model.getDataType());

        if (builtin != null)
            return builtin;

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

    /*
     * When rendering a Block instead of a literal value, the template will start to render but its
     * is effectively just some whitespace and we want to skip it entirely.
     */
    boolean beforeRenderTemplate()
    {
        return false;
    }

    public String getConvertedEnumValue()
    {
        Enum value = (Enum) _model.getConduit().get(_row);

        if (value == null)
            return null;

        return TapestryInternalUtils.getLabelForEnum(_resources.getMessages(), value);
    }
}
