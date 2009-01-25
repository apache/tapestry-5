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

package org.apache.tapestry5.hibernate.annotations;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

/**
 * Marks a method of a service (or a component method) as transactional: the active transaction should {@linkplain
 * org.apache.tapestry5.hibernate.HibernateSessionManager#commit() commit} after invoking the method.  Runtime
 * exceptions will abort the transaction, checked exceptions will <em>also commit</em> the transaction.
 *
 * @see org.apache.tapestry5.hibernate.HibernateTransactionAdvisor
 * @see org.apache.tapestry5.hibernate.HibernateTransactionDecorator
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface CommitAfter
{

}
