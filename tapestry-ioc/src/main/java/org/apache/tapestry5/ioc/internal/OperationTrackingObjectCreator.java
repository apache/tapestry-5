//  Copyright 2008,, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.ioc.Invokable;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.OperationTracker;

/**
 * Makes sure the operations tracker is notified knows that a service is being realized.
 */
public class OperationTrackingObjectCreator implements ObjectCreator
{
    private final OperationTracker tracker;

    private final String message;

    private final ObjectCreator delegate;

    public OperationTrackingObjectCreator(OperationTracker tracker, String message, ObjectCreator delegate)
    {
        this.tracker = tracker;
        this.message = message;
        this.delegate = delegate;
    }

    public Object createObject()
    {
        Invokable<Object> operation = new Invokable<Object>()
        {
            public Object invoke()
            {
                return delegate.createObject();
            }
        };

        return tracker.invoke(message, operation);
    }
}
