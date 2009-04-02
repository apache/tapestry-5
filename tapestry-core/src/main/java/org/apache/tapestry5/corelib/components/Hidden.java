// Copyright 2009 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Request;

/**
 * Used to record a page property as a value into the form. The value is {@linkplain
 * org.apache.tapestry5.ValueEncoder#toClient(Object) encoded} when rendered, then decoded when the form is submitted,
 * and the value parameter updated.
 *
 * @since 5.1.0.2
 */
public class Hidden
{
    /**
     * The value to read (when rendering) or update (when the form is submitted).
     */
    @Parameter(required = true, autoconnect = true, principal = true)
    private Object value;

    /**
     * Value encoder for the value, usually determined automatically from the type of the property bound to the value
     * parameter.
     */
    @Parameter(required = true)
    private ValueEncoder encoder;

    private String controlName;

    @Environmental(false)
    private FormSupport formSupport;

    @Environmental
    private RenderSupport renderSupport;

    @Inject
    private ComponentResources resources;

    @Inject
    private ComponentDefaultProvider defaultProvider;

    @Inject
    private Request request;

    ValueEncoder defaultEncoder()
    {
        return defaultProvider.defaultValueEncoder("value", resources);
    }

    static class ProcessSubmission implements ComponentAction<Hidden>
    {
        private final String controlName;

        public ProcessSubmission(String controlName)
        {
            this.controlName = controlName;
        }

        public void execute(Hidden component)
        {
            component.processSubmission(controlName);
        }
    }

    boolean beginRender(MarkupWriter writer)
    {
        if (formSupport == null)
            throw new RuntimeException("The Hidden component must be enclosed by a Form component.");

        controlName = formSupport.allocateControlName(resources.getId());

        formSupport.store(this, new ProcessSubmission(controlName));

        String encoded = encoder.toClient(value);

        writer.element("input",
                       "type", "hidden",
                       "name", controlName,
                       "value", encoded);
        writer.end();

        return false;
    }


    private void processSubmission(String controlName)
    {
        String encoded = request.getParameter(controlName);

        Object decoded = encoder.toValue(encoded);

        value = decoded;
    }

    public String getControlName()
    {
        return controlName;
    }
}
