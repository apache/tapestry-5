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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.services.TypeCoercer;

/**
 * A PropertyConduit for a literal value in an expression, such as a number, or "true", "false" or "null".
 */
public class LiteralPropertyConduit extends BasePropertyConduit
{
    private final Object value;

    public LiteralPropertyConduit(Class propertyType, AnnotationProvider annotationProvider, String description,
                                  TypeCoercer typeCoercer,
                                  Object value)
    {
        super(propertyType, annotationProvider, description, typeCoercer);

        this.value = value;
    }

    public Object get(Object instance)
    {
        return value;
    }

    public void set(Object instance, Object value)
    {
        throw new RuntimeException(ServicesMessages.literalConduitNotUpdateable());
    }
}
