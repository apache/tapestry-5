// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.annotations;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Optional, but typically used, annotation for service decorator methods, used to define which services the decorator
 * applies to. This annotation defines a number of <em>patterns</em> that allow services across multiple modules to be
 * selected. A decorator is applied to a service if any of its patterns match the service.
 * <p/>
 * TODO: Describe pattern glob-match syntax
 * <p/>
 * When the Match annotation is not supplied, then the decorator only applies to a single service: the service whose id
 * matches the decorators id; that is, method <code>decorateMyService()</code> would decorate only the service provided
 * by the <code>buildMyService()</code> method, within the same module.
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface Match
{

    /**
     * Defines a list of patterns matched against potential service ids to identify to which services the decorator
     * applies. A decorator is applied if <em>any</em> of the patterns match.
     */
    String[] value();
}
