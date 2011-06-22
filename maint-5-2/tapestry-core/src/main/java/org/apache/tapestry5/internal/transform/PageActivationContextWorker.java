// Copyright 2008, 2009, 2010 The Apache Software Foundation
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

import java.util.List;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.runtime.ComponentEvent;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentEventHandler;
import org.apache.tapestry5.services.FieldAccess;
import org.apache.tapestry5.services.TransformField;

/**
 * Provides the page activation context handlers. This worker must be scheduled before
 * {@link org.apache.tapestry5.internal.transform.OnEventWorker} in order for the added event handler methods to be
 * properly picked up and processed.
 * 
 * @see org.apache.tapestry5.annotations.PageActivationContext
 */
public class PageActivationContextWorker implements ComponentClassTransformWorker
{
    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<TransformField> fields = transformation.matchFieldsWithAnnotation(PageActivationContext.class);

        // In the future we may add rules for ordering the fields (new attribute on annotation?)

        if (fields.size() > 1)
            throw new RuntimeException(TransformMessages.illegalNumberOfPageActivationContextHandlers(fields));

        // So there's 0 or 1 of these

        for (TransformField field : fields)
        {
            transformField(transformation, model, field);
        }
    }

    private void transformField(ClassTransformation transformation, MutableComponentModel model, TransformField field)
    {
        PageActivationContext annotation = field.getAnnotation(PageActivationContext.class);

        FieldAccess access = field.getAccess();

        if (annotation.activate())
        {
            transformation.addComponentEventHandler(EventConstants.ACTIVATE, 1,
                    "PageActivationContextWorker activate event handler",
                    createActivationHandler(field.getType(), access));
        }

        if (annotation.passivate())
        {
            transformation.addComponentEventHandler(EventConstants.PASSIVATE, 0,
                    "PageActivationContextWorker passivate event handler", createPassivateHandler(access));
        }

        // We don't claim the field, and other workers may even replace it with a FieldValueConduit.
    }

    private static ComponentEventHandler createActivationHandler(final String fieldType, final FieldAccess access)
    {
        return new ComponentEventHandler()
        {
            public void handleEvent(Component instance, ComponentEvent event)
            {
                Object value = event.coerceContext(0, fieldType);

                access.write(instance, value);
            }
        };
    }

    private static ComponentEventHandler createPassivateHandler(final FieldAccess access)
    {
        return new ComponentEventHandler()
        {
            public void handleEvent(Component instance, ComponentEvent event)
            {
                Object value = access.read(instance);

                event.storeResult(value);
            }
        };
    }
}
