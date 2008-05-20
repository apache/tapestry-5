// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services;

/**
 * Manages per-thread data, and provides a way for listeners to know when such data should be cleaned up.  Typically,
 * data is cleaned up at the end of the request (in a web application). Tapestry IoC has any number of objects that need
 * to know when this event occurs, so that they can clean up any per-thread/per-request state.
 * <p/>
 * Due to <a href="https://issues.apache.org/jira/browse/TAPESTRY-2141">TAPESTRY-2141<a> (and the underlying JDK 1.5 bug
 * <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5025230">5025230</a>), this service has expanded to
 * manager per-thread data (not just end-of-request listeners).
 */
public interface PerthreadManager
{
    /**
     * Adds a listener to the hub.  All listeners are discarded at the {@link #cleanup()}.
     *
     * @param listener to add
     */
    void addThreadCleanupListener(ThreadCleanupListener listener);

    /**
     * Immediately performs a cleanup of the thread, notifying all listeners then discarding the thread locale and the
     * map it stores.
     */
    void cleanup();


    /**
     * Returns an object stored in the per-thread map.    When the object is a string, the expected name is <em>service
     * id</em>.<em>subkey</em>.  Unlike most of Tapestry, such keys <em>will</em> be case sensitive.
     *
     * @param key key used to retrieve object
     * @return corresponding per-thread object, or null
     */
    Object get(Object key);

    /**
     * Stores a value into the per-thread map.
     */
    void put(Object key, Object value);
}
