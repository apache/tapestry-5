// Copyright 2006 The Apache Software Foundation
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

import org.apache.tapestry.ioc.annotations.Contribute;
import org.apache.tapestry.ioc.annotations.Id;
import org.apache.tapestry.ioc.annotations.Inject;

@Id("inject.literal")
public class InjectLiteralModule
{
    public static IntHolder buildIntHolder(@Inject("${value}")
    int value)
    {
        return new IntHolderImpl(value);
    }

    @Contribute("tapestry.ioc.FactoryDefaults")
    public static void contributeDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add("value", "42");
    }
}
