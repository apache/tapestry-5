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

package org.apache.tapestry5.ioc.annotations;

import java.lang.annotation.*;

/**
 * Marks a service as not eligible for decoration. This is useful for services that, if decorated, can cause cycle
 * dependency errors; for example, {@link org.apache.tapestry5.ioc.services.MasterObjectProvider}, or services
 * <em>contributed to</em> MasterObjectProvider, are good candidates for this annotation.
 * <p/>
 * The annotation can be applied to service implementation class or to a service builder method in a module class.
 * <p/>
 * The annotation may also be placed on a module class, to indicate that all services defined for the module should not
 * allow decoration.
 * <p/>
 * Service decoration includes the decoration mechanism (from Tapestry 5.0) and the newer service advice mechanism (from
 * Tapestry 5.1).
 *
 * @see org.apache.tapestry5.ioc.def.ServiceDef2#isPreventDecoration()
 * @since 5.1.0.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PreventServiceDecoration
{
}
