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
import java.util.List;
import java.util.Set;

import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProducerFactory;

public class NoopBeanManager implements BeanManager
{

    @Override
    public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object getInjectableReference(InjectionPoint ij, CreationalContext<?> ctx)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> CreationalContext<T> createCreationalContext(Contextual<T> contextual)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Bean<?>> getBeans(String name)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Bean<?> getPassivationCapableBean(String id)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void validate(InjectionPoint injectionPoint)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void fireEvent(Object event, Annotation... qualifiers)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T event, Annotation... qualifiers)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Decorator<?>> resolveDecorators(Set<Type> types, Annotation... qualifiers)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isScope(Class<? extends Annotation> annotationType)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNormalScope(Class<? extends Annotation> annotationType)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPassivatingScope(Class<? extends Annotation> annotationType)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isQualifier(Class<? extends Annotation> annotationType)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInterceptorBinding(Class<? extends Annotation> annotationType)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isStereotype(Class<? extends Annotation> annotationType)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set<Annotation> getInterceptorBindingDefinition(Class<? extends Annotation> bindingType)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<Annotation> getStereotypeDefinition(Class<? extends Annotation> stereotype)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean areQualifiersEquivalent(Annotation qualifier1, Annotation qualifier2)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean areInterceptorBindingsEquivalent(Annotation interceptorBinding1, Annotation interceptorBinding2)
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int getQualifierHashCode(Annotation qualifier)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getInterceptorBindingHashCode(Annotation interceptorBinding)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Context getContext(Class<? extends Annotation> scopeType)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ELResolver getELResolver()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ExpressionFactory wrapExpressionFactory(ExpressionFactory expressionFactory)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> AnnotatedType<T> createAnnotatedType(Class<T> type)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> InjectionTarget<T> createInjectionTarget(AnnotatedType<T> type)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> InjectionTargetFactory<T> getInjectionTargetFactory(AnnotatedType<T> annotatedType)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X> ProducerFactory<X> getProducerFactory(AnnotatedField<? super X> field, Bean<X> declaringBean)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <X> ProducerFactory<X> getProducerFactory(AnnotatedMethod<? super X> method, Bean<X> declaringBean)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> BeanAttributes<T> createBeanAttributes(AnnotatedType<T> type)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BeanAttributes<?> createBeanAttributes(AnnotatedMember<?> type)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> Bean<T> createBean(BeanAttributes<T> attributes, Class<T> beanClass, InjectionTargetFactory<T> injectionTargetFactory)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T, X> Bean<T> createBean(BeanAttributes<T> attributes, Class<X> beanClass, ProducerFactory<X> producerFactory)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InjectionPoint createInjectionPoint(AnnotatedField<?> field)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InjectionPoint createInjectionPoint(AnnotatedParameter<?> parameter)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T extends Extension> T getExtension(Class<T> extensionClass)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
