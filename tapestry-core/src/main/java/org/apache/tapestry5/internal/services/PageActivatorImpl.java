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

import org.apache.tapestry5.*;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.HttpError;
import org.apache.tapestry5.services.MetaDataLocator;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletResponse;

public class PageActivatorImpl implements PageActivator
{
    private final Logger logger;

    private final MetaDataLocator metaDataLocator;

    private final UnknownActivationContextHandler unknownActivationContextHandler;

    public PageActivatorImpl(Logger logger, MetaDataLocator metaDataLocator,
                             UnknownActivationContextHandler unknownActivationContextHandler)
    {
        this.logger = logger;
        this.metaDataLocator = metaDataLocator;
        this.unknownActivationContextHandler = unknownActivationContextHandler;
    }

    @SuppressWarnings("unchecked")
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

        return false;
    }

}
