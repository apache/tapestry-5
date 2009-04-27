// Copyright 2009 The Apache Software Foundation
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

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.FIELD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Marker annotation for a field that is a <em>session state object</em> (SSO) as controlled by the {@link
 * org.apache.tapestry5.services.ApplicationStateManager}. An SSO stored as global session object (i.e., in the {@link
 * javax.servlet.ServletContext}); every page or component. In fact, the built-in strategies for ASO management are
 * <em>very</em> user specific, ultimately storing data in the {@link org.apache.tapestry5.services.Session}.
 * <p/>
 * An SSO field may have a companion field, of type boolean, used to see if the SSO has been created yet. If another
 * field exists with the same name, suffixed with "Exists" (i.e., "sso" for the SSO field, and "ssoExists" for the
 * companion field) and the type of that field is boolean, then access to the field will determine whether the SSO has
 * already been created. This is necessary because even a null check ("sso != null") may force the SSO to be created.
 * Instead, check the companion boolean field ("asoExists").
 * <p/>
 * Note: Tapestry 5.0 called these objects "Application State Objects"; thus many of the underlying services have a
 * confusing name (ApplicationStateManager, which really should be SessionStateManager ... but can't be renamed for
 * backwards compatibility reasons).
 *
 * @since 5.1.0.4
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
public @interface SessionState
{
    /**
     * If true (the default), then referencing an field marked with the annotation will create the SSO.  If false, then
     * accessing the field will not create the SSO, it will only allow access to it if it already exists.
     */
    boolean create() default true;
}
