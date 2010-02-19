// Copyright 2010 The Apache Software Foundation
//
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

import org.apache.tapestry5.runtime.ComponentEvent;

/**
 * An object that can invoke an event handler method on a component instance.
 * 
 * @since 5.2.0
 */
public interface EventHandlerMethodInvoker
{
    /**
     * The type of event this method handles, i.e., "action".
     */
    String getEventType();

    /**
     * The id of the component this method should be invoked for, or
     * the blank string to ignore component id when matching.
     */
    String getComponentId();

    /**
     * The minimum number of of context values needed. The method
     * should be invoked if there are at least this number of
     * context values.
     */
    int getMinContextValueCount();

    /**
     * Given an event and a component instance, invoke the component event method. The method
     * is passed appropriate parameters. If the invocation throws a checked exception, then
     * the exception is wrapped in a RuntimeException and rethrown.
     */
    void invokeEventHandlerMethod(ComponentEvent event, Object instance);
}
