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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.internal.structure.Page;
import org.apache.tapestry5.services.PersistentLocale;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;

public class LinkFactoryImpl implements LinkFactory
{
    private final Request request;

    private final Response response;

    private final RequestSecurityManager requestSecurityManager;

    private final RequestPathOptimizer optimizer;

    private final ComponentInvocationMap componentInvocationMap;

    private final PersistentLocale persistentLocale;

    public LinkFactoryImpl(Request request, Response response, RequestSecurityManager requestSecurityManager,
                           RequestPathOptimizer optimizer, ComponentInvocationMap componentInvocationMap,
                           PersistentLocale persistentLocale)
    {
        this.request = request;
        this.response = response;
        this.requestSecurityManager = requestSecurityManager;
        this.optimizer = optimizer;
        this.componentInvocationMap = componentInvocationMap;
        this.persistentLocale = persistentLocale;
    }

    public Link create(Page page, ComponentInvocation invocation)
    {
        String baseURL = requestSecurityManager.getBaseURL(page);

        Link link = new LinkImpl(response, optimizer, baseURL, request.getContextPath(), persistentLocale.get(),
                                 invocation);

        // This is a hook used for testing; we can relate the link to an invocation so that we can simulate
        // the clicking of the link (or submitting of the form).

        componentInvocationMap.store(link, invocation);

        return link;
    }
}
