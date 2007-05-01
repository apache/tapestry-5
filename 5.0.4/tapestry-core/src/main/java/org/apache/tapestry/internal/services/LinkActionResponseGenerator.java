// Copyright 2007 The Apache Software Foundation
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

import org.apache.tapestry.Link;
import org.apache.tapestry.services.ActionResponseGenerator;
import org.apache.tapestry.services.Response;

/**
 * The standard response generator which converts a {@link Link} into a
 * {@link Link#toRedirectURI() redirect URI} and
 * {@link Response#sendRedirect(String) sends the redirect}.
 */
public class LinkActionResponseGenerator implements ActionResponseGenerator
{
    private final Link _link;

    public LinkActionResponseGenerator(final Link link)
    {
        _link = link;
    }

    public void sendClientResponse(Response response) throws IOException
    {
        response.sendRedirect(_link.toRedirectURI());
    }

    public Link getLink()
    {
        return _link;
    }
}
