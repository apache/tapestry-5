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

package org.apache.tapestry5;

/**
 * An optional interface implemented by objects that are persisted in the {@link org.apache.tapestry5.services.Session}.
 * At the end of each request, any objects read from the session are re-stored into the session, to ensure that
 * in-memory changes are flushed to other servers in a cluster. Objects that implement this interface are expected to
 * track when they are dirty (have pending changes), so that the save back into the session can be avoided when not
 * necessary.
 *
 * @see org.apache.tapestry5.annotations.ImmutableSessionPersistedObject
 * @see org.apache.tapestry5.services.SessionPersistedObjectAnalyzer
 * @since 5.1.1.0
 */
public interface OptimizedSessionPersistedObject
{
    /**
     * Returns true if the object has in-memory changes.  It is the object's responsibility to set its internal flag to
     * false, typically by implementing {@link javax.servlet.http.HttpSessionBindingListener}.
     *
     * @return
     */
    boolean isSessionPersistedObjectDirty();
}
