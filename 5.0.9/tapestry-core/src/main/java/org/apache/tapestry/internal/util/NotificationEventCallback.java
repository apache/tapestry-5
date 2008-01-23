// Copyright 2006, 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.util;

import org.apache.tapestry.ComponentEventCallback;
import org.apache.tapestry.runtime.Component;

/**
 * A {@link org.apache.tapestry.ComponentEventCallback} used for notification events. Event handler methods may return
 * true (to abort the event) or false (to allow the event to continue bubbling up), but all other values are forbidden.
 */
public class NotificationEventCallback implements ComponentEventCallback
{
    private final String _eventType;

    private final String _completeId;

    public NotificationEventCallback(String eventType, String completeId)
    {
        _eventType = eventType;
        _completeId = completeId;
    }

    public boolean handleResult(Object result, Component component, String methodDescription)
    {
        if (result instanceof Boolean) return ((Boolean) result);

        throw new IllegalArgumentException(
                UtilMessages.noReturnValueAccepted(_eventType, _completeId, result, methodDescription));
    }

}
