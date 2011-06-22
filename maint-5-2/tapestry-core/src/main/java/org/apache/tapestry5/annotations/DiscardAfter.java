// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.COMPONENT;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.MIXIN;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.PAGE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.tapestry5.ioc.annotations.UseWith;

/**
 * Marks a method of a page or a component to discard all persistent field changes. The changes are
 * eliminated from persistent storage after the marked method is invoked. Any exception (runtime or checked) 
 * thrown by annotated method will cause persistent fields to keep their values.
 * 
 * @see org.apache.tapestry5.ComponentResources#discardPersistentFieldChanges()
 * @since 5.2.0
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
@UseWith({COMPONENT,MIXIN,PAGE})
public @interface DiscardAfter
{

}
