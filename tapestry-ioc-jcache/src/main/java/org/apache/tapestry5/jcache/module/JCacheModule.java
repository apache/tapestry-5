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
package org.apache.tapestry5.jcache.module;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResolverFactory;
import javax.cache.annotation.CacheResult;

import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Advise;
import org.apache.tapestry5.ioc.annotations.Match;
import org.apache.tapestry5.jcache.internal.CacheLookupUtil;
import org.apache.tapestry5.jcache.internal.CacheMethodAdvice;
import org.apache.tapestry5.jcache.internal.CachePutMethodAdvice;
import org.apache.tapestry5.jcache.internal.CacheRemoveAllMethodAdvice;
import org.apache.tapestry5.jcache.internal.CacheRemoveMethodAdvice;
import org.apache.tapestry5.jcache.internal.CacheResultMethodAdvice;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.jsr107.ri.annotations.CacheContextSource;
import org.jsr107.ri.annotations.DefaultCacheKeyGenerator;
import org.jsr107.ri.annotations.DefaultCacheResolverFactory;

/**
 * Tapestry-IoC module that
 */
public final class JCacheModule
{

    private JCacheModule()
    {
    }

    /**
     * Declares some services.
     *
     * @param binder
     *            a {@link ServiceBinder}.
     */
    public static void bind(ServiceBinder binder)
    {
        binder.bind(CacheKeyGenerator.class, DefaultCacheKeyGenerator.class);
        binder.bind(CacheResolverFactory.class, DefaultCacheResolverFactory.class);
        binder.bind(CacheContextSource.class, CacheLookupUtil.class);
    }

    /**
     * Applies the advice to the services.
     *
     * @param receiver
     *            a {@link MethodAdviceReceiver}.
     * @param objectLocator
     *            an {@link ObjectLocator}.
     */
    @Match("*")
    @Advise(id = "JCache")
    public static void adviseCache(MethodAdviceReceiver receiver, ObjectLocator objectLocator)
    {
        advise(CachePut.class, objectLocator, CachePutMethodAdvice.class, receiver);
        advise(CacheRemoveAll.class, objectLocator, CacheRemoveAllMethodAdvice.class,
                receiver);
        advise(CacheRemove.class, objectLocator, CacheRemoveMethodAdvice.class, receiver);
        advise(CacheResult.class, objectLocator, CacheResultMethodAdvice.class, receiver);
    }

    private static void advise(Class<? extends Annotation> annotationClass, ObjectLocator objectLocator,
            Class<? extends CacheMethodAdvice> adviceClass, MethodAdviceReceiver methodAdviceReceiver)
    {
        // TAP5-2466: create the advice on-demand to avoid recursion issues
        MethodAdvice advice = null;

        if (methodAdviceReceiver.getClassAnnotationProvider().getAnnotation(annotationClass) != null)
        {
            advice = build(objectLocator, adviceClass);

            methodAdviceReceiver.adviseAllMethods(advice);
        }
        else
        {
            for (Method method : methodAdviceReceiver.getInterface().getMethods())
            {
                if (methodAdviceReceiver.getMethodAnnotation(method, annotationClass) != null)
                {
                    if(advice== null)
                    {
                        advice = build(objectLocator, adviceClass);
                    }

                    methodAdviceReceiver.adviseMethod(method, advice);
                }
            }
        }
    }

    private static CacheMethodAdvice build(ObjectLocator objectLocator, Class<? extends CacheMethodAdvice> advice)
    {
        return objectLocator.autobuild(advice);
    }
}
