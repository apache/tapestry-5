// Copyright 2006, 2007, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.annotations.EagerLoad;
import org.apache.tapestry5.ioc.annotations.Scope;

/**
 * Used by {@link org.apache.tapestry5.ioc.internal.DefaultModuleDefImplTest}.
 */
public class SimpleModule
{
    @Scope("threaded")
    public FoeService buildBarney()
    {
        return null;
    }

    public FieService buildFred()
    {
        return null;
    }

    @EagerLoad
    public FoeService buildWilma()
    {
        return null;
    }

    void ignoredMethod()
    {
    }

    /**
     * Minimal decorator method that uses generics to qualify the delegate passed in and the object returned.
     */
    public <T> T decorateLogging(Class<T> serviceInterace, T delegate)
    {
        return null;
    }

    /**
     * Minimal contribution method.
     */
    public void contributeBarney(Configuration configuration)
    {

    }
}
