// Copyright 2008, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.ioc.internal.services.LoggingAdvice;
import org.apache.tapestry5.ioc.services.ExceptionTracker;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.services.transform.ComponentClassTransformWorker2;
import org.apache.tapestry5.services.transform.TransformationSupport;

import java.util.List;

/**
 * Looks for the {@link org.apache.tapestry5.annotations.Log} marker annotation and adds method advice to perform the
 * logging. This is similar to what the {@link org.apache.tapestry5.ioc.services.LoggingDecorator} does for service
 * interface methods.
 */
public class LogWorker implements ComponentClassTransformWorker2
{
    private final ExceptionTracker exceptionTracker;

    public LogWorker(ExceptionTracker exceptionTracker)
    {
        this.exceptionTracker = exceptionTracker;
    }

    public void transform(PlasticClass plasticClass, TransformationSupport support, MutableComponentModel model)
    {
        List<PlasticMethod> methods = plasticClass.getMethodsWithAnnotation(Log.class);

        if (methods.isEmpty())
        {
            return;
        }

        final MethodAdvice loggingAdvice = new LoggingAdvice(model.getLogger(), exceptionTracker);

        for (PlasticMethod method : methods)
        {
            method.addAdvice(loggingAdvice);
        }
    }
}
