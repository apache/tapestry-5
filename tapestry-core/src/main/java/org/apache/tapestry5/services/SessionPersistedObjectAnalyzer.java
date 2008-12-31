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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Analyzes a session-persisted object, specifically to see if it is dirty or not.  The service implementation uses a
 * mapped configuration to form a {@linkplain org.apache.tapestry5.ioc.services.StrategyBuilder strategy} based on
 * object type. The service is injectable using the {@link org.apache.tapestry5.ioc.annotations.Primary} marker
 * annotation.
 *
 * @see org.apache.tapestry5.annotations.ImmutableSessionPersistedObject
 * @see org.apache.tapestry5.OptimizedSessionPersistedObject
 * @since 5.1.0.0
 */
@UsesMappedConfiguration(key = Class.class, value = SessionPersistedObjectAnalyzer.class)
public interface SessionPersistedObjectAnalyzer<T>
{
    /**
     * Passed an object (never null) to see if it is dirty or not. Dirty objects that are stored in the session are
     * re-stored into the session at the end of the request.
     *
     * @param object
     * @return true if object needs to be re-stored into the session
     */
    boolean isDirty(T object);
}
