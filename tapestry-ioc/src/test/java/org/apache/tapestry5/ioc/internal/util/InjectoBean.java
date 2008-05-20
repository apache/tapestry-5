// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.util;

import org.apache.tapestry5.ioc.annotations.Inject;

public class InjectoBean
{
    private final String foo;
    private final Runnable bar;

    public InjectoBean()
    {
        this(null);
    }

    @Inject
    public InjectoBean(String foo)
    {
        this(foo, null);
    }

    /**
     * Normally, this would be chosen as it has the most parameters.
     */
    public InjectoBean(String foo, Runnable bar)
    {

        this.foo = foo;
        this.bar = bar;
    }

    public String getFoo()
    {
        return foo;
    }

    public Runnable getBar()
    {
        return bar;
    }
}
