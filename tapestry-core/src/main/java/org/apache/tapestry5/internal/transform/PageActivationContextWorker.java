// Copyright 2008, 2009, 2010, 2011 The Apache Software Foundation
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
import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.FieldHandle;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.ComponentEvent;
import org.apache.tapestry5.services.ComponentEventHandler;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

import java.util.List;

/**
 * Provides the page activation context handlers.
 *
 * @see org.apache.tapestry5.annotations.PageActivationContext
 */
public class PageActivationContextWorker implements ComponentClassTransformWorker2
{
    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        List<PlasticField> fields = plasticClass.getFieldsWithAnnotation(PageActivationContext.class);

        switch (fields.size())
        {
            case 0:
                break;

            case 1:

                transformField(support, fields.get(0));

                break;

            default:

                throw new RuntimeException(TransformMessages.illegalNumberOfPageActivationContextHandlers2(fields));
        }
    }

    private void transformField(TransformationSupport support, PlasticField field)
    {
        PageActivationContext annotation = field.getAnnotation(PageActivationContext.class);

        FieldHandle handle = field.getHandle();

        if (annotation.activate())
        {
            support.addEventHandler(EventConstants.ACTIVATE, 1,
                    "PageActivationContextWorker activate event handler",
                    createActivationHandler(field.getTypeName(), handle));
        }

        if (annotation.passivate())
        {
            support.addEventHandler(EventConstants.PASSIVATE, 0,
                    "PageActivationContextWorker passivate event handler", createPassivateHandler(handle));
        }

        // We don't claim the field, and other workers may even replace it with a FieldConduit.

    }

    private static ComponentEventHandler createActivationHandler(final String fieldType, final FieldHandle handle)
    {
        return new ComponentEventHandler()
        {
            public void handleEvent(Component instance, ComponentEvent event)
            {
                Object value = event.coerceContext(0, fieldType);

                handle.set(instance, value);
            }
        };
    }

    private static ComponentEventHandler createPassivateHandler(final FieldHandle handle)
    {
        return new ComponentEventHandler()
        {
            public void handleEvent(Component instance, ComponentEvent event)
            {
                Object value = handle.get(instance);

                event.storeResult(value);
            }
        };
    }
}
