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

package org.apache.tapestry5.ioc.def;

/**
 * Extension to {@link org.apache.tapestry5.ioc.def.ServiceDef} containing new methods added for Tapestry 5.1.
 */
public interface ServiceDef2 extends ServiceDef
{
    /**
     * Returns true if the service should not be decorated.  Most services allow decoration, unless the {@link
     * org.apache.tapestry5.ioc.annotations.PreventServiceDecoration} annotation is present.
     */
    boolean isPreventDecoration();
}
