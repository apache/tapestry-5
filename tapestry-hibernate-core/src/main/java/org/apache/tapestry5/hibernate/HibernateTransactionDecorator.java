// Copyright 2008, 2009 The Apache Software Foundation
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

/**
 * Service that can create an interceptor that wraps around a service implementation. After invoking service methods
 * marked by {@link org.apache.tapestry5.hibernate.annotations.CommitAfter} the current transaction is committed.
 * Declared exceptions will also {@linkplain org.apache.tapestry5.hibernate.HibernateSessionManager#commit() commit the
 * transaction}; runtime exceptions will {@linkplain org.apache.tapestry5.hibernate.HibernateSessionManager#abort() the
 * transaction}.
 * <p/>
 * It is recommended that you switch from the use of decoration to the use of advice; use the {@link
 * org.apache.tapestry5.hibernate.HibernateTransactionAdvisor} (from a service advisor method) instead.
 */
public interface HibernateTransactionDecorator
{
    /**
     * Builds a transaction interceptor instance around the delegate.
     *
     * @param <T>
     * @param serviceInterface interface implemented by the delegate
     * @param delegate         existing object to be wrapped
     * @param serviceId        id of service
     * @return a new object implementing the interface that can be used in place of the delegate, providing
     *         transactional behavior
     */
    <T> T build(Class<T> serviceInterface, T delegate, String serviceId);
}
