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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.commons.AnnotationProvider;
import org.apache.tapestry5.ioc.AnnotationAccess;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

/**
 * Standard AnnotationAccess for a specific type.
 * 
 * @since 5.3
 */
public class AnnotationAccessImpl implements AnnotationAccess
{
    private final Class type;

    public AnnotationAccessImpl(Class type)
    {
        this.type = type;
    }

    @Override
    public AnnotationProvider getClassAnnotationProvider()
    {
        return InternalUtils.toAnnotationProvider(type);
    }

    @Override
    public AnnotationProvider getMethodAnnotationProvider(String methodName, Class... parameterTypes)
    {
        return InternalUtils.toAnnotationProvider(InternalUtils.findMethod(type, methodName, parameterTypes));
    }

}
