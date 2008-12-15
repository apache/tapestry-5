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

package org.apache.tapestry5.internal.events;

import org.apache.tapestry5.services.Request;

import java.util.EventObject;

public final class EndOfRequestEvent extends EventObject
{
    private final Request request;


    /**
     * Constructs a prototypical Event.
     *
     * @param source The Request which is completing.
     * @throws IllegalArgumentException if source is null.
     */
    public EndOfRequestEvent(Request source)
    {
        super(source);

        this.request = source;
    }

    /**
     * The Request object (the source of the event).
     */
    public Request getRequest()
    {
        return request;
    }
}
