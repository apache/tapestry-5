// Copyright 2008, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.http.internal.services;

import org.apache.tapestry5.http.annotations.ImmutableSessionPersistedObject;
import org.apache.tapestry5.http.services.SessionPersistedObjectAnalyzer;

/**
 * Default catch-all implementation of {@link org.apache.tapestry5.http.services.SessionPersistedObjectAnalyzer}.
 *
 * @since 5.1.0.0
 */
public class DefaultSessionPersistedObjectAnalyzer implements SessionPersistedObjectAnalyzer<Object>
{
    /**
     * An object is dirty <em>unless</em> it has the {@link org.apache.tapestry5.http.annotations.ImmutableSessionPersistedObject}
     * annotation.
     *
     * @param sessionPersistedObject to analyze
     * @return false if immutable, true otherwise
     */
    public boolean checkAndResetDirtyState(Object sessionPersistedObject)
    {
        boolean immutable = sessionPersistedObject.getClass().getAnnotation(ImmutableSessionPersistedObject.class) != null;

        // Immutable objects are always clean, others are assumed dirty.
        // Go implement OptimizedSessionPersistedObject if you don't like it.

        return !immutable;
    }
}
