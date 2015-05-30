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
 * Used with {@link PeriodicExecutor} to control the schedule for when jobs execute.
 */
public interface Schedule
{
    /**
     * For a newly created job, what is the start time for the job.  Often, the current clock time is returned, to start
     * a job as soon as possible.
     *
     * @return start time for new job, in system clock millis
     */
    long firstExecution();

    /**
     * Computes the next execution time for a job.
     *
     * @param previousExecution time of previous execution (in system clock millis)
     * @return time of next execution (in system clock millis) or a value &lt;= 0 to cancel the job's execution
     */
    long nextExecution(long previousExecution);
}
