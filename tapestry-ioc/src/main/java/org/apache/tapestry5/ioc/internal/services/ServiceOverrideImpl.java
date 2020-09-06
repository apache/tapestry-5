// Copyright 2009, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import java.util.Map;

import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.commons.ObjectProvider;
import org.apache.tapestry5.ioc.annotations.PreventServiceDecoration;
import org.apache.tapestry5.ioc.services.ServiceOverride;

@PreventServiceDecoration
public class ServiceOverrideImpl implements ServiceOverride
{
    private final Map<Class, Object> configuration;

    public ServiceOverrideImpl(Map<Class, Object> configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public ObjectProvider getServiceOverrideProvider()
    {
        return new ObjectProvider()
        {
            @Override
            public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider, ObjectLocator locator)
            {
                return objectType.cast(configuration.get(objectType));
            }
        };
    }
}
