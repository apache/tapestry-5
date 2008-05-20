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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.Registry;

/**
 * Used in {@link ServiceActivity} to identify the state of the service in terms of its overall lifecycle.
 */
public enum Status
{
    /**
     * A builtin service that exists before the {@link Registry} is constructed.
     */
    BUILTIN,

    /**
     * The service is defined in a module, but has not yet been referenced.
     */
    DEFINED,

    /**
     * A proxy has been created for the service, but no methods of the proxy have been invoked.
     */
    VIRTUAL,

    /**
     * A service implementation for the service has been created.
     */
    REAL
}
