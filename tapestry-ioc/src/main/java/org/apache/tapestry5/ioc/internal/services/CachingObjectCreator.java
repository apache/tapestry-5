// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.ObjectCreator;

/**
 * An {@link org.apache.tapestry5.ioc.ObjectCreator} that delegates to another {@link
 * org.apache.tapestry5.ioc.ObjectCreator} and caches the result.
 */
public class CachingObjectCreator implements ObjectCreator
{
    private boolean cached;

    private Object cachedValue;

    private ObjectCreator delegate;

    public CachingObjectCreator(ObjectCreator delegate)
    {
        this.delegate = delegate;
    }

    public synchronized Object createObject()
    {
        if (!cached)
        {
            cachedValue = delegate.createObject();
            cached = true;
            delegate = null;
        }

        return cachedValue;
    }
}
