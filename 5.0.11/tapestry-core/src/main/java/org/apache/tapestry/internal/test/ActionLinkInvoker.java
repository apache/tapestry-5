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

package org.apache.tapestry.internal.test;

import org.apache.tapestry.Link;
import org.apache.tapestry.dom.Document;
import org.apache.tapestry.internal.URLEventContext;
import org.apache.tapestry.internal.services.ActionLinkTarget;
import org.apache.tapestry.internal.services.ComponentInvocation;
import org.apache.tapestry.internal.services.ComponentInvocationMap;
import org.apache.tapestry.internal.services.InvocationTarget;
import org.apache.tapestry.ioc.Registry;
import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.services.ComponentEventRequestHandler;
import org.apache.tapestry.services.ComponentEventRequestParameters;
import org.apache.tapestry.services.ContextValueEncoder;

import java.io.IOException;

/**
 * Simulates a click on an action link.
 */
public class ActionLinkInvoker implements ComponentInvoker
{
    private final Registry _registry;

    private final ComponentInvoker _followupInvoker;

    private final ComponentEventRequestHandler _componentEventRequestHandler;

    private final ComponentInvocationMap _componentInvocationMap;

    private final TestableResponse _response;

    private final ContextValueEncoder _contextValueEncoder;

    public ActionLinkInvoker(Registry registry, ComponentInvoker followupInvoker,
                             ComponentInvocationMap componentInvocationMap)
    {
        _registry = registry;
        _followupInvoker = followupInvoker;
        _componentEventRequestHandler = _registry.getService("ComponentEventRequestHandler",
                                                             ComponentEventRequestHandler.class);

        _response = _registry.getObject(TestableResponse.class, null);

        _componentInvocationMap = componentInvocationMap;
        _contextValueEncoder = _registry.getService(ContextValueEncoder.class);

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

        Link link = _response.getRedirectLink();

        _response.clear();

        if (link == null) throw new RuntimeException("Action did not set a redirect link.");


        ComponentInvocation followup = _componentInvocationMap.get(link);

        return _followupInvoker.invoke(followup);
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

                    new URLEventContext(_contextValueEncoder, invocation.getActivationContext()),

                    new URLEventContext(_contextValueEncoder, invocation.getContext()));

            _componentEventRequestHandler.handle(parameters);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            _registry.cleanupThread();
        }
    }
}
