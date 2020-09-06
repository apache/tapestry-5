// Copyright 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;

public abstract class AbstractConfigurationImpl<T>
{
    private final Class<T> contributionType;

    private final ObjectLocator locator;

    public AbstractConfigurationImpl(Class<T> contributionType, ObjectLocator locator)
    {
        this.contributionType = contributionType;
        this.locator = locator;
    }

    protected T instantiate(Class<? extends T> clazz)
    {
        assert clazz != null;

        // Only attempt to proxy the class if it is the right type for the contribution. Starting
        // in 5.3, it is allowed to make contributions of different types (as long as they can be
        // coerced to the right type) ... but this means that sometimes, a class is passed that isn't
        // assignable to the actual contribution type.

        if (contributionType.isInterface() && InternalUtils.isLocalFile(clazz)
                && contributionType.isAssignableFrom(clazz))
            return locator.proxy(contributionType, clazz);

        return locator.autobuild(clazz);
    }
}
