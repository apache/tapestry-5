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

package org.apache.tapestry5.ioc.services.metrics;

import java.util.Date;
import java.util.List;

/**
 * @since 5.4
 */
public interface Metric
{
    enum Type
    {
        /**
         * Storing a value accumulates into an ever increasing total. Example:
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
        COUNTER
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
     * Accumulates a value of 1; useful  when the metric's type is {@link Units#COUNTER}.
     */
    void increment();

    /**
     * Adds the provided value to the current time interval's value. This may be called multiple times during
     * a time interval, and the values will accumulate. In addition, {@link #accumulate(double)}
     * propagates the value up to the parent metrics, if any, all the way up to the root metric.
     *
     * @param value
     */
    void accumulate(double value);

    /**
     * Returns the children of this metric, sorted by name.
     */
    List<Metric> getChildren();

    /**
     * Returns the last time the archive was updated (this happens a regular intervals called heartbeats).
     *
     * @return archive update time, or null if there is an exception accessing the time
     */
    Date getLastUpdateTime();
}
