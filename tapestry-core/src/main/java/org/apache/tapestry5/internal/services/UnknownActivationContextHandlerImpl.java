// Copyright 2030 The Apache Software Foundation
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

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.services.ComponentEventResultProcessor;
import org.apache.tapestry5.services.HttpError;
import org.apache.tapestry5.services.Traditional;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Default implementation for {@link UnknownActivationContextHandler} which answer with a 404 NOT FOUND error.
 */
public class UnknownActivationContextHandlerImpl implements UnknownActivationContextHandler
{
    private final Logger logger;

    private final ComponentEventResultProcessor resultProcessor;


    public UnknownActivationContextHandlerImpl(Logger logger,
                                               @Traditional @Primary
                                               ComponentEventResultProcessor resultProcessor)
    {
        this.logger = logger;
        this.resultProcessor = resultProcessor;
    }

    public void handleUnknownContext(ComponentResources pageResources, EventContext activationContext)
            throws IOException
    {
        logger.warn("Activate event on page {} was fired with context {} but was not handled",
                pageResources.getPage().getClass(), activationContext);

        String message = String.format("Activation context %s unrecognized for page %s",
                activationContext, pageResources.getPage().getClass());

        resultProcessor.processResultValue(new HttpError(HttpServletResponse.SC_NOT_FOUND, message));
    }
}
