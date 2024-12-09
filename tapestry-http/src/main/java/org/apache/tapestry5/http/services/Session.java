// Copyright 2006, 2008, 2020 The Apache Software Foundation
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

package org.apache.tapestry5.http.services;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.tapestry5.http.OptimizedSessionPersistedObject;
import org.apache.tapestry5.http.annotations.ImmutableSessionPersistedObject;
import org.apache.tapestry5.http.internal.services.OptimizedSessionPersistedObjectAnalyzer;


/**
 * Generic version of {@link HttpSession}, used to bridge the gaps between the Servlet API and the Portlet API.
 */
public interface Session
{

    /**
     * The type of lock used to access atttributes in the {@link Session}.
     * <p>
     * The actual lock-type depends on if a lock is already held by the this thread.
     *
     * @since 5.9
     */
    public enum LockMode {
        /**
         * No lock is supposed to be acquired.
         */
        NONE,

        /**
         * Acquire a shared read-lock.
         */
        READ,

        /**
         * Acquire an exclusive write-lock.
         */
        WRITE
    }

    /**
     * Returns a list of the names of all attributes stored in the session.
     * <p>
     * The names are returned sorted alphabetically.
     * <p>
     * By default, a {@code READ} lock is requested.
     */
    List<String> getAttributeNames();

    /**
     * Returns a list of the names of all attributes stored in the session.
     * <p>
     * Uses the requested {@link LockMode} to acquire an appropiate lock.
     *
     * @param lockMode The requested minimum lock mode. If null, {@code READ} is used.
     * @return Alphabetically sorted list of all attributes
     *
     * @since 5.9
     */
    List<String> getAttributeNames(LockMode lockMode);

    /**
     * Returns a list of the names of all attributes stored in the session whose name has the provided prefix.
     * <p>
     * By default, a {@code READ} lock is requested.
     * 
     * @param prefix The attribute prefix
     * @throws NullPointerException if prefix is {@code null}
     * @return Alphabetically sorted list of attributes matching the prefix
     */
    List<String> getAttributeNames(String prefix);

    /**
     * Returns a list of the names of all attributes stored in the session whose name has the
     * provided prefix.
     * <p>
     * Uses the requested {@link LockMode} to acquire an appropriate lock.
     *
     * @param prefix The attribute prefix
     * @throws NullPointerException if prefix is {@code null}
     * @return Alphabetically sorted list of attributes matching the prefix
     * 
     * @since 5.9
     */
    List<String> getAttributeNames(String prefix, Session.LockMode lockMode);

    /**
     * Returns the value previously stored in the session.
     * <p>
     * By default, a {@code WRITE} lock is requested.
     * 
     * @param name The name of the attribute
     * @throws NullPointerException if name is {@code null}
     */
    Object getAttribute(String name);

    /**
     * Returns the value previously stored in the session.
     * <p>
     * Uses the requested {@link LockMode} to acquire an appropriate lock.
     * 
     * @param name The name of the attribute
     * @throws NullPointerException if name is {@code null}
     *
     * @since 5.9
     */
    Object getAttribute(String name, Session.LockMode lockMode);

    /**
     * Sets the value of an attribute. If the value is {@code null}, then the attribute is deleted.
     *
     * @param name The name of the attribute
     * @param value The new value of the attribute; {@code null} deletes the attribute.
     * @throws NullPointerException if name is {@code null}
     */
    void setAttribute(String name, Object value);

    /**
     * Checks if the a value is stored in the session with the specified name.
     * <p>
     * By default, a {@code READ} lock is requested.
     *
     * @param name The name of the attribute
     * @throws NullPointerException if name is {@code null}
     *
     * @since 5.9
     */
    boolean containsAttribute(String name);

    /**
     * Checks if the a value is stored in the session with the specified name.
     * <p>
     * Uses the requested {@link LockMode} to acquire an appropriate lock.
     *
     * @param name The name of the attribute
     * @throws NullPointerException if name is {@code null}
     *
     * @since 5.9
     */
    boolean containsAttribute(String name, Session.LockMode lockMode);

    /**
     * Returns the maximum time interval, in seconds, that the servlet container will keep this
     * session open between client accesses.
     * After this interval, the servlet container will invalidate the session.
     * <p>
     * The maximum time interval can be set with the setMaxInactiveInterval method.
     * <p>
     * A negative time indicates the session should never timeout.
     */
    int getMaxInactiveInterval();

    /**
     * Specifies the time, in seconds, between client requests before the servlet container will
     * invalidate this
     * session.
     * <p>
     * A negative time indicates the session should never timeout.
     */
    void setMaxInactiveInterval(int seconds);

    /**
     * Invalidates this session then unbinds any objects bound to it.
     *
     * @throws IllegalStateException
     *             if this method is called on an already invalidated session
     */
    void invalidate();

    /**
     * Checks to see if the session has been invalidated. Note: since 5.3 this will also catch calls to
     * {@link javax.servlet.http.HttpSession#invalidate()}.
     * 
     * @since 5.1.0.0
     */
    boolean isInvalidated();

    /**
     * Re-stores dirty objects back into the session. This is necessary to support clustering, because (in most
     * application servers) session objects are only broadcast around the cluster from setAttribute(). If a mutable
     * session object is read and changed, those changes will be limited to a single server in the cluster, which can
     * cause confusing application failures in the event of a failover. Does nothing if there are no changes, or
     * the session has been invalidated.
     *
     * @see OptimizedSessionPersistedObject
     * @see OptimizedSessionPersistedObjectAnalyzer
     * @see ImmutableSessionPersistedObject
     * @since 5.1.0.0
     */
    void restoreDirtyObjects();
}
