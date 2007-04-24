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

package org.apache.tapestry.ioc.services;

/**
 * Event hub used to identify when the end of thread cleanup (i.e., end of request cleanup in a
 * typical web application) should occur. Tapestry IoC has any number of objects that need to know
 * when this event occurs, so that they can clean up any per-thread/per-request state.
 * 
 * 
 */
public interface ThreadCleanupHub
{
    /**
     * Adds a listener to the hub. The hub maintains a seperate list of listeners for each thread
     * (i.e., using a ThreadLocal). Further, the listener list is discarded at the end of the
     * request.
     * 
     * @param listener
     *            to add
     */
    void addThreadCleanupListener(ThreadCleanupListener listener);
}
