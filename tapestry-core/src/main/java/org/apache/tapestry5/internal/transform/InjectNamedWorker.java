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

import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Predicate;
import org.apache.tapestry5.internal.services.ComponentClassCache;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Processes the combination of {@link javax.inject.Inject} and {@link javax.inject.Named} annotations.
 *
 * @since 5.3
 */
public class InjectNamedWorker implements ComponentClassTransformWorker2
{
    private final ObjectLocator locator;

    private final ComponentClassCache cache;

    private final Predicate<PlasticField> MATCHER = new Predicate<PlasticField>()
    {
        public boolean accept(PlasticField field)
        {
            return field.hasAnnotation(Inject.class) && field.hasAnnotation(Named.class);
        }
    };

    public InjectNamedWorker(ObjectLocator locator, ComponentClassCache cache)
    {
        this.locator = locator;
        this.cache = cache;
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        Flow<PlasticField> fields = F.flow(plasticClass.getAllFields()).filter(MATCHER);

        for (PlasticField field : fields)
        {
            Named annotation = field.getAnnotation(Named.class);

            field.claim(annotation);

            Class fieldType = cache.forName(field.getTypeName());

            Object service = locator.getService(annotation.value(), fieldType);

            field.inject(service);
        }
    }
}
