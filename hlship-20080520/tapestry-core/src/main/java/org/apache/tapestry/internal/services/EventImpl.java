// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ComponentEventCallback;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;
import org.apache.tapestry.runtime.Event;

public class EventImpl implements Event
{
    private boolean aborted;

    private String methodDescription;

    private final ComponentEventCallback handler;

    public EventImpl(ComponentEventCallback handler)
    {
        this.handler = notNull(handler, "handler");
    }

    public boolean isAborted()
    {
        return aborted;
    }

    public void setMethodDescription(String methodDescription)
    {
        this.methodDescription = methodDescription;
    }

    @SuppressWarnings("unchecked")
    public boolean storeResult(Object result)
    {
        // Given that this method is *only* invoked from code
        // that is generated at runtime and proven to be correct,
        // this should never, ever happen. But what the hell,
        // let's check anyway.

        if (aborted) throw new IllegalStateException(ServicesMessages
                .componentEventIsAborted(methodDescription));


        if (result != null) aborted |= handler.handleResult(result);

        return aborted;
    }

    protected String getMethodDescription()
    {
        return methodDescription;
    }
}
