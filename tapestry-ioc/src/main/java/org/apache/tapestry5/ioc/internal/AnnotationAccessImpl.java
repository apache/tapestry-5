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

import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.ioc.AnnotationAccess;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.internal.services.AnnotationProviderChain;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

import java.lang.reflect.Method;

/**
 * Standard AnnotationAccess for an array of classes
 * 
 * @since 5.3
 */
public class AnnotationAccessImpl implements AnnotationAccess
{
    private final Class[] classes;

    public AnnotationAccessImpl(Class ...types)
    {
        this.classes = types;
    }

    @Override
    public AnnotationProvider getClassAnnotationProvider()
    {
        return AnnotationProviderChain.create(F.flow(classes).removeNulls().map(InternalUtils.CLASS_TO_AP_MAPPER).toList());
    }

    @Override
    public AnnotationProvider getMethodAnnotationProvider(String methodName, Class... parameterTypes) {
        Flow<Class> searchClasses = F.flow(classes).removeNulls();
        return AnnotationProviderChain.create(searchClasses.map(new Mapper<Class, Method>() {
            @Override
            public Method map(Class element) {
                return InternalUtils.findMethod(element, methodName, parameterTypes);
            }
        }).map(InternalUtils.METHOD_TO_AP_MAPPER).toList());
    }

}
