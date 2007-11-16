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

package org.apache.tapestry.corelib.internal;

import org.apache.tapestry.ComponentEventHandler;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.Translator;
import org.apache.tapestry.ValidationException;
import org.apache.tapestry.internal.util.Holder;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.runtime.ComponentEventException;

/**
 * A wrapoer around a component and a {@link org.apache.tapestry.Translator} that allows the component
 * priority in terms of the behaviors of the translator.  Events fired upon the component
 * may bypass corresponding methods of the translator, allowing code in the component to
 * customize translator-like behavior.
 */
@SuppressWarnings("unchecked")
public class ComponentTranslatorWrapper implements Translator
{
    static final String PARSE_CLIENT_EVENT = "parseClient";
    static final String TO_CLIENT_EVENT = "toClient";

    private final ComponentResources _resources;
    private final Translator _delegate;

    /**
     * @param resources resources of component, used to trigger events
     * @param delegate  translator used when the component does not provide an event handler
     */
    public ComponentTranslatorWrapper(ComponentResources resources, Translator delegate)
    {
        _delegate = delegate;
        _resources = resources;
    }


    public Object parseClient(String clientValue, Messages messages) throws ValidationException
    {
        final Holder<Object> resultHolder = Holder.create();

        ComponentEventHandler handler = new ComponentEventHandler()
        {
            public boolean handleResult(Object result, Component component, String methodDescription)
            {
                resultHolder.put(result);
                return true;
            }
        };

        try
        {
            _resources.triggerEvent(PARSE_CLIENT_EVENT, new Object[]{clientValue}, handler);
        }
        catch (ComponentEventException ex)
        {
            ValidationException ve = ex.get(ValidationException.class);

            if (ve != null) throw ve;

            throw ex;
        }

        if (resultHolder.hasValue()) return resultHolder.get();

        // Otherwise, let the normal translator do the job.

        return _delegate.parseClient(clientValue, messages);
    }

    public String toClient(Object value)
    {
        final Holder<String> resultHolder = Holder.create();

        ComponentEventHandler handler = new ComponentEventHandler()
        {
            public boolean handleResult(Object result, Component component, String methodDescription)
            {
                // What's nice is that the ComponentEventException will automatically identify
                // the method description.

                if (!(result instanceof String))
                    throw new RuntimeException(InternalMessages.toClientShouldReturnString());

                resultHolder.put((String) result);

                return true;
            }
        };

        _resources.triggerEvent(TO_CLIENT_EVENT, new Object[]{value}, handler);

        if (resultHolder.hasValue()) return resultHolder.get();

        return _delegate.toClient(value);
    }


}
