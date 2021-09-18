// Copyright 2007, 2008, 2009, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.http.Link;

public class TestableResponseImpl implements TestableResponse
{
    private Link link;

    private boolean committed;

    private Document renderedDocument;

    private Map<String, Object> headers;

    private String redirectURL;

    private int status = HttpServletResponse.SC_OK;

    private String errorMessage;

    private int contentLength = 0;
    
    private String contentType;

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private ServletOutputStream outputStream = new TesableServletOutputStream(output);

    private PrintWriter printWriter;

    public TestableResponseImpl()
    {
        headers = CollectionFactory.newMap();
    }

    public OutputStream getOutputStream(String contentType) throws IOException
    {
        this.contentType = contentType;
        
        return this.outputStream;
    }

    public PrintWriter getPrintWriter(String contentType) throws IOException
    {
        committed = true;
        
        this.contentType = contentType;

        if (printWriter == null)
        {
            this.printWriter = new PrintWriter(new OutputStreamWriter(output));
        }

        return this.printWriter;
    }

    public void sendError(int sc, String message) throws IOException
    {
        setCommitted();

        this.status = sc;
        this.errorMessage = message;
    }

    public void sendRedirect(String URL) throws IOException
    {
        setCommitted();

        this.redirectURL = URL;
    }

    public void setContentLength(int length)
    {
        this.contentLength = length;
    }

    public void setDateHeader(String name, long date)
    {
        headers.put(name, date);
    }

    public void setHeader(String name, String value)
    {
        headers.put(name, value);
    }
    
    @SuppressWarnings("unchecked")
    public void addHeader(String name, String value)
    {
        List<String> values = (List<String>) headers.get(name);
        if (values == null) {
            values = new ArrayList<String>();
            headers.put(name, values);
        }
        values.add(value);
    }
    
    public List<?> getHeaders(String name)
    {
        return (List<?>) headers.get(name);
    }

    public void setIntHeader(String name, int value)
    {
        headers.put(name, value);
    }

    public void sendRedirect(Link link) throws IOException
    {
        setCommitted();

        this.link = link;
    }

    public void setStatus(int sc)
    {
        this.status = sc;
    }

    public String encodeRedirectURL(String URL)
    {
        return URL;
    }

    public String encodeURL(String URL)
    {
        return URL;
    }

    public Link getRedirectLink()
    {
        return link;
    }

    public boolean isCommitted()
    {
        return committed;
    }

    public void clear()
    {
        committed = false;
        link = null;
        renderedDocument = null;
        headers.clear();
        redirectURL = null;
        printWriter = null;
        status = HttpServletResponse.SC_OK;
        errorMessage = null;
        contentLength = 0;
        contentType = null;
        output.reset();
    }

    public Document getRenderedDocument()
    {
        return renderedDocument;
    }

    public void setRenderedDocument(Document document)
    {
        renderedDocument = document;
    }

    public void disableCompression()
    {
    }

    public Object getHeader(String name)
    {
        return headers.get(name);
    }

    public String getRedirectURL()
    {
        return this.redirectURL;
    }

    public int getStatus()
    {
        return status;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }

    public int getContentLength()
    {
        return contentLength;
    }

    private void setCommitted()
    {
        this.committed = true;
    }
    
    public String getContentType()
    {
        return this.contentType;
    }

    public String getOutput()
    {
        return output.toString();
    }

    private class TesableServletOutputStream extends ServletOutputStream
    {
        private OutputStream delegate;

        public TesableServletOutputStream(OutputStream delegate)
        {
            super();
            this.delegate = delegate;
        }

        @Override
        public void write(int b) throws IOException
        {
            delegate.write(b);
        }

        @Override
        public void flush() throws IOException
        {
            super.flush();

            this.delegate.flush();

            setCommitted();
        }

        @Override
        public void close() throws IOException
        {
            super.close();

            this.delegate.close();
        }

    }
}
