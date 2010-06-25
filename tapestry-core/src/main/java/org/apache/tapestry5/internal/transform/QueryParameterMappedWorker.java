// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.QueryParameterMapped;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.ComponentEvent;
import org.apache.tapestry5.services.*;

/**
 * Hooks the activate event handler on the component (presumably, a page) to
 * extract query parameters, and hooks the link decoration events to extract values
 * and add them to the {@link Link}.
 * 
 * @see QueryParameterMapped
 * @since 5.2.0
 */
public class QueryParameterMappedWorker implements ComponentClassTransformWorker
{
    private final Request request;

    private final ComponentClassCache classCache;

    private final ValueEncoderSource valueEncoderSource;

    interface EventHandler
    {
        void invoke(Component component, ComponentEvent event);
    }

    public QueryParameterMappedWorker(Request request, ComponentClassCache classCache,
            ValueEncoderSource valueEncoderSource)
    {
        this.request = request;
        this.classCache = classCache;
        this.valueEncoderSource = valueEncoderSource;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (TransformField field : transformation.matchFieldsWithAnnotation(QueryParameterMapped.class))
        {
            mapFieldToQueryParameter(field, transformation, model);
        }
    }

    @SuppressWarnings("unchecked")
    private void mapFieldToQueryParameter(TransformField field, ClassTransformation transformation,
            MutableComponentModel model)
    {
        QueryParameterMapped annotation = field.getAnnotation(QueryParameterMapped.class);

        String parameterName = getParameterName(field, annotation);

        TransformMethod dispatchMethod = transformation.getOrCreateMethod(TransformConstants.DISPATCH_COMPONENT_EVENT);

        // Assumption: the field type is not one that's loaded by the component class loader, so it's safe
        // to convert to a hard type during class transformation.

        Class fieldType = classCache.forName(field.getType());

        ValueEncoder encoder = valueEncoderSource.getValueEncoder(fieldType);

        FieldAccess access = field.getAccess();

        setValueFromInitializeEventHandler(access, parameterName, encoder, dispatchMethod, model);
        decorateLinks(access, parameterName, encoder, dispatchMethod, model);
    }

    @SuppressWarnings("unchecked")
    private void setValueFromInitializeEventHandler(final FieldAccess access, final String parameterName,
            final ValueEncoder encoder, TransformMethod dispatchMethod, MutableComponentModel model)
    {
        EventHandler handler = new EventHandler()
        {
            public void invoke(Component component, ComponentEvent event)
            {
                String clientValue = request.getParameter(parameterName);

                if (clientValue == null)
                    return;

                Object value = encoder.toValue(clientValue);

                access.write(component, value);
            }
        };

        add(dispatchMethod, model, EventConstants.ACTIVATE, handler);
    }

    @SuppressWarnings("unchecked")
    private void decorateLinks(final FieldAccess access, final String parameterName, final ValueEncoder encoder,
            TransformMethod dispatchMethod, MutableComponentModel model)
    {
        EventHandler handler = new EventHandler()
        {
            public void invoke(Component component, ComponentEvent event)
            {
                Object value = access.read(component);

                if (value == null)
                    return;

                Link link = event.getEventContext().get(Link.class, 0);

                String clientValue = encoder.toClient(value);

                link.addParameter(parameterName, clientValue);
            }
        };

        add(dispatchMethod, model, EventConstants.DECORATE_COMPONENT_EVENT_LINK, handler);
        add(dispatchMethod, model, EventConstants.DECORATE_PAGE_RENDER_LINK, handler);
    }

    private void add(TransformMethod dispatchMethod, MutableComponentModel model, final String eventType,
            final EventHandler handler)
    {
        dispatchMethod.addAdvice(new ComponentMethodAdvice()
        {
            public void advise(ComponentMethodInvocation invocation)
            {
                ComponentEvent event = (ComponentEvent) invocation.getParameter(0);

                if (event.matches(eventType, "", 0))
                {
                    handler.invoke(invocation.getInstance(), event);
                }
                
                invocation.proceed();
            }
        });

        model.addEventHandler(eventType);
    }

    private String getParameterName(TransformField field, QueryParameterMapped annotation)
    {
        if (annotation.value().equals(""))
            return field.getName();

        return annotation.value();
    }

}
