// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.test;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.internal.URLEventContext;
import org.apache.tapestry5.internal.services.ActionLinkTarget;
import org.apache.tapestry5.internal.services.ComponentInvocation;
import org.apache.tapestry5.internal.services.ComponentInvocationMap;
import org.apache.tapestry5.internal.services.InvocationTarget;
import org.apache.tapestry5.ioc.Registry;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.services.ComponentEventRequestHandler;
import org.apache.tapestry5.services.ComponentEventRequestParameters;
import org.apache.tapestry5.services.ContextValueEncoder;

import java.io.IOException;

/**
 * Simulates a click on an action link.
 */
public class ActionLinkInvoker implements ComponentInvoker
{
    private final Registry registry;

    private final ComponentInvoker followupInvoker;

    private final ComponentEventRequestHandler componentEventRequestHandler;

    private final ComponentInvocationMap componentInvocationMap;

    private final TestableResponse response;

    private final ContextValueEncoder contextValueEncoder;

    public ActionLinkInvoker(Registry registry, ComponentInvoker followupInvoker,
                             ComponentInvocationMap componentInvocationMap)
    {
        this.registry = registry;
        this.followupInvoker = followupInvoker;
        componentEventRequestHandler = this.registry.getService("ComponentEventRequestHandler",
                                                                ComponentEventRequestHandler.class);

        response = this.registry.getObject(TestableResponse.class, null);

        this.componentInvocationMap = componentInvocationMap;
        contextValueEncoder = this.registry.getService(ContextValueEncoder.class);

    }

    /**
     * Click on the action link and get another link in return. Then follow up the link with another {@link
     * ComponentInvoker}.
     *
     * @param invocation The ComponentInvocation object corresponding to the action link.
     * @return The DOM created. Typically you will assert against it.
     */
    public Document invoke(ComponentInvocation invocation)
    {
        click(invocation);

        Link link = response.getRedirectLink();

        response.clear();

        if (link == null) throw new RuntimeException("Action did not set a redirect link.");


        ComponentInvocation followup = componentInvocationMap.get(link);

        return followupInvoker.invoke(followup);
    }

    private void click(ComponentInvocation invocation)
    {
        try
        {
            InvocationTarget target = invocation.getTarget();

            ActionLinkTarget actionLinkTarget = Defense.cast(target, ActionLinkTarget.class, "target");

            ComponentEventRequestParameters parameters = new ComponentEventRequestParameters(
                    actionLinkTarget.getPageName(),

                    actionLinkTarget.getPageName(),

                    actionLinkTarget.getComponentNestedId(),

                    actionLinkTarget.getEventType(),

                    new URLEventContext(contextValueEncoder, invocation.getActivationContext()),

                    new URLEventContext(contextValueEncoder, invocation.getContext()));

            componentEventRequestHandler.handle(parameters);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            registry.cleanupThread();
        }
    }
}
