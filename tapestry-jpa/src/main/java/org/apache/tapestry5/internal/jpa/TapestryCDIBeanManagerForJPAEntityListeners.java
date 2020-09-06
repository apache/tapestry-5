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

import java.lang.reflect.Method;

import javax.annotation.PreDestroy;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionTarget;

import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TapestryCDIBeanManagerForJPAEntityListeners extends NoopBeanManager
{
    private static final Logger logger = LoggerFactory
            .getLogger(TapestryCDIBeanManagerForJPAEntityListeners.class);

    @Inject
    private ObjectLocator objectLocator;

    @Override
    public <T> AnnotatedType<T> createAnnotatedType(final Class<T> type)
    {
        return new NoopAnnotatedType<T>()
        {
            @Override
            public Class<T> getJavaClass()
            {
                return type;
            }
        };
    }

    @Override
    public <T> InjectionTarget<T> createInjectionTarget(final AnnotatedType<T> type)
    {
        return new NoopInjectionTarget<T>()
        {
            @Override
            public T produce(CreationalContext<T> ctx)
            {
                return objectLocator.autobuild(type.getJavaClass());
            }

            @Override
            public void preDestroy(T instance)
            {
                try
                {
                    for (Method method : type.getJavaClass().getMethods())
                    {
                        if (method.getAnnotation(PreDestroy.class) != null)
                        {
                            method.invoke(instance);
                        }
                    }
                }
                catch (Exception e)
                {
                    logger.error(
                            "Error invoking @PreDestroy callback on instance of class "
                                    + type.getJavaClass(), e);
                }
            }
        };
    }

    @Override
    public <T> CreationalContext<T> createCreationalContext(Contextual<T> contextual)
    {
        return new NoopCreationalContext<T>();
    }
}
