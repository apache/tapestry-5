// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.Heartbeat;

/**
 * Generates a &lt;label&gt; element for a particular field.
 * <p/>
 * A Label will render its body, if it has one.  However, in most cases it will not have a body, and will render its
 * {@linkplain org.apache.tapestry5.Field#getLabel() field's label} as it's body. Remember, however, that it is the
 * field label that will be used in any error messages. The Label component allows for client- and server-side
 * validation error decorations.
 */
@SupportsInformalParameters
public class Label
{
    /**
     * The for parameter is used to identify the {@link Field} linked to this label (it is named this way because it
     * results in the for attribute of the label element).
     */
    @Parameter(name = "for", required = true, allowNull = false, defaultPrefix = BindingConstants.COMPONENT)
    private Field field;

    @Environmental
    private Heartbeat heartbeat;

    @Environmental
    private ValidationDecorator decorator;

    @Inject
    private ComponentResources resources;

    private Element labelElement;

    /**
     * If true, then the body of the label element (in the template) is ignored. This is used when a designer places a
     * value inside the &lt;label&gt; element for WYSIWYG purposes, but it should be replaced with a different
     * (probably, localized) value at runtime. The default is false, so a body will be used if present and the field's
     * label will only be used if the body is empty or blank.
     */
    @Parameter
    private boolean ignoreBody;

    boolean beginRender(MarkupWriter writer)
    {
        final Field field = this.field;

        decorator.beforeLabel(field);

        labelElement = writer.element("label");

        resources.renderInformalParameters(writer);

        // Since we don't know if the field has rendered yet, we need to defer writing the for and id
        // attributes until we know the field has rendered (and set its clientId property). That's
        // exactly what Heartbeat is for.

        Runnable command = new Runnable()
        {
            public void run()
            {
                String fieldId = field.getClientId();

                labelElement.forceAttributes("for", fieldId, "id", fieldId + "-label");

                decorator.insideLabel(field, labelElement);
            }
        };

        heartbeat.defer(command);

        return !ignoreBody;
    }

    void afterRender(MarkupWriter writer)
    {
        // If the Label element has a body that renders some non-blank output, that takes precendence
        // over the label string provided by the field.

        boolean bodyIsBlank = InternalUtils.isBlank(labelElement.getChildMarkup());

        if (bodyIsBlank) writer.write(field.getLabel());

        writer.end(); // label

        decorator.afterLabel(field);
    }
}
