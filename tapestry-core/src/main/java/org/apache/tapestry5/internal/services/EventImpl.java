// Copyright 2007, 2008, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ComponentEventCallback;
import org.apache.tapestry5.TapestryMarkers;
import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.runtime.Event;
import org.slf4j.Logger;

@SuppressWarnings("all")
public class EventImpl implements Event
{
    private boolean aborted;

    private String methodDescription;

    private final ComponentEventCallback handler;

    private final Logger logger;

    private final boolean debugEnabled;

    protected final OperationTracker tracker;

    /**
     * @param handler informed of return values from methods, deems when the event is aborted
     * @param logger  used to log method invocations
     * @param tracker
     */
    public EventImpl(ComponentEventCallback handler, Logger logger, OperationTracker tracker)
    {
        this.tracker = tracker;
        assert handler != null;
        this.handler = handler;
        this.logger = logger;

        // TAP5-471: Thousands of calls to isDebugEnabled() do add up
        debugEnabled = logger.isDebugEnabled();
    }

    public boolean isAborted()
    {
        return aborted;
    }

    public void setMethodDescription(String methodDescription)
    {
        if (debugEnabled)
            logger.debug(TapestryMarkers.EVENT_HANDLER_METHOD, "Invoking: " + methodDescription);

        this.methodDescription = methodDescription;
    }

    @SuppressWarnings("unchecked")
    public boolean storeResult(final Object result)
    {
        // Given that this method is *only* invoked from code
        // that is generated at runtime and proven to be correct,
        // this should never, ever happen. But what the hell,
        // let's check anyway.

        if (aborted)
        {
            throw new IllegalStateException(String.format("Can not store result from invoking method %s, because an event result value has already been obtained from some other event handler method.", methodDescription));
        }


        if (result != null)
        {
            boolean handleResult =
                    tracker.invoke("Handling result from method " + methodDescription + '.', new Invokable<Boolean>()
                    {
                        public Boolean invoke()
                        {
                            return handler.handleResult(result);
                        }
                    });

            aborted |= handleResult;
        }

        return aborted;
    }

    protected String getMethodDescription()
    {
        return methodDescription;
    }
}
