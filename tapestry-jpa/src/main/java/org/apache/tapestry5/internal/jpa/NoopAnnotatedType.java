/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tapestry5.internal.jpa;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

public class NoopAnnotatedType<X> implements AnnotatedType<X>
{

    @Override
    public Type getBaseType()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Type> getTypeClosure()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationType)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Annotation> getAnnotations()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Class<X> getJavaClass()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<AnnotatedConstructor<X>> getConstructors()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<AnnotatedMethod<? super X>> getMethods()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<AnnotatedField<? super X>> getFields()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
