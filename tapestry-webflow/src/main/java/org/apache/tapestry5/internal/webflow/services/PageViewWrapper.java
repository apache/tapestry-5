// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.internal.webflow.services;

import org.apache.tapestry5.runtime.Component;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.View;

import java.io.IOException;

public class PageViewWrapper implements View
{
    private final String pageName;

    private final Component page;

    private final ComponentEventResultProcessor<Component> resultProcessor;

    public PageViewWrapper(String pageName, Component page, ComponentEventResultProcessor<Component> resultProcessor)
    {
        this.pageName = pageName;
        this.page = page;
        this.resultProcessor = resultProcessor;
    }

    public void render() throws IOException
    {
        resultProcessor.processResultValue(page);
    }

    public void processUserEvent()
    {
    }

    public boolean hasFlowEvent()
    {
        return false;
    }

    public Event getFlowEvent()
    {
        return null;
    }

    @Override
    public String toString()
    {
        return String.format("PageViewWrapper[%s]", pageName);
    }
}
