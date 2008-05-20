// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Field;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.ValidationDecorator;
import org.apache.tapestry.annotation.*;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.ioc.annotation.Inject;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.services.Heartbeat;

/**
 * Generates a &lt;label&gt; element for a particular field.
 * <p/>
 * A Label will render its body, if it has one.  However, in most cases it will not have a body, and will render its
 * {@linkplain org.apache.tapestry.Field#getLabel() field's label} as it's body. Remember, however, that it is the field
 * label that will be used in any error messages. The Label component allows for client- and server-side validation
 * error decorations.
 */
@SupportsInformalParameters
public class Label
{
    /**
     * The for parameter is used to identify the {@link Field} linked to this label (it is named this way because it
     * results in the for attribute of the label element).
     */
    @Parameter(name = "for", required = true, defaultPrefix = "component")
    private Field field;

    @Environmental
    private Heartbeat heartbeat;

    @Environmental
    private ValidationDecorator decorator;

    @Inject
    private ComponentResources resources;

    private Element labelElement;

    @BeginRender
    void begin(MarkupWriter writer)
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

                labelElement.forceAttributes("for", fieldId, "id", fieldId + ":label");

                decorator.insideLabel(field, labelElement);
            }
        };

        heartbeat.defer(command);
    }

    @AfterRender
    void after(MarkupWriter writer)
    {
        // If the Label element has a body that renders some non-blank output, that takes precendence
        // over the label string provided by the field.

        boolean bodyIsBlank = InternalUtils.isBlank(labelElement.getChildMarkup());

        if (bodyIsBlank) writer.write(field.getLabel());

        writer.end(); // label

        decorator.afterLabel(field);
    }
}
