// Copyright 2011 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.services.Coercion;
import org.apache.tapestry5.ioc.services.CoercionTuple;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

/**
 * A {@link org.apache.tapestry5.ioc.services.Coercion} for converting the
 * deprecated ComponentClassTransformWorker to the new ComponentClassTransformWorker2.
 */
@SuppressWarnings("deprecation")
public class CCTWToCCTW2Coercion implements Coercion<ComponentClassTransformWorker, ComponentClassTransformWorker2>
{
    public ComponentClassTransformWorker2 coerce(final ComponentClassTransformWorker oldStyleWorker)
    {
        return new ComponentClassTransformWorker2()
        {
            public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
            {
                ClassTransformation ct = new BridgeClassTransformation(plasticClass, support, model);

                oldStyleWorker.transform(ct, model);
            }
        };
    }

    public static final CoercionTuple<ComponentClassTransformWorker, ComponentClassTransformWorker2> TUPLE = new CoercionTuple<ComponentClassTransformWorker, ComponentClassTransformWorker2>(
            ComponentClassTransformWorker.class, ComponentClassTransformWorker2.class, new CCTWToCCTW2Coercion());
}
