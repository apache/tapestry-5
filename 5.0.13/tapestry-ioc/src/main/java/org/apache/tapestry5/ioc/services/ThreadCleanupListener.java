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

package org.apache.tapestry5.ioc.services;

import java.util.EventListener;

/**
 * Listener interface for object that need to know about thread event cleanup.
 * <p/>
 * Note that registration with the {@link org.apache.tapestry5.ioc.services.PerthreadManager} is a one-shot affair; it
 * lasts no longer than the next cleanup.
 */
public interface ThreadCleanupListener extends EventListener
{
    /**
     * Invoked by {@link org.apache.tapestry5.ioc.services.PerthreadManager} service when a thread performs and
     * end-of-request cleanup.
     */
    void threadDidCleanup();
}
