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

import org.apache.tapestry5.internal.util.Base64OutputStream;
import org.apache.tapestry5.services.ClientDataSink;
import org.apache.tapestry5.services.URLEncoder;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class ClientDataSinkImpl implements ClientDataSink
{
    private final Base64OutputStream base64OutputStream;

    private final ObjectOutputStream objectOutputStream;

    private final URLEncoder urlEncoder;

    private boolean closed;

    public ClientDataSinkImpl(URLEncoder urlEncoder) throws IOException
    {
        this.urlEncoder = urlEncoder;
        base64OutputStream = new Base64OutputStream();

        final BufferedOutputStream pipeline = new BufferedOutputStream(new GZIPOutputStream(base64OutputStream));

        OutputStream guard = new OutputStream()
        {
            @Override
            public void write(int b) throws IOException
            {
                pipeline.write(b);
            }

            @Override
            public void close() throws IOException
            {
                closed = true;

                pipeline.close();
            }

            @Override
            public void flush() throws IOException
            {
                pipeline.flush();
            }

            @Override
            public void write(byte[] b) throws IOException
            {
                pipeline.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException
            {
                pipeline.write(b, off, len);
            }
        };


        objectOutputStream = new ObjectOutputStream(guard);
    }

    public ObjectOutputStream getObjectOutputStream()
    {
        return objectOutputStream;
    }

    public String getClientData()
    {
        if (!closed)
        {
            try
            {
                objectOutputStream.close();
            }
            catch (IOException ex)
            {
                // Ignore.
            }
        }

        return base64OutputStream.toBase64();
    }

    public String getEncodedClientData()
    {
        return urlEncoder.encode(getClientData());
    }
}
