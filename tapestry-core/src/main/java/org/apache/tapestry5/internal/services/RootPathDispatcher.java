// Copyright 2007, 2008, 2010, 2011, 2012 The Apache Software Foundation
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

import org.apache.tapestry5.EventContext;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.beanmodel.services.*;
import org.apache.tapestry5.http.services.Dispatcher;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.internal.EmptyEventContext;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.ComponentClassResolver;
import org.apache.tapestry5.services.ComponentRequestHandler;
import org.apache.tapestry5.services.LocalizationSetter;
import org.apache.tapestry5.services.PageRenderRequestParameters;

import java.io.IOException;

/**
 * Recognizes a request for the application root (i.e., "/") and handles this the same as a render request for the
 * "Start" page. Support for the Start page is kept for legacy purposes, Index pages are the correct approach.
 */
public class RootPathDispatcher implements Dispatcher
{
    private static final EventContext EMPTY_CONTEXT = new EmptyEventContext();

    private final ComponentClassResolver componentClassResolver;

    private final ComponentRequestHandler handler;

    private final String startPageName;

    private final PageRenderRequestParameters parameters;

    private final LocalizationSetter localizationSetter;

    public RootPathDispatcher(ComponentClassResolver componentClassResolver,

                              ComponentRequestHandler handler,

                              @Symbol(SymbolConstants.START_PAGE_NAME)
                              String startPageName, LocalizationSetter localizationSetter)
    {
        this.componentClassResolver = componentClassResolver;
        this.handler = handler;
        this.startPageName = startPageName;
        this.localizationSetter = localizationSetter;

        parameters = new PageRenderRequestParameters(this.startPageName, EMPTY_CONTEXT, false);
    }

    public boolean dispatch(Request request, final Response response) throws IOException
    {
        // Only match the root path

        if (request.getPath().equals("/") && componentClassResolver.isPageName(startPageName))
        {
            localizationSetter.setNonPersistentLocaleFromLocaleName(request.getLocale().toString());

            handler.handlePageRender(parameters);

            return true;
        }

        return false;
    }

}
