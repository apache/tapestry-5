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

import org.apache.tapestry5.services.ApplicationGlobals;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.context.servlet.ServletExternalContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExternalContextSourceImpl implements ExternalContextSource
{
    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final ApplicationGlobals applicationGlobals;

    private final FlowUrlHandler flowUrlHandler;

    public ExternalContextSourceImpl(HttpServletRequest request, HttpServletResponse response,
                                     ApplicationGlobals applicationGlobals, FlowUrlHandler flowUrlHandler)
    {
        this.request = request;
        this.response = response;
        this.applicationGlobals = applicationGlobals;
        this.flowUrlHandler = flowUrlHandler;
    }

    public ExternalContext create()
    {
        return new ServletExternalContext(applicationGlobals.getServletContext(), request, response, flowUrlHandler);
    }
}
