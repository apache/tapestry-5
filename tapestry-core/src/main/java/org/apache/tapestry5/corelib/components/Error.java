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

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.HeartbeatDeferred;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.Inject;

/**
 * Provides a client-side element to contain validation errors; this renders as a {@code <p class="help-block">}.
 * Must be enclosed by a
 * {@link org.apache.tapestry5.corelib.components.Form} component and assumes the field and the Error component
 * are enclosed by a {@code <div class="form-group">}.
 *
 * @tapestrydoc
 * @since 5.2.0
 */
@SupportsInformalParameters
public class Error
{
    /**
     * The for parameter is used to identify the {@link Field} to present errors of.
     */
    @Parameter(name = "for", required = true, allowNull = false, defaultPrefix = BindingConstants.COMPONENT)
    private Field field;

    @Inject
    private ComponentResources resources;

    boolean beginRender(final MarkupWriter writer)
    {
        Element element = writer.element("p", "class", "help-block");

        resources.renderInformalParameters(writer);

        updateElement(element);

        writer.end();

        return false;
    }

    @HeartbeatDeferred
    private void updateElement(final Element element)
    {
        element.attribute("data-error-block-for", field.getClientId());
    }
}
