//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Arrays;

/**
 * A set of markers used internally by Tapestry when logging in code related to paqes and components. Most logging
 * toolkits, including Log4J, do not incorporate markers, but  <a href="http://logback.qos.ch/">LOGBack</a> does.
 */
public class TapestryMarkers
{
    /**
     * A root marker for all things Tapestry related. The remaining markers are children of the TAPESTRY marker.
     */
    public static final Marker TAPESTRY = MarkerFactory.getMarker("TAPESTRY");

    /**
     * Logs the final version of the class transformation. This is useful when debugging {@link
     * org.apache.tapestry5.services.ComponentClassTransformWorker}s, as it shows exactly what transformation operations
     * occured, at the Java code level.
     */
    public static final Marker CLASS_TRANSFORMATION = MarkerFactory.getMarker("CLASS_TRANSFORMATION");

    /**
     * Marker for a debug log that occurs just before invocation of a event handler method.
     */
    public static final Marker EVENT_HANDLER_METHOD = MarkerFactory.getMarker("EVENT_HANDLER_METHOD");

    /**
     * Marker for logging related to component event dispatch.
     */
    public static final Marker EVENT_DISPATCH = MarkerFactory.getMarker("EVENT_DISPATCH");

    /**
     * Marker for logging, at trace level verbose details about each individual {@link
     * org.apache.tapestry5.runtime.RenderCommand} involved in rendering the page, as well as a final (debug level)
     * summary of command count and elapsed time.
     */

    public static final Marker RENDER_COMMANDS = MarkerFactory.getMarker("RENDER_COMMANDS");

    static
    {
        for (Marker child : Arrays.asList(CLASS_TRANSFORMATION, EVENT_HANDLER_METHOD, EVENT_DISPATCH, RENDER_COMMANDS))
        {
            TAPESTRY.add(child);
        }
    }
}
