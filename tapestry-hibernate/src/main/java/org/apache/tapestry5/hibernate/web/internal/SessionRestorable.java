// Copyright 2014 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.hibernate.web.internal;

import org.hibernate.Session;

import java.io.Serializable;

/**
 * Interface for serializable objects stored in the HTTP Session that can be restored to active state via
 * the Hibernate {@linkplain org.hibernate.Session}.
 *
 * @since 5.4
 */
public interface SessionRestorable extends Serializable
{
    Object restoreWithSession(Session session);
}
