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

import org.apache.tapestry5.plastic.MethodInvocation;
import org.jsr107.ri.annotations.AbstractCacheRemoveAllInterceptor;
import org.jsr107.ri.annotations.CacheContextSource;
import org.jsr107.ri.annotations.InterceptorType;

public class CacheRemoveAllMethodAdvice extends AbstractCacheRemoveAllInterceptor<MethodInvocation>
        implements CacheMethodAdvice
{

    private final CacheContextSource<MethodInvocation> cacheContextSource;

    /** Single constructor of this class. */
    public CacheRemoveAllMethodAdvice(CacheContextSource<MethodInvocation> cacheContextSource)
    {
        this.cacheContextSource = cacheContextSource;
    }

    @Override
    public InterceptorType getInterceptorType()
    {
        return InterceptorType.CACHE_REMOVE_ALL;
    }

    @Override
    public void advise(MethodInvocation invocation)
    {
        try
        {
            this.cacheRemoveAll(cacheContextSource, invocation);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object proceed(MethodInvocation invocation) throws Throwable
    {
        invocation.proceed();
        if (invocation.didThrowCheckedException())
        {
            invocation.rethrow();
        }
        return invocation.getReturnValue();
    }

}
