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

package org.apache.tapestry5.integration.app2.pages;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.integration.app2.FortyTwo;
import org.apache.tapestry5.integration.app2.PlusOne;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.BeanModelSource;

public class TestPrefixMethod
{
    @FortyTwo
    public int getValue()
    {
        return 0;
    }

    @Inject
    private ComponentResources resources;

    @Inject
    private BeanModelSource modelSource;

    @InjectPage
    private TestPrefixMethod2 otherPage;

    private int foo;

    @FortyTwo
    public int getValue2()
    {
        foo = modelSource.hashCode();
        return foo;
    }

    @PlusOne
    public int getValue3()
    {
        int value = otherPage.hashCode();
        return value * 0;
    }
}
