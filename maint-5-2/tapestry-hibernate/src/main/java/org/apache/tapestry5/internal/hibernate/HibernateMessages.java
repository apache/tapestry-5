// Copyright 2007, 2008, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.hibernate;

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.internal.util.MessagesImpl;

public class HibernateMessages
{
    private static final Messages MESSAGES = MessagesImpl.forClass(HibernateMessages.class);

    public static String sessionPersistedEntityLoadFailure(String entityName, Object id, Throwable cause)
    {
        return MESSAGES.format("session-persisted-entity-load-failure", entityName, id, cause);
    }

    public static String entityNotAttached(Object entity)
    {
        return MESSAGES.format("entity-not-attached", entity);
    }

    public static String commitTransactionInterceptor(String serviceId, Class serviceInterface)
    {
        return MESSAGES.format("commit-transaction-interceptor", serviceId, serviceInterface.getName());
    }
}
