// Copyright 2006, 2007 The Apache Software Foundation
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

import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry.ioc.internal.util.Defense;
import org.apache.tapestry.services.Response;

/**
 * Implementation of {@link Response} that wraps around an underlying {@link HttpServletResponse}.
 */
public class ResponseImpl implements Response
{
    private final HttpServletResponse _response;

    public ResponseImpl(HttpServletResponse response)
    {
        Defense.notNull(response, "response");

        _response = response;
    }

    public PrintWriter getPrintWriter(String contentType) throws IOException
    {
        notBlank(contentType, "contentType");

        _response.setContentType(contentType);

        return _response.getWriter();
    }

    public String encodeURL(String URL)
    {
        return _response.encodeURL(URL);
    }

    public String encodeRedirectURL(String URL)
    {
        return _response.encodeRedirectURL(URL);
    }

    public void sendRedirect(String URL) throws IOException
    {
        _response.sendRedirect(URL);
    }

    public OutputStream getOutputStream(String contentType) throws IOException
    {
        notBlank(contentType, "contentType");

        _response.setContentType(contentType);

        return _response.getOutputStream();
    }

    public void sendError(int sc, String message) throws IOException
    {
        _response.sendError(sc, message);
    }

    public void setContentLength(int length)
    {
        _response.setContentLength(length);
    }

    public void setDateHeader(String name, long date)
    {
        _response.setDateHeader(name, date);
    }

}
