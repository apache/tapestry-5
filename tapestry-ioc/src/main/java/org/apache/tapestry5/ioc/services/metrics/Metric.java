package org.apache.tapestry5.ioc.services.metrics;

import org.apache.xpath.operations.String;

import java.util.List;

public interface Metric
{
    enum Type
    {
        /**
         * Storing a value accum;ulates into an ever increasing total. Example:
         * number of burgers served.
         */
        TOTAL,

        /**
         * Storing a value accumulates just for the current time interval.  Example:
         * burgers served per second.
         */
        RATE
    }


    /**
     * Primarily used when reporting to indicate the types of values.
     */
    enum Units
    {
        /**
         * Appropriate when measuring bytes transferred, etc.
         */
        BYTES,
        /**
         * Useful when measuring the time to produce a result, such as processing some kind of request.
         */
        MILLISECONDS,
        /**
         * Placeholder for all other kinds of units, such as "pages viewed" or "messages processed".
         */
        UNIT
    }

    /**
     * Creates a child metric with the same type and units.
     * Values posted to the child are also posted to the container (this
     * bubbling up can occur across several levels).
     *
     * @param name
     *         child's extension to this Metric's name
     * @return the child with the given name (creating it if necessary).
     */
    Metric createChild(String name);

    /**
     * Returns the path to this metric: the metric's name appended (after a slash) to the parent
     * metric's path.
     */
    String getPath();

    Type getType();

    Units getUnits();

    /**
     * Stores a value of 1; useful  when the metric's type is {@linkplain Units#UNIT unit}.
     */
    void increment();

    /**
     * Stores a value into the current time interval.
     *
     * @param value
     */
    void store(double value);

    /**
     * Returns the children of this metric, sorted by name.
     */
    List<Metric> getChildren();
}
