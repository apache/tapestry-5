// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ioc.ObjectProvider;

/**
 * Allows injection of various objects into a component class. for certain cases, the type dictates
 * a particular property from {@link ComponentResources} is injected, but in most cases it works
 * like the {@link org.apache.tapestry.ioc.annotations.Inject} annotation used by the IoC container.
 * 
 * @see org.apache.tapestry.services.InjectionProvider
 * @see ObjectProvider
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
public @interface Inject
{

}
