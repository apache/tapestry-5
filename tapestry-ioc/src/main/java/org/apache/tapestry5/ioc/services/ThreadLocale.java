// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

import org.apache.tapestry5.ioc.Registry;

import java.util.Locale;

/**
 * Stores the locale <em>for the current thread</em>. This value persists until {@link Registry#cleanupThread()} is
 * invoked.
 */
public interface ThreadLocale
{
    /**
     * Updates the locale for the current thread.
     *
     * @param locale the new locale (may not be null)
     */
    void setLocale(Locale locale);

    /**
     * Returns the thread's locale, which will be the JVM's default locale, until {@link #setLocale(Locale)} is
     * invoked.
     *
     * @return the thread's locale
     */
    Locale getLocale();
}
