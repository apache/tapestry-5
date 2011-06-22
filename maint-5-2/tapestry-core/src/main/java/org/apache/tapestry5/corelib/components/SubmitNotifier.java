// Copyright 2008, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.corelib.components;

import org.apache.tapestry5.ComponentAction;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Events;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.FormSupport;

/**
 * A non visual component used to provide notifications to its container during a form submission. Records actions into
 * the form on {@link org.apache.tapestry5.annotations.BeginRender} and {@link org.apache.tapestry5.annotations.AfterRender}
 * that (during the form submission) triggers "BeginSubmit" and "AfterSubmit" events.  The container can receive these
 * events to perform setup before a group of components process their submission, and perform cleanup afterwards.
 */
@Events({ "BeginSubmit", "AfterSubmit" })
public class SubmitNotifier
{
    private static final class TriggerEvent implements ComponentAction<SubmitNotifier>
    {
        private final String eventType;

        public TriggerEvent(String eventType)
        {
            this.eventType = eventType;
        }

        public void execute(SubmitNotifier component)
        {
            component.trigger(eventType);
        }

        @Override
        public String toString()
        {
            return String.format("SubmitNotifier.TriggerEvent[%s]", eventType);
        }
    }


    @Inject
    private ComponentResources resources;

    @Environmental
    private FormSupport formSupport;

    void beginRender()
    {
        formSupport.store(this, new TriggerEvent("BeginSubmit"));
    }

    void afterRender()
    {
        formSupport.store(this, new TriggerEvent("AfterSubmit"));
    }

    private void trigger(String eventType)
    {
        resources.triggerEvent(eventType, null, null);
    }
}
