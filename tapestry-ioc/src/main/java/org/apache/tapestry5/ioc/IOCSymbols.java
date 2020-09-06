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

package org.apache.tapestry5.ioc;

/**
 * Configuration symbols used by the IoC container.
 * 
 * @since 5.2.2
 */
public class IOCSymbols
{
    /**
     * The minimum size of the thread pool. The default is 3. When a task is created and there are fewer
     * than this number of threads in the pool, a new thread is created for the task.
     */
    public static final String THREAD_POOL_CORE_SIZE = "tapestry.thread-pool.core-pool-size";

    /**
     * The size of the task queue. When there are at least {@linkplain #THREAD_POOL_CORE_SIZE the core number} of
     * threads in the pool, tasks will be placed in the queue. If the queue is empty, more threads
     * may be created (up to the {@linkplain #THREAD_POOL_MAX_SIZE maximum pool size}). If the queue is full and
     * all threads have been created, the task is rejected.
     *
     * The default is 100.
     * 
     * @since 5.3
     * @see <a href="http://www.bigsoft.co.uk/blog/index.php/2009/11/27/rules-of-a-threadpoolexecutor-pool-size">
     *     Rules of a ThreadPoolExecutor pool size
     *     </a>
     */
    public static final String THREAD_POOL_QUEUE_SIZE = "tapestry.thread-pool.queue-size";

    /**
     * Maximium size of the thread pool, which defaults to 10.
     */
    public static final String THREAD_POOL_MAX_SIZE = "tapestry.thread-pool.max-pool-size";

    /**
     * Time in milliseconds (via {@link org.apache.tapestry5.commons.util.TimeInterval}) to keep waiting threads alive.
     * Default is one minute.
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
