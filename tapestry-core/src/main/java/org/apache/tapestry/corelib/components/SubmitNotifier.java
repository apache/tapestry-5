// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry.corelib.components;

import org.apache.tapestry.ComponentAction;
import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.annotations.Environmental;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.FormSupport;

/**
 * A non visual component used to provide notifications to its container during a form submission. Records actions into
 * the form on {@link org.apache.tapestry.annotations.BeginRender} and {@link org.apache.tapestry.annotations.AfterRender}
 * that (during the form submission) triggers "BeginSubmit" and "AfterSubmit" events.  The container can receive these
 * events to perform setup before a group of components process their submission, and perform cleanup afterwards.
 */
public class SubmitNotifier
{
    private static final class TriggerEvent implements ComponentAction<SubmitNotifier>
    {
        private final String _eventType;

        public TriggerEvent(String eventType)
        {
            _eventType = eventType;
        }

        public void execute(SubmitNotifier component)
        {
            component.trigger(_eventType);
        }
    }


    @Inject
    private ComponentResources _resources;

    @Environmental
    private FormSupport _formSupport;

    void beginRender()
    {
        _formSupport.store(this, new TriggerEvent("BeginSubmit"));
    }

    void afterRender()
    {
        _formSupport.store(this, new TriggerEvent("AfterSubmit"));
    }

    private void trigger(String eventType)
    {
        _resources.triggerEvent(eventType, null, null);
    }
}
