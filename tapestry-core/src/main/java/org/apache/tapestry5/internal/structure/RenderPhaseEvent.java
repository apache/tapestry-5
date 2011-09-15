// Copyright 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.internal.services.EventImpl;
import org.apache.tapestry5.ioc.OperationTracker;
import org.slf4j.Logger;

public class RenderPhaseEvent extends EventImpl
{
    private final RenderPhaseEventHandler handler;

    public RenderPhaseEvent(RenderPhaseEventHandler handler, Logger logger, OperationTracker tracker)
    {
        super(handler, logger, tracker);

        this.handler = handler;

    }

    /**
     * Delegates to {@link org.apache.tapestry5.internal.structure.RenderPhaseEventHandler#enqueueSavedRenderCommands()}, to queue up any
     * render commands returned from invoked event handler methods.
     */
    public void enqueueSavedRenderCommands()
    {
        handler.enqueueSavedRenderCommands();
    }

    public boolean getResult()
    {
        return handler.getResult();
    }
}
