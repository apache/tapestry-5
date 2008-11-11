// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.services.SymbolSource;

/**
 * Used by {@link org.apache.tapestry5.ioc.internal.ModuleImplTest}.
 */
public class ExtraPublicConstructorsModule
{
    private final SymbolSource source;

    public ExtraPublicConstructorsModule()
    {
        throw new RuntimeException("This constructor should not be invoked.");
    }

    /**
     * Should be the first constructor, the one that gets invoked. I'm worried that different compilers or JVMs will
     * order the constructors differently.
     */
    public ExtraPublicConstructorsModule(SymbolSource source)
    {
        this.source = source;
    }

    public UpcaseService buildUpcaser()
    {
        return new UpcaseService()
        {
            public String upcase(String input)
            {
                return source.expandSymbols(input).toUpperCase();
            }
        };
    }

    public void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add("fred", "flintstone");
    }
}
