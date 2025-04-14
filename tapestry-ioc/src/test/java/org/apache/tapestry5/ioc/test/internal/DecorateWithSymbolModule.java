// Copyright 2025 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.test.internal;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Decorate;
import org.apache.tapestry5.ioc.annotations.Match;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.services.FactoryDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;

public class DecorateWithSymbolModule
{
    public static final String SYMBOL_KEY = "symbol-key";

    public static final int ORIGINAL_VALUE = 23;
    public static final int SYMBOL_VALUE = 42;

    @FactoryDefaults
    @Contribute(SymbolProvider.class)
    public static void contributeFactoryDefaults(MappedConfiguration<String, Object> conf)
    {
        conf.add(SYMBOL_KEY, SYMBOL_VALUE);
    }

    public FoeService buildFoeService()
    {
        return new FoeService()
        {

            @Override
            public int foe()
            {
                return ORIGINAL_VALUE;
            }
        };
    }

    @Decorate(serviceInterface = FoeService.class)
    @Match(value = "FoeService")
    public static FoeService decorateSymbolService(FoeService delegate,
            @Symbol(SYMBOL_KEY)
            int symbolValue)
    {
        return () -> symbolValue;
    }
}
