// Copyright 2010-2013 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.HeartbeatDeferred;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.dom.Element;

/**
 * Presents validation errors of a single field. Must be enclosed by a
 * {@link org.apache.tapestry5.corelib.components.Form} component.
 * 
 * @since 5.2.0
 * @tapestrydoc
 * @deprecated Deprecated in 5.4 with no replacement; the significant changes in 5.4 ensure that validation errors
 * on re-rendered forms appear with the controls.
 */
public class Error
{
    /**
     * The for parameter is used to identify the {@link Field} to present errors of.
     */
    @Parameter(name = "for", required = true, allowNull = false, defaultPrefix = BindingConstants.COMPONENT)
    private Field field;

    /**
     * The CSS class for the div element rendered by the component. The default value is "t-error-single".
     */
    @Parameter(name = "class")
    private String className = CSSClassConstants.ERROR_SINGLE;

    @Environmental(false)
    private ValidationTracker tracker;

    void beginRender(final MarkupWriter writer)
    {
        if (tracker == null)
            throw new RuntimeException("The Error component must be enclosed by a Form component.");

        Element element = writer.element("div");

        updateElement(element);

        writer.end();
    }

    @HeartbeatDeferred
    private void updateElement(final Element element)
    {
        final String error = tracker.getError(field);

        if (error == null)
        {
            element.remove();
        }
        else
        {
            element.forceAttributes("class", className);
            element.text(error);
        }
    }

}
