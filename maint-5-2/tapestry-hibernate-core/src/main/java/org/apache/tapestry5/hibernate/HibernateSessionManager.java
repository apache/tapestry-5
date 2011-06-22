// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.hibernate;

import org.hibernate.Session;

/**
 * Manages the Hibernate session for the current thread. This includes creating the session as needed, allowing the
 * session to checkpoint (commit the current transaction and continue) and commit the transaction automatically at the
 * end of the request.
 * <p/>
 * Remember that in Tapestry, action requests and render requests are entirely separate, and you will see a separate
 * request and a separate transaction for each. Care should be taken to ensure that entity objects that are retained (in
 * the session, as persistent field values) between requests are handled correctly (they tend to become detached
 * instances).
 * <p/>
 * This implementation of this service is per-thread.
 */
public interface HibernateSessionManager
{
    /**
     * Gets the active session for this request, creating it as necessary. When the session is first created, a
     * transaction is started.
     *
     * @return the request's session
     * @see HibernateSessionSource
     */
    Session getSession();

    /**
     * Commits the current transaction (which will cause a flush of data to the database), then starts a new transaction
     * to replace it.
     */
    void commit();

    /**
     * Aborts the current transaction, and starts a new transaction to replace it.
     */
    void abort();
}
