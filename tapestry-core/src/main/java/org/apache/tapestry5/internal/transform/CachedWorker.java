// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.annotations.Cached;
import org.apache.tapestry5.ioc.util.BodyBuilder;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.*;

import static java.lang.reflect.Modifier.PRIVATE;
import java.util.List;

/**
 * Caches method return values for methods annotated with {@link Cached}.
 */
public class CachedWorker implements ComponentClassTransformWorker
{
    private final BindingSource bindingSource;

    public CachedWorker(BindingSource bindingSource)
    {
        this.bindingSource = bindingSource;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<TransformMethodSignature> methods = transformation.findMethodsWithAnnotation(Cached.class);
        if (methods.isEmpty())
            return;

        for (TransformMethodSignature method : methods)
        {
            if (method.getReturnType().equals("void"))
                throw new IllegalArgumentException(TransformMessages.cachedMethodMustHaveReturnValue(method));

            if (method.getParameterTypes().length != 0)
                throw new IllegalArgumentException(TransformMessages.cachedMethodsHaveNoParameters(method));

            String propertyName = method.getMethodName();

            // add a property to store whether or not the method has been called
            String fieldName = transformation.addField(PRIVATE, method.getReturnType(), propertyName);
            String calledField = transformation.addField(PRIVATE, "boolean", fieldName + "$called");

            Cached once = transformation.getMethodAnnotation(method, Cached.class);
            String bindingField = null;
            String bindingValueField = null;
            boolean watching = once.watch().length() > 0;

            if (watching)
            {
                // add fields to store the binding and the value
                bindingField = transformation.addField(PRIVATE, Binding.class.getCanonicalName(),
                                                       fieldName + "$binding");
                bindingValueField = transformation.addField(PRIVATE, "java.lang.Object", fieldName + "$bindingValue");

                String bindingSourceField = transformation.addInjectedField(BindingSource.class,
                                                                            fieldName + "$bindingsource",
                                                                            bindingSource);

                String body = String.format("%s = %s.newBinding(\"Watch expression\", %s, \"%s\", \"%s\");",
                                            bindingField,
                                            bindingSourceField,
                                            transformation.getResourcesFieldName(),
                                            BindingConstants.PROP,
                                            once.watch());

                transformation.extendMethod(TransformConstants.CONTAINING_PAGE_DID_LOAD_SIGNATURE, body);
            }

            BodyBuilder b = new BodyBuilder();

            // on cleanup, reset the field values
            b.begin();

            if (!TransformUtils.isPrimitive(method.getReturnType()))
                b.addln("%s = null;", fieldName);
            b.addln("%s = false;", calledField);

            if (watching)
                b.addln("%s = null;", bindingValueField);

            b.end();

            // TAPESTRY-2338: Cleanup at page detach, not render cleanup.  In an Ajax request, the rendering
            // objects may reference properties of components that don't render and so won't execute the
            // PostCleanupRender phase.

            transformation.extendMethod(TransformConstants.CONTAINING_PAGE_DID_DETACH_SIGNATURE, b.toString());

            // prefix the existing method to cache the result
            b.clear();
            b.begin();

            // if it has been called and watch is set and the old value is the same as the new value then return
            // get the old value and cache it
            /* NOTE: evaluates the binding twice when checking the new value.
                * this is probably not a problem because in most cases properties
                * that are being watched are not expensive operations. plus, we
                * never guaranteed that it would be called exactly once when
                * watching.
                */
            if (watching)
            {
                b.addln("if (%s && %s == %s.get()) return %s;",
                        calledField, bindingValueField, bindingField, fieldName);
                b.addln("%s = %s.get();", bindingValueField, bindingField);
            }
            else
            {
                b.addln("if (%s) return %s;", calledField, fieldName);
            }

            b.addln("%s = true;", calledField);
            b.end();
            transformation.prefixMethod(method, b.toString());

            // cache the return value
            b.clear();
            b.begin();
            b.addln("%s = $_;", fieldName);
            b.end();
            transformation.extendExistingMethod(method, b.toString());
        }
    }
}
