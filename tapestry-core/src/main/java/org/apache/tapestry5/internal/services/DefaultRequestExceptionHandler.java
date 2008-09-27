// Copyright 2006, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ExceptionReporter;
import org.apache.tapestry5.services.RequestExceptionHandler;
import org.apache.tapestry5.services.Response;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Default implementation of {@link RequestExceptionHandler} that displays the standard ExceptionReport page. The page
 * must implement the {@link ExceptionReporter} interface.
 */
public class DefaultRequestExceptionHandler implements RequestExceptionHandler
{
    private final RequestPageCache pageCache;

    private final PageResponseRenderer renderer;

    private final Logger logger;

    private final String pageName;

    private final Response response;

    public DefaultRequestExceptionHandler(RequestPageCache pageCache, PageResponseRenderer renderer, Logger logger,

                                          @Inject @Symbol(SymbolConstants.EXCEPTION_REPORT_PAGE)
                                          String pageName,

                                          Response response)
    {
        this.pageCache = pageCache;
        this.renderer = renderer;
        this.logger = logger;
        this.pageName = pageName;
        this.response = response;
    }

    public void handleRequestException(Throwable exception) throws IOException
    {
        logger.error(ServicesMessages.requestException(exception), exception);

        // TAP5-233: Make sure the client knows that an error occurred.

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.setHeader("X-Tapestry-ErrorMessage", InternalUtils.toMessage(exception));

        Page page = pageCache.get(pageName);

        ExceptionReporter rootComponent = (ExceptionReporter) page.getRootComponent();

        // Let the page set up for the new exception.

        rootComponent.reportException(exception);

        renderer.renderPageResponse(page);
    }
}
