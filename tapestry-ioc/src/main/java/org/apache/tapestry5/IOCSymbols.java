// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5;

/**
 * Configuration symbols used by the IoC container.
 *
 * @since 5.1.0.1
 */
public class IOCSymbols
{
    /**
     * The minimum size of the thread pool. The default is 3.
     */
    public static final String THREAD_POOL_CORE_SIZE = "tapestry.thread-pool.core-pool-size";

    /**
     * Maximium size of the pool before submitted invocations must wait to execute; the default is 20.
     */
    public static final String THREAD_POOL_MAX_SIZE = "tapestry.thread-pool.max-pool-size";

    /**
     * Time in milliseconds (via {@link org.apache.tapestry5.ioc.util.TimeInterval}) to keep waiting threads alive.
     * Default is one minute (an epoch in application time).
     */
    public static final String THREAD_POOL_KEEP_ALIVE = "tapestry.thread-pool.keep-alive";

    /**
     * By default, the {@link org.apache.tapestry5.ioc.services.ParallelExecutor} service uses a thread pool. In
     * environments (such as Google Application Engine) where thread creation is not allowed, this can be set to
     * "false", and deferred logic will, instead, execute immediately.
     *
     * @since 5.1.0.3
     */
    public static final String THREAD_POOL_ENABLED = "tapestry.thread-pool-enabled";
}
