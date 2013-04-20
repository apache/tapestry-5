package org.apache.tapestry5.ioc.services.metrics;

import java.util.List;

public interface MetricCollector
{
    /**
     * Creates a root metric, or returns an existing one.
     *
     * @param name
     * @param type
     * @param units
     * @return the metric
     * @throws IllegalMonitorStateException
     *         if an existing metric is present, but does
     *         not have the matching type and units.
     */
    Metric createRootMetric(String name, Metric.Type type, Metric.Units units);

    /**
     * Returns root metrics, sorted by name.
     */
    List<Metric> getRootMetrics();
}
