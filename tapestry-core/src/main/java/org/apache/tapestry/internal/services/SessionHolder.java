// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.services.Session;

public interface SessionHolder
{
    /**
     * Gets the {@link Session}. If create is false and the session has not be created
     * previously, returns null.
     * 
     * @param create
     *            true to force the creation of the session
     * @return the session (or null if create is false the session has not been previously created)
     */
    Session getSession(boolean create);

}
