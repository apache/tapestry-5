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

package org.apache.tapestry.ioc;

import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.annotations.InjectService;

public class IndirectionModule
{
    public Indirection buildInner()
    {
        return new Indirection()
        {
            public String getName()
            {
                return "INNER";
            }
        };
    }

    public Indirection buildOuter(final @InjectService("${indirection.inner}")
    Indirection inner)
    {
        return new Indirection()
        {
            public String getName()
            {
                return String.format("OUTER[%S]", inner.getName());
            }
        };
    }

    public Indirection buildOuter2(final @Inject("${indirection.object-inner}")
    Indirection inner)
    {
        return new Indirection()
        {
            public String getName()
            {
                return String.format("OUTER2[%S]", inner.getName());
            }
        };
    }

    public void contributeFactoryDefaults(MappedConfiguration<String, String> configuration)
    {
        // You tend to want to use the fully qualified service id, since you can't tell under which
        // context the
        // symbols will be expanded.

        configuration.add("indirection.inner", "Inner");
        configuration.add("indirection.object-inner", "service:Inner");
    }
}
