package org.apache.tapestry5.ioc.services.metrics;

import java.util.List;

/**
 * Central hub for creating or obtaining {@link Metric}s.
 *
 * @since 5.4
 */
public interface MetricCollector
{
    /**
     * Creates a root metric, or returns an existing one.
     *
     * @param name
     *         name of the metric to create
     * @param type
     *         used when creating the metric
     * @param units
     *         used when creating the metric
     * @return the metric
     * @throws IllegalMonitorStateException
     *         if an existing root metric with that name is present, but does
     *         not have the matching type and units.
     */
    Metric createRootMetric(String name, Metric.Type type, Metric.Units units);

    /**
     * Returns root metrics, sorted by name.
     */
    List<Metric> getRootMetrics();
}
