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
import java.lang.reflect.Method;

import org.apache.tapestry5.plastic.MethodInvocation;
import org.jsr107.ri.annotations.AbstractInternalCacheInvocationContext;
import org.jsr107.ri.annotations.StaticCacheInvocationContext;

/**
 * Tapestry-IoC specific cache invocation context using {@link MethodInvocation}
 * 
 * @param <A>
 *            The type of annotation this context information is for. One of
 *            {@link javax.cache.annotation.CacheResult}, {@link javax.cache.annotation.CachePut},
 *            {@link javax.cache.annotation.CacheRemove}, or
 *            {@link javax.cache.annotation.CacheRemoveAll}.
 */
public class TapestryIoCInternalCacheInvocationContext<A extends Annotation> extends
        AbstractInternalCacheInvocationContext<MethodInvocation, A>
{

    /**
     * Create new cache key invocation context for the static context and
     * invocation
     * 
     * @param staticCacheInvocationContext
     *            Static information about the invoked method
     * @param invocation
     *            The AOP Alliance invocation context
     */
    public TapestryIoCInternalCacheInvocationContext(
            StaticCacheInvocationContext<A> staticCacheInvocationContext,
            MethodInvocation invocation)
    {
        super(staticCacheInvocationContext, invocation);
    }

    @Override
    protected Object[] getParameters(MethodInvocation invocation)
    {
        Object[] parameters = new Object[invocation.getMethod().getParameterTypes().length];
        for (int i = 0; i < parameters.length; i++)
        {
            parameters[i] = invocation.getParameter(i);
        }
        return parameters;
    }

    @Override
    protected Method getMethod(MethodInvocation invocation)
    {
        return invocation.getMethod();
    }

    @Override
    protected Object getTarget(MethodInvocation invocation)
    {
        return invocation.getInstance();
    }

}
