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

package org.apache.tapestry5.util;

import org.apache.tapestry5.Link;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.services.Response;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Implementation of {@link org.apache.tapestry5.services.Response} that delegates all method invocations to a delegate
 * instance. This is used as a base class for overriding just some behaviors of Response.
 */
public class ResponseWrapper implements Response
{
    protected final Response response;

    public ResponseWrapper(Response response)
    {
        Defense.notNull(response, "response");

        this.response = response;
    }

    public PrintWriter getPrintWriter(String contentType) throws IOException
    {
        return response.getPrintWriter(contentType);
    }

    public OutputStream getOutputStream(String contentType) throws IOException
    {
        return response.getOutputStream(contentType);
    }

    public void sendRedirect(String URL) throws IOException
    {
        response.sendRedirect(URL);
    }

    public void sendRedirect(Link link) throws IOException
    {
        response.sendRedirect(link);
    }

    public void setStatus(int sc)
    {
        response.setStatus(sc);
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

    public String encodeURL(String URL)
    {
        return response.encodeURL(URL);
    }

    public String encodeRedirectURL(String URL)
    {
        return response.encodeRedirectURL(URL);
    }

    public boolean isCommitted()
    {
        return response.isCommitted();
    }
}
