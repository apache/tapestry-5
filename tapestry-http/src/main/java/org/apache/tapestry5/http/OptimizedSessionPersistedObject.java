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

package org.apache.tapestry5.http;

/**
 * An optional interface implemented by objects that are persisted in the {@link org.apache.tapestry5.http.services.Session}.
 * At the end of each request, any objects read from the session are re-stored into the session, to ensure that
 * in-memory changes are flushed to other persistent session stores (e.g. RDBMS, servers in a cluster, etc). Objects
 * that implement this interface are expected to track when they are dirty (have pending changes), so that the save
 * back into the session can be avoided when not necessary.
 *
 * This method is accessed concurrently.
 *
 * @see org.apache.tapestry5.http.annotations.ImmutableSessionPersistedObject
 * @see org.apache.tapestry5.http.services.SessionPersistedObjectAnalyzer
 * @since 5.1.1.0
 */
public interface OptimizedSessionPersistedObject
{
    /**
     * @return true if the object has in-memory changes since the last time this method was called.
     */
    boolean checkAndResetDirtyMarker();
}
