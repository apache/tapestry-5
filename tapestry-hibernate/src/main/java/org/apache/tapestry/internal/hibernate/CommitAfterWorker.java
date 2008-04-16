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

package org.apache.tapestry.internal.hibernate;

import org.apache.tapestry.hibernate.HibernateSessionManager;
import org.apache.tapestry.hibernate.annotations.CommitAfter;
import org.apache.tapestry.ioc.util.BodyBuilder;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.ComponentClassTransformWorker;
import org.apache.tapestry.services.TransformMethodSignature;

/**
 * Searches for methods that have the {@link org.apache.tapestry.hibernate.annotations.CommitAfter} annotation and adds
 * logic around the method to commit or abort the transaction.  The commit/abort logic is the same as for the {@link
 * org.apache.tapestry.hibernate.HibernateTransactionDecorator} service.
 */
public class CommitAfterWorker implements ComponentClassTransformWorker
{
    private final HibernateSessionManager _manager;

    public CommitAfterWorker(HibernateSessionManager manager)
    {
        _manager = manager;
    }

    public void transform(ClassTransformation transformation, MutableComponentModel model)
    {
        for (TransformMethodSignature sig : transformation.findMethodsWithAnnotation(CommitAfter.class))
        {
            addCommitAbortLogic(sig, transformation);
        }
    }

    private void addCommitAbortLogic(TransformMethodSignature method, ClassTransformation transformation)
    {
        String managerField = transformation.addInjectedField(HibernateSessionManager.class, "manager", _manager);

        // Handle the normal case, a succesful method invocation.

        transformation.extendExistingMethod(method, String.format("%s.commit();", managerField));

        // Now, abort on any RuntimeException

        BodyBuilder builder = new BodyBuilder().begin().addln("%s.abort();", managerField);

        builder.addln("throw $e;").end();

        transformation.addCatch(method, RuntimeException.class.getName(), builder.toString());

        // Now, a commit for each thrown exception

        builder.clear();
        builder.begin().addln("%s.commit();", managerField).addln("throw $e;").end();

        String body = builder.toString();

        for (String name : method.getExceptionTypes())
        {
            transformation.addCatch(method, name, body);
        }
    }
}
