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

package org.apache.tapestry.services;

import org.apache.tapestry.ioc.ObjectProvider;

/**
 * Primarily, the Alias service is an ObjectProvider (for the "alias" object provider prefix).
 * However, it has to know what mode it is operating in (this allows for spot-overrides of certain
 * services to adapt to a new environment, such as Portlet).
 */
public interface Alias
{
    /** Inform the Alias service about what mode it is operating in. */
    void setMode(String mode);

    /**
     * Returns an object that can provide objects based on contributions into the Alias (and
     * AliasOverrides) service.
     */
    ObjectProvider getObjectProvider();
}
