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

package org.apache.tapestry.internal.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.tapestry.services.Response;

public class TestableResponseImpl implements Response
{

    private void nyi(String methodName)
    {
        throw new RuntimeException(String.format(
                "TestableResponse: Method %s() not yet implemented.",
                methodName));
    }

    public OutputStream getOutputStream(String contentType) throws IOException
    {
        nyi("getOutputStream");

        return null;
    }

    public PrintWriter getPrintWriter(String contentType) throws IOException
    {
        nyi("getPrintWriter");

        return null;
    }

    public void sendError(int sc, String message) throws IOException
    {
        nyi("sendError");
    }

    public void sendRedirect(String URL) throws IOException
    {
        nyi("sendRedirect");
    }

    public void setContentLength(int length)
    {
        nyi("setContentLength");
    }

    public void setDateHeader(String name, long date)
    {
        nyi("setDateHeader");
    }

    public void setHeader(String name, String value)
    {
        nyi("setHeader");
    }

    public void setIntHeader(String name, int value)
    {
        nyi("setIntHeader");
    }

    public String encodeRedirectURL(String URL)
    {
        return URL;
    }

    public String encodeURL(String URL)
    {
        return URL;
    }

}
