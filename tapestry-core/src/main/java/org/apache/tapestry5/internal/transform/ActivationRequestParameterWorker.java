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
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.commons.internal.util.TapestryException;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.util.IdAllocator;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.FieldHandle;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.ComponentEvent;
import org.apache.tapestry5.services.ComponentEventHandler;
import org.apache.tapestry5.services.URLEncoder;
import org.apache.tapestry5.services.ValueEncoderSource;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * Hooks the activate event handler on the component (presumably, a page) to
 * extract query parameters, and hooks the link decoration events to extract values
 * and add them to the {@link Link}.
 *
 * @see ActivationRequestParameter
 * @since 5.2.0
 */
@SuppressWarnings("all")
public class ActivationRequestParameterWorker implements ComponentClassTransformWorker2
{
    private final Request request;

    private final ComponentClassCache classCache;

    private final ValueEncoderSource valueEncoderSource;

    private final URLEncoder urlEncoder;

    public ActivationRequestParameterWorker(Request request, ComponentClassCache classCache,
                                            ValueEncoderSource valueEncoderSource, URLEncoder urlEncoder)
    {
        this.request = request;
        this.classCache = classCache;
        this.valueEncoderSource = valueEncoderSource;
        this.urlEncoder = urlEncoder;
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        for (PlasticField field : plasticClass.getFieldsWithAnnotation(ActivationRequestParameter.class))
        {
            mapFieldToQueryParameter(field, support);
        }
    }

    private void mapFieldToQueryParameter(PlasticField field, TransformationSupport support)
    {
        ActivationRequestParameter annotation = field.getAnnotation(ActivationRequestParameter.class);

        String parameterName = getParameterName(field, annotation);

        // Assumption: the field type is not one that's loaded by the component class loader, so it's safe
        // to convert to a hard type during class transformation.

        Class fieldType = classCache.forName(field.getTypeName());

        ValueEncoder encoder = valueEncoderSource.getValueEncoder(fieldType);

        FieldHandle handle = field.getHandle();

        String fieldName = String.format("%s.%s", field.getPlasticClass().getClassName(), field.getName());

        setValueFromInitializeEventHandler(support, fieldName, annotation.required(), handle, parameterName, encoder, urlEncoder);

        decorateLinks(support, fieldName, handle, parameterName, encoder, urlEncoder);

        preallocateName(support, parameterName);
    }


    private static void preallocateName(TransformationSupport support, final String parameterName)
    {
        ComponentEventHandler handler = new ComponentEventHandler()
        {
            public void handleEvent(Component instance, ComponentEvent event)
            {
                IdAllocator idAllocator = event.getEventContext().get(IdAllocator.class, 0);

                idAllocator.allocateId(parameterName);
            }
        };

        support.addEventHandler(EventConstants.PREALLOCATE_FORM_CONTROL_NAMES, 1,
                "ActivationRequestParameterWorker preallocate form control name '" + parameterName + "' event handler",
                handler);
    }

    @SuppressWarnings("all")
    private void setValueFromInitializeEventHandler(final TransformationSupport support, final String fieldName, final boolean required, final FieldHandle handle,
                                                    final String parameterName, final ValueEncoder encoder, final URLEncoder urlEncoder)
    {
        ComponentEventHandler handler = new ComponentEventHandler()
        {
            public void handleEvent(Component instance, ComponentEvent event)
            {
                String clientValue = request.getParameter(parameterName);

                if (clientValue == null)
                {
                    if (required)
                    {
                        throw new TapestryException(String.format("Activation request parameter field %s is marked as required, but query parameter '%s' is null.",
                                fieldName,
                                parameterName), null);
                    }

                    return;
                }

                // TAP5-1768: unescape encoded value
                clientValue = urlEncoder.decode(clientValue);

                Object value = encoder.toValue(clientValue);

                handle.set(instance, value);
            }
        };

        support.addEventHandler(EventConstants.ACTIVATE, 0,
                String.format("Restoring field %s from query parameter '%s'", fieldName, parameterName), handler);

    }

    @SuppressWarnings("all")
    private static void decorateLinks(TransformationSupport support, String fieldName, final FieldHandle handle,
                                      final String parameterName, final ValueEncoder encoder, final URLEncoder urlEncoder)
    {
        ComponentEventHandler handler = new ComponentEventHandler()
        {
            public void handleEvent(Component instance, ComponentEvent event)
            {
                Object value = handle.get(instance);

                if (value == null)
                {
                    return;
                }

                Link link = event.getEventContext().get(Link.class, 0);

                String clientValue = encoder.toClient(value);

                // TAP5-1768: escape special characters
                clientValue = urlEncoder.encode(clientValue);

                link.addParameter(parameterName, clientValue);
            }
        };

        support.addEventHandler(EventConstants.DECORATE_COMPONENT_EVENT_LINK, 0,
                String.format("ActivationRequestParameterWorker decorate component event link event handler for field %s as query parameter '%s'", fieldName, parameterName), handler);

        support.addEventHandler(EventConstants.DECORATE_PAGE_RENDER_LINK, 0, String.format(
                "ActivationRequestParameterWorker decorate page render link event handler for field %s as query parameter '%s'", fieldName, parameterName), handler);
    }

    private String getParameterName(PlasticField field, ActivationRequestParameter annotation)
    {
        if (annotation.value().equals(""))
            return field.getName();

        return annotation.value();
    }

}
