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

package org.apache.tapestry5.ioc.internal.services.cron;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.services.ParallelExecutor;
import org.apache.tapestry5.ioc.services.RegistryShutdownHub;
import org.apache.tapestry5.ioc.services.cron.PeriodicExecutor;
import org.apache.tapestry5.ioc.services.cron.PeriodicJob;
import org.apache.tapestry5.ioc.services.cron.Schedule;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PeriodicExecutorImpl implements PeriodicExecutor, Runnable
{
    private final ParallelExecutor parallelExecutor;

    private final Logger logger;

    // Synchronized by jobLock
    private final List<Job> jobs = CollectionFactory.newList();

    private final Thread thread = new Thread(this, "Tapestry PeriodicExecutor");

    private transient boolean shutdown;

    private static final long FIVE_MINUTES = 5 * 60 * 1000;

    private final AtomicInteger jobIdAllocator = new AtomicInteger();
    
    private final AtomicBoolean started = new AtomicBoolean();

    private final Lock jobLock = new ReentrantLock();

    private class Job implements PeriodicJob, Invokable<Void>
    {
        final int jobId = jobIdAllocator.incrementAndGet();

        private final Schedule schedule;

        private final String name;

        private final Runnable runnableJob;

        private boolean executing, canceled;

        private long nextExecution;

        public Job(Schedule schedule, String name, Runnable runnableJob)
        {
            this.schedule = schedule;
            this.name = name;
            this.runnableJob = runnableJob;

            nextExecution = schedule.firstExecution();
        }

        @Override
        public String getName()
        {
            return name;
        }

        public long getNextExecution()
        {
            try
            {
                jobLock.lock();
                return nextExecution;
            } finally
            {
                jobLock.unlock();
            }
        }


        @Override
        public boolean isExecuting()
        {
            try
            {
                jobLock.lock();
                return executing;
            } finally
            {
                jobLock.unlock();
            }
        }

        @Override
        public boolean isCanceled()
        {
            try
            {
                jobLock.lock();
                return canceled;
            } finally
            {
                jobLock.unlock();
            }
        }

        @Override
        public void cancel()
        {
            try
            {
                jobLock.lock();

                canceled = true;

                if (!executing)
                {
                    removeJob(this);
                }

                // Otherwise, it will be caught when the job finishes execution.
            } finally
            {
                jobLock.unlock();
            }
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder("PeriodicJob[#").append(jobId);

            builder.append(", (").append(name).append(')');

            if (executing)
            {
                builder.append(", executing");
            }

            if (canceled)
            {
                builder.append(", canceled");
            } else
            {
                builder.append(String.format(", next execution %Tk:%<TM:%<TS+%<TL", nextExecution));
            }

            return builder.append(']').toString();
        }

        /**
         * Starts execution of the job; this sets the executing flag, calculates the next execution time,
         * and uses the ParallelExecutor to run the job.
         */
        void start()
        {
            try
            {
                jobLock.lock();
                executing = true;

                // This is a bit naive; it assumes there will not be a delay waiting to execute. There's a lot of options
                // here, such as basing the next execution on the actual start time, or event actual completion time, or allowing
                // overlapping executions of the Job on a more rigid schedule.  Use Quartz.

                nextExecution = schedule.nextExecution(nextExecution);

                parallelExecutor.invoke(this);
            } finally
            {
                jobLock.unlock();
            }

            if (logger.isTraceEnabled())
            {
                logger.trace(this + " sent for execution");
            }
        }

        void cleanupAfterExecution()
        {
            try
            {
                if (logger.isTraceEnabled())
                {
                    logger.trace(this + " execution complete");
                }

                executing = false;

                if (canceled)
                {
                    removeJob(this);
                } else
                {
                    // Again, naive but necessary.
                    thread.interrupt();
                }
            } finally
            {
                jobLock.unlock();
            }
        }

        @Override
        public Void invoke()
        {
            logger.debug("Executing job #{} ({})", jobId, name);

            try
            {
                runnableJob.run();
            } finally
            {
                cleanupAfterExecution();
            }

            return null;
        }

    }

    public PeriodicExecutorImpl(ParallelExecutor parallelExecutor, Logger logger)
    {
        this.parallelExecutor = parallelExecutor;
        this.logger = logger;
    }

    @PostInjection
    public void start(RegistryShutdownHub hub)
    {
        hub.addRegistryShutdownListener(new Runnable()
        {
            @Override
            public void run()
            {
                registryDidShutdown();
            }
        });

    }

    public void init()
    {
        if (!started.get())
        {
            started.set(true);
            thread.start();
        }
    }

    void removeJob(Job job)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Removing " + job);
        }

        try
        {
            jobLock.lock();
            jobs.remove(job);
        } finally
        {
            jobLock.unlock();
        }
    }


    @Override
    public PeriodicJob addJob(Schedule schedule, String name, Runnable job)
    {
        assert schedule != null;
        assert name != null;
        assert job != null;

        Job periodicJob = new Job(schedule, name, job);

        try
        {
            jobLock.lock();

            jobs.add(periodicJob);
        } finally
        {
            jobLock.unlock();
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Added " + periodicJob);
        }

        // Wake the thread so that it can start the job, if necessary.

        // Technically, this is only necessary if the new job is scheduled earlier
        // than any job currently in the list of jobs, but this naive implementation
        // is simpler.
        thread.interrupt();

        return periodicJob;
    }

    @Override
    public void run()
    {
        while (!shutdown)
        {
            long nextExecution = executeCurrentBatch();

            try
            {
                long delay = nextExecution - System.currentTimeMillis();

                if (logger.isTraceEnabled())
                {
                    logger.trace(String.format("Sleeping for %,d ms", delay));
                }

                if (delay > 0)
                {
                    Thread.sleep(delay);
                }
            } catch (InterruptedException
                    ex)
            {
                // Ignored; the thread is interrupted() to shut it down,
                // or to have it execute a new batch.

                logger.trace("Interrupted");
            }
        }
    }

    private void registryDidShutdown()
    {
        shutdown = true;

        thread.interrupt();
    }

    /**
     * Finds jobs and executes jobs that are ready to be executed.
     *
     * @return the next execution time (from the non-executing job that is scheduled earliest for execution).
     */
    private long executeCurrentBatch()
    {
        long now = System.currentTimeMillis();
        long nextExecution = now + FIVE_MINUTES;

        try
        {
            jobLock.lock();
            // TAP5-2455
            Set<Job> jobsToCancel = null;

            for (Job job : jobs)
            {
                if (job.isExecuting())
                {
                    continue;
                }

                long jobNextExecution = job.getNextExecution();

                if (jobNextExecution <= 0)
                {
                    if (jobsToCancel == null)
                    {
                        jobsToCancel = new HashSet<PeriodicExecutorImpl.Job>();
                    }
                    jobsToCancel.add(job);
                } else if (jobNextExecution <= now)
                {
                    job.start();
                } else
                {
                    nextExecution = Math.min(nextExecution, jobNextExecution);
                }
            }
            if (jobsToCancel != null)
            {
                for (Job job : jobsToCancel)
                {
                    job.cancel();
                }
            }
        } finally
        {
            jobLock.unlock();
        }

        return nextExecution;
    }


}
