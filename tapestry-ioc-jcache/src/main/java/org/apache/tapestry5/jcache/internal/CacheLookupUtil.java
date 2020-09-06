// Copyright 2014 The Apache Software Foundation
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
package org.apache.tapestry5.jcache.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.CacheResolverFactory;
import javax.inject.Singleton;

import org.apache.tapestry5.beanmodel.services.PlasticProxyFactoryImpl;
import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.jsr107.ri.annotations.AbstractCacheLookupUtil;
import org.jsr107.ri.annotations.InternalCacheInvocationContext;
import org.jsr107.ri.annotations.InternalCacheKeyInvocationContext;
import org.jsr107.ri.annotations.StaticCacheInvocationContext;
import org.jsr107.ri.annotations.StaticCacheKeyInvocationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapted to Tapestry-IoC from the Guice implementation in the reference implementation.
 */
@Singleton
public class CacheLookupUtil extends AbstractCacheLookupUtil<MethodInvocation>
{

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheLookupUtil.class);

    private static final Pattern SERVICE_PROXY_CLASS_NAME = Pattern.compile("\\$.+_[a-f0-9]+");

    private final ObjectLocator objectLocator;
    private final CacheKeyGenerator defaultCacheKeyGenerator;
    private final CacheResolverFactory defaultCacheResolverFactory;

    /**
     * Single constructor of this class.
     * 
     * @param defaultCacheKeyGenerator
     *            the default CacheKeyGenerator
     * @param defaultCacheResolverFactory
     *            the default CacheResolverFactory
     */
    public CacheLookupUtil(ObjectLocator objectLocator, CacheKeyGenerator defaultCacheKeyGenerator,
            CacheResolverFactory defaultCacheResolverFactory)
    {
        this.objectLocator = objectLocator;
        this.defaultCacheKeyGenerator = defaultCacheKeyGenerator;
        this.defaultCacheResolverFactory = defaultCacheResolverFactory;
    }

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    @Override
    protected InternalCacheKeyInvocationContext<? extends Annotation> createCacheKeyInvocationContextImpl(
            StaticCacheKeyInvocationContext<? extends Annotation> staticCacheKeyInvocationContext,
            MethodInvocation invocation)
    {
        return new TapestryIoCInternalCacheKeyInvocationContext(staticCacheKeyInvocationContext,
                invocation);
    }

    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    @Override
    protected InternalCacheInvocationContext<? extends Annotation> createCacheInvocationContextImpl(
            StaticCacheInvocationContext<? extends Annotation> staticCacheInvocationContext,
            MethodInvocation invocation)
    {
        return new TapestryIoCInternalCacheInvocationContext(staticCacheInvocationContext,
                invocation);
    }

    @Override
    protected Class<?> getTargetClass(MethodInvocation invocation)
    {
        final Object instance = invocation.getInstance();
        Class<?> clasz = instance.getClass();
        // Here be dragons . . .
        if (SERVICE_PROXY_CLASS_NAME.matcher(clasz.getName()).matches())
        {
            clasz = getDelegateType(instance);
        }
        return clasz;
    }

    // Here be bigger dragons . . .
    private Class<?> getDelegateType(Object instance)
    {
        Object delegate = instance;
        Class<?> clasz = delegate.getClass();
        if (SERVICE_PROXY_CLASS_NAME.matcher(clasz.getName()).matches())
        {
            try
            {
                delegate = getDelegate(delegate, clasz);
                // More than one advice causes proxies to be nested
                clasz = getDelegateType(delegate);
            }
            catch (Exception e)
            {
                LOGGER.error("Exception while getting service implementation type", e);
                throw new RuntimeException("Exception while getting service implementation type", e);
            }
        }
        return clasz;
    }

    private Object getDelegate(Object instance, Class<?> clasz) throws IllegalAccessException,
            InvocationTargetException
    {
        try
        {
            return clasz.getDeclaredMethod(PlasticProxyFactoryImpl.INTERNAL_GET_DELEGATE).invoke(
                    instance);
        }
        catch (Exception e)
        {
            throw new RuntimeException(String.format("Couldn't find method %s in %s",
                    PlasticProxyFactoryImpl.INTERNAL_GET_DELEGATE, instance.getClass().getName()));
        }
    }

    @Override
    protected Method getMethod(MethodInvocation invocation)
    {
        Method method = invocation.getMethod();
        final Class<?> methodClass = method.getClass();
        final Class<?> targetClass = getTargetClass(invocation);
        if (methodClass != targetClass)
        {
            method = findMethod(method, targetClass);
        }
        return method;
    }

    private Method findMethod(final Method method, final Class<?> targetClass)
    {
        try
        {
            return targetClass.getMethod(method.getName(), (Class<?>[]) method.getParameterTypes());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected <T> T getObjectByType(Class<T> type)
    {
        return this.objectLocator.getObject(type, null);
    }

    @Override
    protected CacheKeyGenerator getDefaultCacheKeyGenerator()
    {
        return this.defaultCacheKeyGenerator;
    }

    @Override
    protected CacheResolverFactory getDefaultCacheResolverFactory()
    {
        return this.defaultCacheResolverFactory;
    }
}
