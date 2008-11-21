//  Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.MethodAdvice;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Used by {@link org.apache.tapestry5.ioc.internal.services.AdvisedMethodInvocationBuilder} to track the method being
 * invoked, and the list of {@link org.apache.tapestry5.ioc.MethodAdvice} associated with the method.
 */
public class MethodInfo
{
    private final Method method;

    private final List<MethodAdvice> advice = CollectionFactory.newList();

    public MethodInfo(Method method)
    {
        this.method = method;
    }

    void addAdvice(MethodAdvice advice)
    {
        this.advice.add(advice);
    }

    public int getAdviceCount() { return advice.size(); }

    public MethodAdvice getAdvice(int index)
    {
        return advice.get(index);
    }

    public Method getMethod()
    {
        return method;
    }
}
