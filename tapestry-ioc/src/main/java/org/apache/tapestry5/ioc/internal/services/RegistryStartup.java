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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.internal.util.OneShotLock;
import org.slf4j.Logger;

import java.util.List;

/**
 * Startup service for Tapestry IoC: automatically invoked at {@linkplain Registry#performRegistryStartup() registry
 * startup} to execute a series of operations, via its ordered configuration of Runnable objects.
 */
public class RegistryStartup implements Runnable
{
    private final Logger logger;

    private final List<Runnable> configuration;

    private final OneShotLock lock = new OneShotLock();

    public RegistryStartup(Logger logger, final List<Runnable> configuration)
    {
        this.logger = logger;
        this.configuration = configuration;
    }

    /**
     * Invokes run() on each contributed object. If the object throws a runtime exception, it is logged but startup
     * continues anyway. This method may only be {@linkplain OneShotLock invoked once}.
     */
    public void run()
    {
        lock.lock();

        // Do we want extra exception catching here?

        for (Runnable r : configuration)
        {
            try
            {
                r.run();
            }
            catch (RuntimeException ex)
            {
                logger.error(ServiceMessages.startupFailure(ex));
            }
        }

        // We don't need them any more since this method can only be run once. It's a insignificant
        // savings, but still a nice thing to do.

        configuration.clear();
    }

}
