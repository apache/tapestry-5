// Copyright 2009, 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.commons.ObjectCreator;
import org.apache.tapestry5.commons.internal.util.LockSupport;

/**
 * An {@link org.apache.tapestry5.commons.ObjectCreator} that delegates to another
 * {@link org.apache.tapestry5.commons.ObjectCreator} and caches the result.
 */
public class CachingObjectCreator<T> extends LockSupport implements ObjectCreator<T>
{
    private boolean cached;

    private T cachedValue;

    private ObjectCreator<T> delegate;

    public CachingObjectCreator(ObjectCreator<T> delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public T createObject()
    {
        try
        {
            acquireReadLock();

            if (!cached)
            {
                cacheValueFromDelegate();
            }

            return cachedValue;
        } finally
        {
            releaseReadLock();
        }
    }

    private void cacheValueFromDelegate()
    {
        try
        {
            upgradeReadLockToWriteLock();

            if (!cached)
            {
                cachedValue = delegate.createObject();
                cached = true;
                delegate = null;
            }
        } finally
        {
            downgradeWriteLockToReadLock();
        }
    }
}
