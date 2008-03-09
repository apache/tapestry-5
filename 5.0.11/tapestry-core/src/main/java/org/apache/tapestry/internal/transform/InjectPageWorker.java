// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.transform;

import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.internal.services.RequestPageCache;
import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.apache.tapestry.ioc.util.BodyBuilder;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassResolver;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.TransformMethodSignature;

import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Peforms transformations that allow pages to be injected into components.
 *
 * @see org.apache.tapestry.annotations.InjectPage
 */
public class InjectPageWorker implements ComponentClassTransformWorker
{
    private final RequestPageCache _requestPageCache;

    private final ComponentClassResolver _resolver;

    public InjectPageWorker(RequestPageCache requestPageCache, ComponentClassResolver resolver)
    {
        _requestPageCache = requestPageCache;
        _resolver = resolver;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        List<String> names = transformation.findFieldsWithAnnotation(InjectPage.class);

        if (names.isEmpty()) return;

        String cacheFieldName = transformation.addInjectedField(RequestPageCache.class, "_requestPageCache",
                                                                _requestPageCache);

        for (String name : names)
            addInjectedPage(transformation, name, cacheFieldName);

    }

    private void addInjectedPage(ClassTransformation transformation, String fieldName, String cacheFieldName)
    {
        InjectPage annotation = transformation.getFieldAnnotation(fieldName, InjectPage.class);

        String pageName = annotation.value();

        String fieldType = transformation.getFieldType(fieldName);
        String methodName = transformation.newMemberName("read_inject_page", fieldName);

        String injectedPageName = InternalUtils.isBlank(pageName) ? _resolver
                .resolvePageClassNameToPageName(fieldType) : pageName;

        TransformMethodSignature sig = new TransformMethodSignature(Modifier.PRIVATE, fieldType, methodName, null,
                                                                    null);

        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        builder.addln("%s page = %s.get(\"%s\");", Page.class.getName(), cacheFieldName, injectedPageName);

        builder.addln("return (%s) page.getRootElement().getComponent();", fieldType);

        builder.end();

        transformation.addMethod(sig, builder.toString());
        transformation.replaceReadAccess(fieldName, methodName);
        transformation.makeReadOnly(fieldName);
        transformation.removeField(fieldName);

        transformation.claimField(fieldName, annotation);
    }
}
