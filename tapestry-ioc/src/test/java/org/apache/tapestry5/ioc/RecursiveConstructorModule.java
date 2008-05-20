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

package org.apache.tapestry5.ioc;

import org.apache.tapestry5.ioc.annotations.InjectService;

/**
 * Used by {@link org.apache.tapestry5.ioc.IntegrationTest}.
 */
public class RecursiveConstructorModule
{

    public RecursiveConstructorModule(@InjectService("Runnable")
    Runnable r)
    {
        // Invoking a method on the service proxy is going to cause a recursive attempt to
        // instantiate the module. Hilarity ensues.

        r.run();
    }

    public Runnable buildRunnable()
    {
        return new Runnable()
        {
            public void run()
            {
            }
        };
    }
}
