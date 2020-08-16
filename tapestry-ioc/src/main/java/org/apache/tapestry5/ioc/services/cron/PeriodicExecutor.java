// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services.cron;

/**
 * A service that executes a job at intervals specified by a {@link Schedule}.
 *
 * @since 5.3
 */
public interface PeriodicExecutor
{
    /**
     * Adds a job to be executed. The job is executed in a thread pool (via {@link org.apache.tapestry5.ioc.services.ParallelExecutor#invoke(org.apache.tapestry5.ioc.Invokable)}), as determined by the schedule.
     *
     * @param schedule defines when the job will next execute
     * @param name     a name used in debugging output related to the job
     * @param job      a Runnable object that represents the work to be done
     * @return a PeriodicJob that can be used to query when the job executes, or to cancel its execution
     */
    PeriodicJob addJob(Schedule schedule, String name, Runnable job);
    
    /**
     * Initializes this service. <em>Never call this method direclty. It's intended
     * for internal Tapestry-IoC usage only</em>.
     */
    public void init();
}
