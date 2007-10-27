// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.ioc.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.tapestry.ioc.def.ServiceDef;

/**
 * Used to define a {@linkplain ServiceDef#getMarker() marker annotation} for a service
 * implementation. This allows for injection based on the combination of type and marker interface.
 * These marker interfaces should not have any values. The mere presence of the marker annotation is
 * all that is needed.
 * <p>
 * When applied to a module class, this sets the default marker for all services within the module
 * (whereas the normal default marker is null).
 */
@Target(
{ TYPE, METHOD })
@Retention(RUNTIME)
@Documented
public @interface Marker
{
    /** The type of annotation (which will be present at the injection point). */
    Class value();
}
