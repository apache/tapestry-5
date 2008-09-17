//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc;

import org.apache.tapestry5.ioc.services.ChainBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ContributeByClassModule
{
    public static StringTransformer buildMasterStringTransformer(final Collection<StringTransformer> configuration)
    {
        return new StringTransformer()
        {
            public String transform(String input)
            {
                String current = input;

                for (StringTransformer t : configuration)
                {
                    current = t.transform(current);
                }
                return current;
            }
        };
    }

    public static StringTransformer buildStringTransformerChain(final List<StringTransformer> configuration,
                                                                ChainBuilder builder)
    {
        return builder.build(StringTransformer.class, configuration);
    }

    public static void contributeMasterStringTransformer(Configuration<StringTransformer> configuration)
    {
        configuration.addInstance(UppercaseStringTransformer.class);
    }

    public static void contributeStringTransformerChain(final OrderedConfiguration<StringTransformer> configuration)
    {
        configuration.addInstance("Default", UppercaseStringTransformer.class);
    }

    public static StringTransformer buildMappedStringTransformer(final Map<String, StringTransformer> configuration)
    {
        return new StringTransformer()
        {
            public String transform(String input)
            {
                return configuration.get("Default").transform(input);
            }
        };
    }

    public static void contributeMappedStringTransformer(MappedConfiguration<String, StringTransformer> configuration)
    {
        configuration.addInstance("Default", UppercaseStringTransformer.class);
    }
}
