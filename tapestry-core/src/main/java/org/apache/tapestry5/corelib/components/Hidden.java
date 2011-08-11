// Copyright 2009, 2010, 2011 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.SupportsInformalParameters;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ComponentDefaultProvider;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/**
 * Used to record a page property as a value into the form. The value is
 * {@linkplain org.apache.tapestry5.ValueEncoder#toClient(Object) encoded} when rendered, then decoded when the form is
 * submitted,
 * and the value parameter updated.
 *
 * @tapestrydoc
 * @since 5.1.0.2
 */
@SupportsInformalParameters
public class Hidden implements ClientElement
{
    /**
     * The value to read (when rendering) or update (when the form is submitted).
     */
    @Parameter(required = true, autoconnect = true, principal = true)
    private Object value;

    /**
     * Defines how nulls on the server side, or sent from the client side, are treated. The selected strategy may
     * replace the nulls with some other value. The default strategy leaves nulls alone.  Another built-in strategy,
     * zero, replaces nulls with the value 0.
     */
    @Parameter(defaultPrefix = BindingConstants.NULLFIELDSTRATEGY, value = "default")
    private NullFieldStrategy nulls;

    /**
     * Value encoder for the value, usually determined automatically from the type of the property bound to the value
     * parameter.
     */
    @Parameter(required = true)
    private ValueEncoder encoder;

    private String clientId;

    private String controlName;

    private Element hiddenInputElement;

    @Environmental(false)
    private FormSupport formSupport;

    @Environmental
    private JavaScriptSupport jsSupport;

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
        {
            throw new RuntimeException("The Hidden component must be enclosed by a Form component.");
        }

        controlName = formSupport.allocateControlName(resources.getId());

        clientId = null;

        formSupport.store(this, new ProcessSubmission(controlName));

        Object toEncode = value == null ? nulls.replaceToClient() : value;

        String encoded = toEncode == null ? "" : encoder.toClient(toEncode);

        hiddenInputElement = writer.element("input", "type", "hidden", "name", controlName, "value", encoded);

        resources.renderInformalParameters(writer);

        writer.end();

        return false;
    }

    private void processSubmission(String controlName)
    {
        String encoded = request.getParameter(controlName);

        String toDecode = InternalUtils.isBlank(encoded) ? nulls.replaceFromClient() : encoded;

        Object decoded = toDecode == null ? null : encoder.toValue(toDecode);

        value = decoded;
    }

    public String getClientId()
    {
        if (clientId == null)
        {
            clientId = jsSupport.allocateClientId(resources);
            hiddenInputElement.forceAttributes("id", clientId);
        }

        return clientId;
    }

    public String getControlName()
    {
        return controlName;
    }
}
