// Copyright 2010 The Apache Software Foundation
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

import java.util.Set;

/**
 * Extended version of {@link org.apache.tapestry5.ioc.def.ModuleDef2} introduced in Tapestry 5.2 to allow
 * the definition of {@link org.apache.tapestry5.ioc.def.StartupDef}s.
 *
 * @since 5.2.0
 */
public interface ModuleDef3 extends ModuleDef2
{
    /**
     * Returns all the {@link org.apache.tapestry5.ioc.def.StartupDef}s.
     */
    Set<StartupDef> getStartupDefs();
}
