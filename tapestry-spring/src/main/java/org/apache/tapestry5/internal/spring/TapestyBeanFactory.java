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

package org.apache.tapestry5.internal.spring;

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.springframework.beans.BeansException;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Identifies dependencies whose field or method parameter contains the {@link org.apache.tapestry5.ioc.annotations.Inject}
 * or {@link org.apache.tapestry5.ioc.annotations.InjectService} annotations and, if so, invokes {@link
 * org.apache.tapestry5.ioc.Registry#getObject(Class, org.apache.tapestry5.ioc.AnnotationProvider)} to provide the
 * value.
 */
public class TapestyBeanFactory extends DefaultListableBeanFactory
{
    private final Registry registry;

    public TapestyBeanFactory(BeanFactory parentBeanFactory, Registry registry)
    {
        super(parentBeanFactory);

        this.registry = registry;
    }

    @Override
    public Object resolveDependency(final DependencyDescriptor descriptor, String beanName, Set autowiredBeanNames,
                                    TypeConverter typeConverter) throws BeansException
    {

        Class objectType = descriptor.getDependencyType();

        final Object[] annotations = descriptor.getAnnotations();

        if (annotations != null)
        {
            AnnotationProvider provider = new AnnotationProvider()
            {
                public <T extends Annotation> T getAnnotation(Class<T> annotationClass)
                {
                    for (Object a : annotations)
                    {
                        if (annotationClass.isInstance(a)) return annotationClass.cast(a);
                    }

                    return null;
                }
            };

            if (provider.getAnnotation(Inject.class) != null || provider.getAnnotation(InjectService.class) != null)
                return registry.getObject(objectType, provider);
        }

        return super.resolveDependency(descriptor, beanName, autowiredBeanNames, typeConverter);
    }
}