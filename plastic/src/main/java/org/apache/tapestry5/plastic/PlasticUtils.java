// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.plastic;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Utilities for user code making use of Plastic.
 */
public class PlasticUtils
{
    private static final AtomicLong UID_GENERATOR = new AtomicLong(System.nanoTime());

    /**
     * Returns a string that can be used as part of a Java identifier and is unique
     * for this JVM. Currently returns a hexadecimal string and initialized by
     * System.nanoTime() (but both those details may change in the future).
     * <p>
     * Note that the returned value may start with a numeric digit, so it should be used as a <em>suffix</em>, not
     * <em>prefix</em> of a Java identifier.
     * 
     * @return unique id that can be used as part of a Java identifier
     */
    public static String nextUID()
    {
        return Long.toHexString(PlasticUtils.UID_GENERATOR.getAndIncrement());
    }
}
