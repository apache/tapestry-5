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

package org.apache.tapestry5.http.services;

import org.apache.tapestry5.ioc.annotations.UsesMappedConfiguration;

/**
 * Analyzes a session-persisted object, specifically to see if it is dirty or not.
 *
 * This service is provided to support applications which store mutable session attributes where the
 * session is replicated to a slower medium (e.g. RDMBS, Cluster, etc) this can help alleviate excessive writes
 * to the session store while ensuring changes are propagated.
 *
 * The service implementation uses a mapped configuration to form a
 * {@linkplain org.apache.tapestry5.ioc.services.StrategyBuilder strategy} based on object type. The service may be
 * injected using the {@link org.apache.tapestry5.ioc.annotations.Primary} marker annotation.
 *
 * @see org.apache.tapestry5.http.annotations.ImmutableSessionPersistedObject
 * @see org.apache.tapestry5.http.OptimizedSessionPersistedObject
 * @since 5.1.0.0
 */
@UsesMappedConfiguration(key = Class.class, value = SessionPersistedObjectAnalyzer.class)
public interface SessionPersistedObjectAnalyzer<T>
{
    /**
     * Atomically check and reset the dirty state of the session persisted object.
     *
     * The implementer should take consideration for the fact that session attributes are accessed concurrently. A
     * naive check/set algorithm may allow changes to go un-noticed.
     *
     * @param sessionPersistedObject the session attribute (never null)
     * @return true if the object needs to be re-stored into the session
     * @since 5.3
     */
    boolean checkAndResetDirtyState(T sessionPersistedObject);

}
