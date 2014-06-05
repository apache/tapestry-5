// Copyright 2009, 2011 The Apache Software Foundation
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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Internal utilities for identifying the JDK version, used in the rare cases
 * that we are patching around JDK bugs.
 */
public class JDKUtils
{
    /**
     * Is the running JVM JDK 1.5?
     */
    public static final boolean JDK_1_5 = isVersion("1.5");

    private static boolean isVersion(String versionId)
    {
        return System.getProperty("java.specification.version").equals(versionId);
    }

    /**
     * Returns a {@link ReentrantLock} used to serialize access to the construction of a thread local; this is only needed under JDK 1.5 (due to a bug in the JDK);
     * for other JDKs, a {@link DummyLock} is returned.
     *
     * @return lock to use when creating
     * @since 5.3
     * @deprecated Deprecated in 5.4 with no replacement.
     */
    public static Lock createLockForThreadLocalCreation()
    {
        return JDK_1_5 ? new ReentrantLock() : new DummyLock();
    }
}
