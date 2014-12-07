//
// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.transform;

import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.OperationTracker;
import org.apache.tapestry5.runtime.ComponentEvent;

/**
 * Used  to encapsulate the list of {@link EventHandlerMethodParameterProvider}s for a particular
 * method of a particular component, providing {@link OperationTracker} behavior as parameter values
 * are obtained/computed/coerced.
 *
 * @since 5.3
 */
public class EventHandlerMethodParameterSource
{
    private static final class ParameterExtractor implements Invokable<Object> {
        private final EventHandlerMethodParameterProvider[] providers;
        private final int index;
        private final ComponentEvent event;

        private ParameterExtractor(EventHandlerMethodParameterProvider[] providers, int index, ComponentEvent event) {
            this.providers = providers;
            this.index = index;
            this.event = event;
        }

        public Object invoke()
        {
            return providers[index].valueForEventHandlerMethodParameter(event);
        }
    }

    private final String methodIdentifier;

    private final OperationTracker operationTracker;

    private final EventHandlerMethodParameterProvider[] providers;

    public EventHandlerMethodParameterSource(String methodIdentifier, OperationTracker operationTracker, EventHandlerMethodParameterProvider[] providers)
    {

        this.methodIdentifier = methodIdentifier;
        this.operationTracker = operationTracker;
        this.providers = providers;
    }

    public Object get(final ComponentEvent event, final int index)
    {
        // Hopefully this will not be too much overhead; it's really nice to be able to track what parameter
        // caused a failure.

        return operationTracker.invoke("Obtaining value for parameter #" + (index + 1) + " of "+ methodIdentifier,
                new ParameterExtractor(providers, index, event));
    }
}
