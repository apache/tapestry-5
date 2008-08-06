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

package org.apache.tapestry5.integration.app2.services;

import org.apache.tapestry5.integration.app2.FortyTwo;
import org.apache.tapestry5.integration.app2.PlusOne;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.ComponentClassTransformWorker;
import org.apache.tapestry5.services.TransformMethodSignature;
import org.apache.tapestry5.SymbolConstants;

public class LocaleAppModule
{
    public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en,fr,de");
    }

    public static void contributeComponentClassTransformWorker(
            OrderedConfiguration<ComponentClassTransformWorker> configuration)
    {
        configuration.add("FortyTwo", new FortyTwoWorker());
        configuration.add("PlusOne", new PlusOneWorker());
    }

    private static final class FortyTwoWorker implements ComponentClassTransformWorker
    {

        public void transform(ClassTransformation transformation, MutableComponentModel model)
        {
            for (TransformMethodSignature sig : transformation.findMethodsWithAnnotation(FortyTwo.class))
            {
                transformation.prefixMethod(sig, "return 42;");
            }
        }

    }

    private static final class PlusOneWorker implements ComponentClassTransformWorker
    {
        public void transform(ClassTransformation transformation, MutableComponentModel model)
        {
            for (TransformMethodSignature method : transformation.findMethodsWithAnnotation(PlusOne.class))
            {
                transformation.extendExistingMethod(method, "return $_ + 1;");
            }
        }
    }
}
