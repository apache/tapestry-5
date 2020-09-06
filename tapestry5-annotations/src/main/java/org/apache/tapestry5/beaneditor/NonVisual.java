// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.beaneditor;

import org.apache.tapestry5.ioc.annotations.UseWith;
import org.apache.tapestry5.ioc.annotations.AnnotationUseContext;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Marker annotation for properties which are non-visual, and so should not appear (by default) inside a 
 * <a href="https://tapestry.apache.org/current/apidocs/org/apache/tapestry5/beaneditor/BeanModel.html">BeanModel</a>. 
 * The annotation may be placed on either the getter or the setter method or on the field.
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RUNTIME)
@Documented
@UseWith(AnnotationUseContext.BEAN)
public @interface NonVisual
{

}
