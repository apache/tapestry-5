// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.services.PersistentFieldChange;
import org.apache.tapestry5.services.PersistentFieldStrategy;

/**
 * Describes an object that can store {@link PersistentFieldChange}s, and manage a query parameter stored into a {@link
 * Link} to maining this data across requests.
 */
public interface ClientPersistentFieldStorage extends PersistentFieldStrategy
{
    /**
     * Updates a link, adding a query parameter to it (if necessary) to store
     *
     * @param link
     */
    void updateLink(Link link);
}
