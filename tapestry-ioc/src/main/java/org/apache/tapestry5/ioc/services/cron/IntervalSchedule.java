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
 * A very simple schedule, that simply executes the desired job at fixed intervals.
 *
 * @since 5.3
 */
public class IntervalSchedule implements Schedule
{
    private final long interval;

    /**
     * Interval at which the schedule should execute jobs. The first execution is delayed from current time
     * by the interval as well.
     *
     * @param interval in milliseconds
     */
    public IntervalSchedule(long interval)
    {
        assert interval > 0;

        this.interval = interval;
    }

    @Override
    public long firstExecution()
    {
        return nextExecution(System.currentTimeMillis());
    }

    @Override
    public long nextExecution(long previousExecution)
    {
        return previousExecution + interval;
    }
}
