// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import java.io.IOException;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.MetaDataConstants;
import org.apache.tapestry5.TrackableComponentEventCallback;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.InternalConstants;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.MetaDataLocator;
import org.slf4j.Logger;

public class PageActivatorImpl implements PageActivator
{
    private final Logger logger;

    private final MetaDataLocator metaDataLocator;

    private final UnknownActivationContextHandler unknownActivationContextHandler;
    
    private final Request request;

    public PageActivatorImpl(Logger logger, MetaDataLocator metaDataLocator,
                             UnknownActivationContextHandler unknownActivationContextHandler,
                             Request request)
    {
        this.logger = logger;
        this.metaDataLocator = metaDataLocator;
        this.unknownActivationContextHandler = unknownActivationContextHandler;
        this.request = request;
    }

    @SuppressWarnings("rawtypes")
    public boolean activatePage(ComponentResources pageResources, EventContext activationContext,
            ComponentEventResultProcessor resultProcessor) throws IOException
    {
        TrackableComponentEventCallback callback = new ComponentResultProcessorWrapper(resultProcessor);

        boolean handled = pageResources.triggerContextEvent(EventConstants.ACTIVATE, activationContext, callback);

        boolean acceptEmpty = !pageResources.getComponentModel().handlesEvent(EventConstants.ACTIVATE) &&
                                activationContext.getCount() == 0;

        boolean checkUnknown = metaDataLocator.findMeta(MetaDataConstants.UNKNOWN_ACTIVATION_CONTEXT_CHECK,
                                                        pageResources, Boolean.class);

        if ( !handled && !acceptEmpty && checkUnknown &&
                !pageResources.getComponentModel().handleActivationEventContext())
        {
            logger.info("Page {} required an exact activation context, let's handle this", pageResources.getPageName());
            unknownActivationContextHandler.handleUnknownContext(pageResources, activationContext);
            return true;
        }

        if (callback.isAborted())
        {
            callback.rethrow();
            return true;
        }
        else
        {
            if (InternalConstants.TRUE.equals(pageResources.getComponentModel().getMeta(
                    InternalConstants.REST_ENDPOINT_EVENT_HANDLER_METHOD_PRESENT)))
            {
                callback = new ComponentResultProcessorWrapper(resultProcessor);
                handled = pageResources.triggerContextEvent(
                        InternalConstants.HTTP_METHOD_EVENT_PREFIX + request.getMethod(), activationContext, callback);
                if (callback.isAborted())
                {
                    callback.rethrow();
                    return true;
                }
                else
                {
                    throw new RestEndpointNotFoundException(
                            String.format("Page %s (%s) has at least one REST endpoint event handler method "
                                    + "but none handled %s for this request", pageResources.getPageName(),
                                    pageResources.getPage().getClass().getName(), request.getMethod()));
                }
            }
        }

        return false;
    }

}
