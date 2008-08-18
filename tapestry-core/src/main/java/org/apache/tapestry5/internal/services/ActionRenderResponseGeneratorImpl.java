// Copyright 2008 The Apache Software Foundation
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
import org.apache.tapestry5.services.Response;

import java.io.IOException;

/**
 * Simply uses the {@link org.apache.tapestry5.internal.services.LinkFactory} to generate a link which is then
 * {@linkplain org.apache.tapestry5.services.Response#sendRedirect(org.apache.tapestry5.Link)} sent as a redirect}.
 */
public class ActionRenderResponseGeneratorImpl implements ActionRenderResponseGenerator
{
    private final LinkFactory linkFactory;

    private final Response response;

    public ActionRenderResponseGeneratorImpl(LinkFactory linkFactory, Response response)
    {
        this.linkFactory = linkFactory;
        this.response = response;
    }

    public void generateResponse(Page page) throws IOException
    {
        Link link = linkFactory.createPageRenderLink(page, false);

        response.sendRedirect(link);
    }
}
