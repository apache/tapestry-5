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
 * @since 5.3
 */
public interface PeriodicJob
{
    /**
     * Returns the name for the job, supplied when the job is created; this is not unique or meaningful, and
     * primarily exists to assist with debugging.
     *
     * @return name provided for the job
     */
    String getName();

    /**
     * Is this Job currently executing (or queued, awaiting execution)?
     *
     * @return true if executing
     */
    boolean isExecuting();

    /**
     * Has this job been canceled.
     */
    boolean isCanceled();

    /**
     * Cancels the job. If currently executing, the Job will finish (this includes awaiting execution). If not currently
     * executing, the job is discarded immediately.
     */
    void cancel();
}
