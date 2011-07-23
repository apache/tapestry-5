// Copyright 2010, 2011 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.util.IdAllocator;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.ComponentEvent;
import org.apache.tapestry5.services.*;

/**
 * Hooks the activate event handler on the component (presumably, a page) to
 * extract query parameters, and hooks the link decoration events to extract values
 * and add them to the {@link Link}.
 *
 * @see ActivationRequestParameter
 * @since 5.2.0
 */
@SuppressWarnings("all")
public class ActivationRequestParameterWorker implements ComponentClassTransformWorker
{
    private final Request request;

    private final ComponentClassCache classCache;

    private final ValueEncoderSource valueEncoderSource;

    public ActivationRequestParameterWorker(Request request, ComponentClassCache classCache,
                                            ValueEncoderSource valueEncoderSource)
    {
        this.request = request;
        this.classCache = classCache;
        this.valueEncoderSource = valueEncoderSource;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (TransformField field : transformation.matchFieldsWithAnnotation(ActivationRequestParameter.class))
        {
            mapFieldToQueryParameter(field, transformation, model);
        }
    }

    private void mapFieldToQueryParameter(TransformField field, ClassTransformation transformation,
                                          MutableComponentModel model)
    {
        ActivationRequestParameter annotation = field.getAnnotation(ActivationRequestParameter.class);

        String parameterName = getParameterName(field, annotation);

        // Assumption: the field type is not one that's loaded by the component class loader, so it's safe
        // to convert to a hard type during class transformation.

        Class fieldType = classCache.forName(field.getType());

        ValueEncoder encoder = valueEncoderSource.getValueEncoder(fieldType);

        FieldAccess access = field.getAccess();

        setValueFromInitializeEventHandler(transformation, access, parameterName, encoder);
        decorateLinks(transformation, access, parameterName, encoder);
        preallocateName(transformation, parameterName);

        model.addEventHandler(EventConstants.ACTIVATE);
    }

    private static void preallocateName(ClassTransformation transformation, final String parameterName)
    {
        ComponentEventHandler handler = new ComponentEventHandler()
        {
            public void handleEvent(Component instance, ComponentEvent event)
            {
                IdAllocator idAllocator = event.getEventContext().get(IdAllocator.class, 0);

                idAllocator.allocateId(parameterName);
            }
        };

        transformation.addComponentEventHandler(EventConstants.PREALLOCATE_FORM_CONTROL_NAMES, 1,
                "ActivationRequestParameterWorker preallocate form control name '" + parameterName + "' event handler",
                handler);
    }

    @SuppressWarnings("all")
    private void setValueFromInitializeEventHandler(ClassTransformation transformation, final FieldAccess access,
                                                    final String parameterName, final ValueEncoder encoder)
    {
        ComponentEventHandler handler = new ComponentEventHandler()
        {
            public void handleEvent(Component instance, ComponentEvent event)
            {
                String clientValue = request.getParameter(parameterName);

                if (clientValue == null)
                    return;

                Object value = encoder.toValue(clientValue);

                access.write(instance, value);
            }
        };

        ComponentMethodAdvice advice = new ComponentMethodAdvice()
        {
            public void advise(ComponentMethodInvocation invocation)
            {
                // Handle this synthetic event FIRST, before any super-class or event handler method calls.  It's especially important that this execute before
                // any onActivate() event handler method.

                ComponentEvent event = (ComponentEvent) invocation.getParameter(0);

                if (event.matches(EventConstants.ACTIVATE, "", 0))
                {
                    String clientValue = request.getParameter(parameterName);

                    if (clientValue != null)
                    {

                        Object value = encoder.toValue(clientValue);

                        access.write(invocation.getInstance(), value);
                    }
                }

                invocation.proceed();
            }
        };

        transformation.getOrCreateMethod(TransformConstants.DISPATCH_COMPONENT_EVENT).addAdvice(advice);
    }

    @SuppressWarnings("all")
    private static void decorateLinks(ClassTransformation transformation, final FieldAccess access,
                                      final String parameterName, final ValueEncoder encoder)
    {
        ComponentEventHandler handler = new ComponentEventHandler()
        {
            public void handleEvent(Component instance, ComponentEvent event)
            {
                Object value = access.read(instance);

                if (value == null)
                    return;

                Link link = event.getEventContext().get(Link.class, 0);

                String clientValue = encoder.toClient(value);

                link.addParameter(parameterName, clientValue);
            }
        };

        transformation.addComponentEventHandler(EventConstants.DECORATE_COMPONENT_EVENT_LINK, 0,
                "ActivationRequestParameterWorker decorate component event link event handler", handler);

        transformation.addComponentEventHandler(EventConstants.DECORATE_PAGE_RENDER_LINK, 0,
                "ActivationRequestParameterWorker decorate page render link event handler", handler);
    }

    private String getParameterName(TransformField field, ActivationRequestParameter annotation)
    {
        if (annotation.value().equals(""))
            return field.getName();

        return annotation.value();
    }

}
