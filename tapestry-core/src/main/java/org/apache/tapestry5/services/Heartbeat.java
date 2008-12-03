// Copyright 2006, 2008 The Apache Software Foundation
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
 * Allows for deferred execution of logic, useful when trying to get multiple components to coordinate behavior. A
 * component may add a command to be executed "{@linkplain #end() at the end of the heartbeat}". The classic example of
 * this is a Label and the field it labels; since we don't know which order the two will render, we can't tell if the
 * field's id is correct until after both have rendered.
 * <p/>
 * The Heartbeat is injected into components via the {@link org.apache.tapestry5.annotations.Environmental} annotation.
 */
public interface Heartbeat
{
    /**
     * Begins a new Heartbeat. Heartbeats nest. Every call to begin() should be matched by a call to {@link #end()}.
     */
    void begin();

    /**
     * Executes all commands since the most recent {@link #begin()}.
     */
    void end();

    /**
     * Adds a new command to the current Heartbeat. The command will be executed by {@link #end()}.
     *
     * @param command command to be executed at the end of the heartbeat
     */
    void defer(Runnable command);
}
