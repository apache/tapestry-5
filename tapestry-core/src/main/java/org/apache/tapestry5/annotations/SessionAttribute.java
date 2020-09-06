// Copyright 2007, 2009, 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.annotations;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.*;

import org.apache.tapestry5.http.services.Session;
import org.apache.tapestry5.ioc.annotations.UseWith;

/**
 * Used to map a property of a page or component to value stored in session.
 * 
 * @since 5.2.0
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
@UseWith({COMPONENT,MIXIN,PAGE})
public @interface SessionAttribute
{
    
    /**
     * Name of a the {@link Session} attribute to which the field will be mapped; if not specified,
     * defaults to the name of the field.
     */
    String value() default "";
}