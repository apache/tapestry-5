// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.services.cron;

import org.apache.tapestry5.ioc.internal.services.cron.CronExpression;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

public class CronSchedule implements Schedule
{
    private CronExpression cron;

    public CronSchedule(String cronExpression)
    {
        this(cronExpression, TimeZone.getDefault());
    }

    public CronSchedule(String cronExpression, TimeZone timeZone)
    {
        try
        {
            this.cron = new CronExpression(cronExpression);
            this.cron.setTimeZone(timeZone);
        } catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long firstExecution()
    {
        return getNextValidTimeAfterNow();
    }

    @Override
    public long nextExecution(long previousExecution)
    {
        return getNextValidTimeAfterNow();
    }

    private long getNextValidTimeAfterNow()
    {
        final Date time = cron.getNextValidTimeAfter(new Date());

        return time == null ? 0 : time.getTime();
    }
}
