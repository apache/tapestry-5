// Copyright 2006 The Apache Software Foundation
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
import org.apache.tapestry.annotations.AfterRender;
import org.apache.tapestry.annotations.BeforeRenderBody;
import org.apache.tapestry.annotations.BeginRender;
import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.annotations.Inject;
import org.apache.tapestry.annotations.Parameter;
import org.apache.tapestry.annotations.SupportsInformalParameters;
import org.apache.tapestry.dom.Element;
import org.apache.tapestry.services.Heartbeat;

/** Generates a &lt;label&gt; element for a particular field. */
@ComponentClass
@SupportsInformalParameters
public class Label
{
    /**
     * The for parameter is used to identify the {@link Field} linked to this label (it is named
     * this way because it results in the for attribute of the label element).
     */
    @Parameter(name = "for", required = true, defaultPrefix = "component")
    private Field _field;

    @Environmental
    private Heartbeat _heartbeat;

    @Environmental
    private ValidationDecorator _decorator;

    @Inject
    private ComponentResources _resources;

    @BeginRender
    void begin(MarkupWriter writer)
    {
        final Field field = _field;

        final Element element = writer.element("label");

        _resources.renderInformalParameters(writer);

        // Uh oh! Referencing a private field (that happens to get instrumented up the wazoo) from
        // a inner class causes a java.lang.Verify error (Unable to pop operand off an empty stack).
        // Perhaps this is a Javassist error? Shouldn't the inner class be going through a synthetic
        // accessor method of some kind? Resolved by assigning to a local variable and referencing
        // that. Layers on layers, oh my!

        final ValidationDecorator decorator = _decorator;

        // Since we don't know if the field has rendered yet, we need to defer writing the for
        // attribute until we know the field has rendered (and set its clientId property). That's
        // exactly what Heartbeat is for.

        Runnable command = new Runnable()
        {
            public void run()
            {
                String fieldId = field.getClientId();

                element.forceAttributes("for", fieldId);

                decorator.insideLabel(field, element);
            }
        };

        _heartbeat.defer(command);
    }

    @BeforeRenderBody
    boolean renderBody()
    {
        // Don't render the body of the component even if there is one.

        return false;
    }

    @AfterRender
    void after(MarkupWriter writer)
    {
        writer.write(_field.getLabel());

        writer.end(); // label
    }
}
