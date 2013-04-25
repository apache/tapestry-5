// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services.metrics;

import org.apache.tapestry5.func.F;
import org.apache.tapestry5.ioc.annotations.PostInjection;
import org.apache.tapestry5.ioc.annotations.PreventServiceDecoration;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.internal.util.LockSupport;
import org.apache.tapestry5.ioc.services.cron.IntervalSchedule;
import org.apache.tapestry5.ioc.services.cron.PeriodicExecutor;
import org.apache.tapestry5.ioc.services.metrics.Metric;
import org.apache.tapestry5.ioc.services.metrics.MetricCollector;
import org.apache.tapestry5.ioc.services.metrics.MetricsSymbols;
import org.apache.tapestry5.ioc.util.ExceptionUtils;
import org.rrd4j.ConsolFun;
import org.rrd4j.DsType;
import org.rrd4j.core.*;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@PreventServiceDecoration
public class MetricCollectorImpl extends LockSupport implements MetricCollector, Runnable
{
    private final boolean inMemory;

    private final RrdBackendFactory factory;

    private final Logger logger;

    private final String dbDir;

    private final List<Runnable> updates = CollectionFactory.newThreadSafeList();

    public static final int HEARTBEAT = 10; // seconds

    public static final String DS_NAME = "data";

    public class MetricImpl extends LockSupport implements Metric, Comparable<MetricImpl>, Runnable
    {
        private final MetricImpl parent;

        private final Map<String, Metric> children = new HashMap<String, Metric>();

        private final String name;

        private final String path;

        private final Type type;

        private final Units units;

        private final RrdDb db;

        // TODO: May want to initialize this from stored data for Type.TOTAL

        private final AtomicReference<Double> accumulator = new AtomicReference<Double>(0d);

        MetricImpl(MetricImpl parent, String name, Type type, Units units)
        {
            this.name = name;
            assert InternalUtils.isNonBlank(name);
            assert type != null;
            assert units != null;

            this.parent = parent;

            // Parent may be null for a root Metric
            this.path = parent == null ? name : parent.getPath() + "/" + name;

            this.type = type;
            this.units = units;

            try
            {
                this.db = createDb();
            } catch (IOException ex)
            {
                throw new RuntimeException(String.format("Unable to create RrdDb for '%s': %s",
                        path,
                        ExceptionUtils.toMessage(ex)), ex);
            }

            updates.add(this);
        }

        private RrdDb createDb() throws IOException
        {

            if (inMemory)
            {
                return new RrdDb(createDef(path), factory);
            }

            // TODO: If we want to support other options, such as Mongo or Berkley, we'll need
            // to abstract the RRDb factory a bit further!

            String filePath = dbDir + "/" + path + ".rrdb";

            File dbFile = new File(filePath);
            dbFile.getParentFile().mkdirs();

            if (dbFile.exists())
            {
                return new RrdDb(filePath, factory);
            }

            return new RrdDb(createDef(filePath), factory);
        }

        private RrdDef createDef(String filePath)
        {
            RrdDef result = new RrdDef(filePath, HEARTBEAT);

            result.addDatasource(DS_NAME, DsType.COUNTER, HEARTBEAT, 0, Double.NaN);
            // One archive: average for each new data point, 10 minutes worth.
            result.addArchive(new ArcDef(ConsolFun.AVERAGE, .5, 1, 60));

            return result;
        }

        public int compareTo(MetricImpl o)
        {
            return this.name.compareTo(o.name);
        }

        public Metric createChild(String name)
        {
            assert InternalUtils.isNonBlank(name);

            try
            {
                acquireReadLock();

                Metric child = children.get(name);

                if (child == null)
                {
                    try
                    {
                        upgradeReadLockToWriteLock();

                        // Could be a race ...

                        child = children.get(name);

                        if (child == null)
                        {
                            child = new MetricImpl(this, name, type, units);

                            children.put(name, child);
                        }
                    } finally
                    {
                        downgradeWriteLockToReadLock();
                    }
                }

                return child;

            } finally
            {
                releaseReadLock();
            }
        }

        public String getPath()
        {
            return path;
        }

        public Type getType()
        {
            return type;
        }

        public Units getUnits()
        {
            return units;
        }

        public void increment()
        {
            accumulate(1);
        }

        public void accumulate(double value)
        {
            while (true)
            {
                Double current = accumulator.get();
                Double updated = current + value;

                // This is where an Atomic is better than a simple volatile, we can detect
                // when a race condition would have caused the loss of data by overlapping
                // read-and-increment operations. Still miss Clojure's approach, of course.

                if (accumulator.compareAndSet(current, updated))
                {
                    break;
                }
            }

            if (parent != null)
            {
                parent.accumulate(value);
            }
        }

        public List<Metric> getChildren()
        {
            try
            {
                acquireReadLock();

                return F.flow(children.values()).sort().toList();
            } finally
            {
                releaseReadLock();
            }
        }

        public Date getLastUpdateTime()
        {
            try
            {
                return new Date(db.getLastUpdateTime());
            } catch (IOException ex)
            {
                return null;
            }
        }

        public void run()
        {
            try
            {
                db.createSample().setValue(DS_NAME, accumulator.getAndSet(0)).update();
            } catch (IOException ex)
            {
                logger.error(String.format("Unable to update database for metric '%s': %s",
                        path,
                        ExceptionUtils.toMessage(ex)), ex);
            }
        }
    }

    private final Map<String, Metric> rootMetrics = new HashMap<String, Metric>();

    public MetricCollectorImpl(Logger logger, @Symbol(MetricsSymbols.RRD_DB_DIR) String dbDir)
    {
        this.logger = logger;
        this.dbDir = dbDir;

        inMemory = dbDir.equals("");

        factory = inMemory ? new RrdMemoryBackendFactory() : new RrdNioBackendFactory();

        logger.info(String.format("Collecting metrics %s.",
                inMemory ? "in memory" : (" to" + dbDir)));
    }


    public Metric createRootMetric(String name, Metric.Type type, Metric.Units units)
    {

        try
        {
            acquireReadLock();

            Metric result = rootMetrics.get(name);

            if (result == null)
            {
                try
                {
                    upgradeReadLockToWriteLock();

                    // There's a window where another thread may create the metric instead.

                    result = rootMetrics.get(name);

                    // But in the normal case, that won't happen and this thread has the exclusive
                    // write lock to create and cache the new metric.
                    if (result == null)
                    {

                        result = new MetricImpl(null, name, type, units);

                        rootMetrics.put(name, result);
                    }
                } finally
                {
                    downgradeWriteLockToReadLock();
                }
            }

            if (result.getType() != type || result.getUnits() != units)
            {
                throw new IllegalArgumentException(String.format("Metric %s already exists and is type %s, units %s.",
                        result.getPath(),
                        result.getType().name(),
                        result.getUnits().name()));
            }

            return result;

        } finally
        {
            releaseReadLock();
        }

    }

    /**
     * Invoked every few seconds to make all the active metrics update their dbs.
     */
    public void run()
    {
        for (Runnable r : updates)
        {
            r.run();
        }
    }

    @PostInjection
    public void activatePeriodicUpdates(PeriodicExecutor executor)
    {
        executor.addJob(new IntervalSchedule(HEARTBEAT * 1000),
                "UpdateMetrics", this);
    }

    public List<Metric> getRootMetrics()
    {
        return F.flow(rootMetrics.values()).sort().toList();
    }
}
