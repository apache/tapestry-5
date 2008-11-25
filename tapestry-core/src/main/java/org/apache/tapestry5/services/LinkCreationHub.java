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

package org.apache.tapestry5.services;

/**
 * A service that allows listeners to be registerred to learn about {@link org.apache.tapestry5.Link} creation.
 */
public interface LinkCreationHub
{
    /**
     * Adds a listener. If the scope of the listener is per-thread, then it must be removed.
     *
     * @param listener
     */
    void addListener(LinkCreationListener listener);

    /**
     * Removes a previously added listener.
     *
     * @param listener
     */
    void removeListener(LinkCreationListener listener);
}
