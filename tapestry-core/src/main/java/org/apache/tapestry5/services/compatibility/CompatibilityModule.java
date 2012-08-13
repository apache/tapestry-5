// Copyright 2012 The Apache Software Foundation
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

package org.apache.tapestry5.services.compatibility;

import org.apache.tapestry5.internal.services.compatibility.CompatibilityImpl;
import org.apache.tapestry5.internal.services.compatibility.DeprecationWarningImpl;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;

/**
 * Defines services for managing compatibility across releases.
 *
 * @since 5.4
 */
public class CompatibilityModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(Compatibility.class, CompatibilityImpl.class);
        binder.bind(DeprecationWarning.class, DeprecationWarningImpl.class);
    }

    @Contribute(Compatibility.class)
    public void enableAllCompatibilityTemporarily(Configuration<Trait> configuration)
    {
        for (Trait t : Trait.values())
        {
            configuration.add(t);
        }
    }

}
