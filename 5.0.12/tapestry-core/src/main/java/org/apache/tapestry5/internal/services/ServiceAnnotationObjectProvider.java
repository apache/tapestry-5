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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.annotations.Service;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ObjectProvider;

/**
 * Adds support for the {@link Service} annotation (which can be applied to fields or parameters), which is used to
 * disambiguate injection when multiple services implement the same service interface.
 */
public class ServiceAnnotationObjectProvider implements ObjectProvider
{
    public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider,
                         ObjectLocator locator)
    {
        Service annotation = annotationProvider.getAnnotation(Service.class);

        if (annotation == null) return null;

        return locator.getService(annotation.value(), objectType);
    }

}
