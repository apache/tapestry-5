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

package org.apache.tapestry.internal.hibernate;

import org.apache.tapestry.hibernate.HibernateSessionManager;
import org.apache.tapestry.hibernate.HibernateSessionSource;
import org.apache.tapestry.ioc.services.ThreadCleanupListener;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class HibernateSessionManagerImpl implements HibernateSessionManager, ThreadCleanupListener
{
    private final Session _session;

    private Transaction _transaction;

    public HibernateSessionManagerImpl(HibernateSessionSource source)
    {
        _session = source.create();

        _transaction = _session.beginTransaction();
    }

    public void abort()
    {
        _transaction.rollback();
        _transaction.begin();
    }

    public void commit()
    {
        _transaction.commit();
        _transaction.begin();
    }

    public Session getSession()
    {
        return _session;
    }

    /**
     * Rollsback the transaction at the end of the request, then closes the session. This means that any uncommitted
     * changes are lost; code should inject the HSM and invoke {@link #commit()} after making any changes, if they
     * should persist.
     */
    public void threadDidCleanup()
    {
        _transaction.rollback();

        _session.close();
    }

}
