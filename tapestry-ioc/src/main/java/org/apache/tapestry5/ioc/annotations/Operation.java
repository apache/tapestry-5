// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.annotations;

import java.lang.annotation.*;

/**
 * Describes a method as one that should be operation tracked. Operation tracking is useful when an exception in deeply nested code occurs,
 * as it is possible to identify (using human readable descriptions) the path to the code that failed.
 *
 * @see org.apache.tapestry5.ioc.OperationTracker
 * @since 5.4
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@UseWith({AnnotationUseContext.SERVICE, AnnotationUseContext.COMPONENT, AnnotationUseContext.PAGE})
public @interface Operation
{
    /**
     * The message to pass to {@link org.apache.tapestry5.ioc.OperationTracker#invoke(String, org.apache.tapestry5.ioc.Invokable)}. If the message contains
     * the '%' character, it is interpreted to be a {@linkplain java.util.Formatter format string}, passed the method's parameters.
     *
     * @see org.apache.tapestry5.ioc.services.OperationAdvisor#createAdvice(String)
     */
    String value();
}
