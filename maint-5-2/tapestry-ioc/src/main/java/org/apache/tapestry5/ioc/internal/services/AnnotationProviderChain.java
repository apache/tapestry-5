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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.AnnotationProvider;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Chain of command for {@link org.apache.tapestry5.ioc.AnnotationProvider}.
 */
public class AnnotationProviderChain implements AnnotationProvider
{
    private final AnnotationProvider[] providers;

    public AnnotationProviderChain(AnnotationProvider[] providers)
    {
        this.providers = providers;
    }

    /**
     * Creates an AnnotationProvider from the list of providers.  Returns either an {@link AnnotationProviderChain} or
     * the sole element in the list.
     */
    public static AnnotationProvider create(List<AnnotationProvider> providers)
    {
        int size = providers.size();

        if (size == 1) return providers.get(0);

        return new AnnotationProviderChain(providers.toArray(new AnnotationProvider[providers.size()]));
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
    {
        for (AnnotationProvider p : providers)
        {
            T result = p.getAnnotation(annotationClass);

            if (result != null) return result;
        }

        return null;
    }
}
