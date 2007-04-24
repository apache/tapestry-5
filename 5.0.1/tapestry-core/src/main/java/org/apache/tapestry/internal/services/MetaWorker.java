// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.internal.KeyValue;
import org.apache.tapestry.internal.TapestryUtils;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;

/**
 * Checks for any meta-data in the {@link ComponentClass} annotation, and adds it to the model.
 */
public class MetaWorker implements ComponentClassTransformWorker
{

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        ComponentClass annotation = transformation.getAnnotation(ComponentClass.class);

        if (annotation == null)
            return;

        for (String meta : annotation.meta())
        {
            KeyValue kv = TapestryUtils.parseKeyValue(meta);

            model.setMeta(kv.getKey(), kv.getValue());
        }
    }

}
