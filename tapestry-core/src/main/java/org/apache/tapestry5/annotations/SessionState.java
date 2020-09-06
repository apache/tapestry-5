// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.tapestry5.annotations;

import org.apache.tapestry5.ioc.annotations.UseWith;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.apache.tapestry5.ioc.annotations.AnnotationUseContext.*;

/**
 * Marker annotation for a property that is a <em>session state object</em> (SSO) as controlled by
 * the {@link org.apache.tapestry5.services.ApplicationStateManager}. An SSO property is stored as
 * global session object (i.e., in the {@link javax.servlet.ServletContext}), accessible to every
 * page or component, but in fact the built-in strategies for SSO management are
 * session-specific, ultimately storing data in the {@link org.apache.tapestry5.http.services.Session}.
 * <p>
 * For each SSO property, you may declare a companion boolean property that can be checked to see if
 * the SSO has been created yet. The companion boolean property must have the same name as the SSO
 * property but suffixed with "Exists" (e.g., an SSO property named "user" and a companion boolean
 * property named "userExists"). You can access the boolean property to determine whether the SSO
 * has already been created. This mechanism is necessary because even a null check ("user != null")
 * may force the SSO to be created. Instead, check the companion boolean field ("userExists").
 * <p>
 * <em>Note: Tapestry 5.0 called these objects "Application State Objects"; thus some of the underlying
 * services have confusing names (e.g., ApplicationStateManager, which really should be SessionStateManager)
 * but can't be renamed for backward compatibility reasons.</em>
 *
 * @since 5.1.0.4
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
@UseWith({COMPONENT,MIXIN,PAGE})
public @interface SessionState
{
    /**
     * If true (the default), then referencing an field marked with the annotation will create the SSO.  If false, then
     * accessing the field will not create the SSO, it will only allow access to it if it already exists.
     */
    boolean create() default true;
}
