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
import org.apache.tapestry5.runtime.ComponentEvent;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.ComponentMethodAdvice;
import org.apache.tapestry5.services.ComponentMethodInvocation;
import org.apache.tapestry5.services.FieldAccess;
import org.apache.tapestry5.services.TransformConstants;
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

    private void transformField(ClassTransformation transformation, MutableComponentModel model,
            final TransformField field)
    {
        final PageActivationContext annotation = field.getAnnotation(PageActivationContext.class);

        ComponentMethodAdvice advice = createAdvice(field, annotation);

        transformation.getMethod(TransformConstants.DISPATCH_COMPONENT_EVENT).addAdvice(advice);

        if (annotation.activate())
            model.addEventHandler(EventConstants.ACTIVATE);

        if (annotation.passivate())
            model.addEventHandler(EventConstants.PASSIVATE);

        // We don't claim the field, and other workers may even replace it with a FieldValueConduit.
    }

    private ComponentMethodAdvice createAdvice(TransformField field, final PageActivationContext annotation)
    {
        final String fieldType = field.getType();
        final FieldAccess access = field.getAccess();

        return new ComponentMethodAdvice()
        {
            public void advise(ComponentMethodInvocation invocation)
            {
                invocation.proceed();

                ComponentEvent event = (ComponentEvent) invocation.getParameter(0);

                if (event.isAborted())
                    return;

                handleActivateEvent(event, invocation);

                handlePassivateEvent(event, invocation);
            }

            private void handleActivateEvent(ComponentEvent event, ComponentMethodInvocation invocation)
            {
                if (annotation.activate() && event.matches(EventConstants.ACTIVATE, "", 1))
                {
                    event.setMethodDescription(access.toString());

                    Object value = event.coerceContext(0, fieldType);

                    access.write(invocation.getInstance(), value);

                    invocation.overrideResult(true);
                }
            }

            private void handlePassivateEvent(ComponentEvent event, ComponentMethodInvocation invocation)
            {

                if (annotation.passivate() && event.matches(EventConstants.PASSIVATE, "", 0))
                {
                    event.setMethodDescription(access.toString());

                    Object value = access.read(invocation.getInstance());

                    event.storeResult(value);

                    invocation.overrideResult(true);
                }
            }
        };
    }
}
