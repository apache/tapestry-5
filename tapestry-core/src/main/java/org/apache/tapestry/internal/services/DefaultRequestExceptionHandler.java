// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.internal.services;

import java.io.IOException;

import org.apache.tapestry.internal.structure.Page;
import org.apache.tapestry.services.ExceptionReporter;
import org.apache.tapestry.services.RequestExceptionHandler;
import org.apache.tapestry.services.Response;

/**
 * Default implementation of {@link RequestExceptionHandler} that displays the standard
 * ExceptionReport page. The page must implement the {@link ExceptionReporter} interface.
 * 
 * 
 */
public class DefaultRequestExceptionHandler implements RequestExceptionHandler
{
    private final RequestPageCache _pageCache;

    private final PageResponseRenderer _renderer;

    private final Response _response;

    public DefaultRequestExceptionHandler(RequestPageCache pageCache,
            PageResponseRenderer renderer, Response response)
    {
        _pageCache = pageCache;
        _renderer = renderer;
        _response = response;
    }

    public void handleRequestException(Throwable exception) throws IOException
    {
        Page page = _pageCache.get("ExceptionReport");

        ExceptionReporter rootComponent = (ExceptionReporter) page.getRootComponent();

        // Let the page set up for the new exception.

        rootComponent.reportException(exception);

        _renderer.renderPageResponse(page, _response);
    }
}
