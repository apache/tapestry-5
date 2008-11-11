// Copyright 2007, 2008 The Apache Software Foundation
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
 * Marker annotation for a field that is an <em>application state object</em> as controlled by the {@link
 * org.apache.tapestry5.services.ApplicationStateManager}.  <em>Application</em> is something of a misnomer, as it
 * implies that the object is stored as global, application-wide state (i.e., in the {@link
 * javax.servlet.ServletContext}). In fact, the built-in strategies for ASO management are <em>very</em> user specific,
 * ultimately storing data in the {@link org.apache.tapestry5.services.Session}.
 * <p/>
 * An ASO file may have a companion field, of type boolean, used to see if the ASO has been created yet. If another
 * field exists with the same name, suffixed with "Exists" (i.e., "aso" for the ASO field, and "asoExists" for the
 * companion field) and the type of that field is boolean, then access to the field will determine whether the ASO has
 * already been created. This is necessary because even a null check ("aso != null") may force the ASO to be created.
 * Instead, check the companion boolean field ("asoExists").
 */
@Target(FIELD)
@Documented
@Retention(RUNTIME)
public @interface ApplicationState
{
    /**
     * If true (the default), then referencing an field marked with the annotation will create the ASO.  If false, then
     * accessing the field will not create the ASO, it will only allow access to it if it already exists.
     */
    boolean create() default true;
}
