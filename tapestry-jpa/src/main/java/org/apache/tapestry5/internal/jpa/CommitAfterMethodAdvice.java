// Copyright 2011, 2012, 2014 The Apache Software Foundation
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

package org.apache.tapestry5.internal.jpa;

import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.jpa.EntityTransactionManager;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodInvocation;

public class CommitAfterMethodAdvice implements MethodAdvice
{
    private EntityTransactionManager manager;
    private String context;

    public CommitAfterMethodAdvice(EntityTransactionManager manager, String context)
    {
        this.manager = manager;
        this.context = context;
    }

    @Override
    public void advise(final MethodInvocation invocation)
    {
        manager.invokeInTransaction(context, new Invokable<MethodInvocation>()
        {
            @Override
            public MethodInvocation invoke()
            {
                return invocation.proceed();
            }
        });

    }
}
