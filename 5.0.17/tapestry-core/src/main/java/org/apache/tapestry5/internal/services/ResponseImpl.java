// Copyright 2006, 2007, 2008 The Apache Software Foundation
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
import org.apache.tapestry5.ioc.internal.util.Defense;
import static org.apache.tapestry5.ioc.internal.util.Defense.notBlank;
import org.apache.tapestry5.services.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Implementation of {@link Response} that wraps around an underlying {@link HttpServletResponse}.
 */
public class ResponseImpl implements Response
{
    private final HttpServletResponse response;

    public ResponseImpl(HttpServletResponse response)
    {
        Defense.notNull(response, "response");

        this.response = response;
    }

    public PrintWriter getPrintWriter(String contentType) throws IOException
    {
        notBlank(contentType, "contentType");

        response.setContentType(contentType);

        return response.getWriter();
    }

    public String encodeURL(String URL)
    {
        return response.encodeURL(URL);
    }

    public String encodeRedirectURL(String URL)
    {
        return response.encodeRedirectURL(URL);
    }

    public void sendRedirect(String URL) throws IOException
    {
        response.sendRedirect(URL);
    }

    public void sendRedirect(Link link) throws IOException
    {
        Defense.notNull(link, "link");

        String redirectURL = encodeRedirectURL(link.toRedirectURI());

        sendRedirect(redirectURL);
    }

    public void setStatus(int sc)
    {
        response.setStatus(sc);
    }

    public OutputStream getOutputStream(String contentType) throws IOException
    {
        notBlank(contentType, "contentType");

        response.setContentType(contentType);

        return response.getOutputStream();
    }

    public void sendError(int sc, String message) throws IOException
    {
        response.sendError(sc, message);
    }

    public void setContentLength(int length)
    {
        response.setContentLength(length);
    }

    public void setDateHeader(String name, long date)
    {
        response.setDateHeader(name, date);
    }

    public void setHeader(String name, String value)
    {
        response.setHeader(name, value);
    }

    public void setIntHeader(String name, int value)
    {
        response.setIntHeader(name, value);
    }

    public boolean isCommitted()
    {
        return response.isCommitted();
    }
}
