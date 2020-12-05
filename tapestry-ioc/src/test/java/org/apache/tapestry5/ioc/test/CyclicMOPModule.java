// Copyright 2008, 2010 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.ioc.test;

import org.apache.tapestry5.commons.Configuration;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.services.CoercionTuple;
import org.apache.tapestry5.ioc.annotations.Symbol;

public class CyclicMOPModule
{
    public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add("symbol-value", "99");
    }

    public static Runnable decorateTrigger(Runnable delegate, @Symbol("symbol-value")
    int value)
    {
        return delegate;
    }

    public static Runnable buildTrigger()
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
            }
        };
    }

    public static void contributeTypeCoercer(MappedConfiguration<CoercionTuple.Key, CoercionTuple> configuration, @Symbol("symbol-value")
    int value)
    {
        assert value == 99;
    }
}
