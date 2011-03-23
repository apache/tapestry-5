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

package org.apache.tapestry5.internal.hibernate;

import org.apache.tapestry5.hibernate.HibernateSessionManager;
import org.apache.tapestry5.hibernate.annotations.CommitAfter;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.*;

/**
 * Searches for methods that have the {@link org.apache.tapestry5.hibernate.annotations.CommitAfter} annotation and adds
 * logic around the method to commit or abort the transaction.  The commit/abort logic is the same as for the {@link
 * org.apache.tapestry5.hibernate.HibernateTransactionDecorator} service.
 */
public class CommitAfterWorker implements ComponentClassTransformWorker
{
    private final HibernateSessionManager manager;

    private final ComponentMethodAdvice advice = new ComponentMethodAdvice()
    {
        public void advise(ComponentMethodInvocation invocation)
        {
            try
            {
                invocation.proceed();

                // Success or checked exception:

                manager.commit();
            }
            catch (RuntimeException ex)
            {
                manager.abort();

                throw ex;
            }
        }
    };

    public CommitAfterWorker(HibernateSessionManager manager)
    {
        this.manager = manager;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (TransformMethodSignature sig : transformation.findMethodsWithAnnotation(CommitAfter.class))
        {
            transformation.advise(sig, advice);
        }
    }
}
