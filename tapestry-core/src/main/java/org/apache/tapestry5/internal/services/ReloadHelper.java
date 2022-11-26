// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

/**
 * Forces a reload of all caches and invalidates the component class cache. This is only allowed when production mode is off.
 *
 * @since 5.4
 */
public interface ReloadHelper
{
    /**
     * Force a reload (if in development mode). Writes an {@link org.apache.tapestry5.alerts.AlertManager} alert message.
     */
    void forceReload();

    /**
     * Adds a callback to be invoked from {@link #forceReload()}.
     *
     * @param callback
     */
    void addReloadCallback(Runnable callback);
}
