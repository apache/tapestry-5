// Copyright 2008, 2009, 2013 The Apache Software Foundation
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

package org.apache.tapestry5.annotations;

import org.apache.tapestry5.ioc.annotations.AnnotationUseContext;
import org.apache.tapestry5.ioc.annotations.UseWith;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A marker annotation that indicates that the page in question may only be accessed with a exact activation context.
 *
 * @see org.apache.tapestry5.MetaDataConstants#UNKNOWN_ACTIVATION_CONTEXT_CHECK
 * @since 5.4
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@UseWith(AnnotationUseContext.PAGE)
public @interface UnknownActivationContextCheck
{
    /**
     * If <code>true</code>, the default, the framework will check for an exact (number and type of parameters)
     * activation context method and if not found will respond with a 404 Not Found status code, if <code>false</code>
     * the activation context is ignored as it was before 5.4 release.
     *
     * @see org.apache.tapestry5.MetaDataConstants#UNKNOWN_ACTIVATION_CONTEXT_CHECK
     * @see org.apache.tapestry5.services.HttpError
     */
    boolean value() default true;
}
