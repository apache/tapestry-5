// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.util.BodyBuilder;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.*;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Peforms transformations that allow pages to be injected into components.
 *
 * @see org.apache.tapestry5.annotations.InjectPage
 */
public class InjectPageWorker implements ComponentClassTransformWorker
{
    private final ComponentSource componentSource;

    private final ComponentClassResolver resolver;

    public InjectPageWorker(ComponentSource componentSource, ComponentClassResolver resolver)
    {
        this.componentSource = componentSource;
        this.resolver = resolver;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> names = transformation.findFieldsWithAnnotation(InjectPage.class);

        if (names.isEmpty()) return;

        String componentSource = transformation.addInjectedField(ComponentSource.class, "componentSource",
                                                                 this.componentSource);


        for (String name : names)
            addInjectedPage(transformation, name, componentSource);

    }

    private void addInjectedPage(ClassTransformation transformation, String fieldName, String componentSource)
    {
        InjectPage annotation = transformation.getFieldAnnotation(fieldName, InjectPage.class);
        
        transformation.claimField(fieldName, annotation);

        String pageName = annotation.value();

        String fieldType = transformation.getFieldType(fieldName);
        String methodName = transformation.newMemberName("read_inject_page", fieldName);

        String injectedPageName = InternalUtils.isBlank(pageName) ? resolver
                .resolvePageClassNameToPageName(fieldType) : pageName;

        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PRIVATE, fieldType, methodName, null,
                                                                    null);

        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        builder.addln("return (%s) %s.getPage(\"%s\");", fieldType, componentSource, injectedPageName);

        builder.end();

        transformation.addMethod(sig, builder.toString());
        transformation.replaceReadAccess(fieldName, methodName);
        transformation.makeReadOnly(fieldName);
        transformation.removeField(fieldName);
    }
}
