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

import org.apache.tapestry5.internal.util.Base64InputStream;
import org.apache.tapestry5.services.ClientDataEncoder;
import org.apache.tapestry5.services.ClientDataSink;
import org.apache.tapestry5.services.URLEncoder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.zip.GZIPInputStream;

public class ClientDataEncoderImpl implements ClientDataEncoder
{
    private final URLEncoder urlEncoder;

    public ClientDataEncoderImpl(URLEncoder urlEncoder)
    {
        this.urlEncoder = urlEncoder;
    }

    public ClientDataSink createSink()
    {
        try
        {
            return new ClientDataSinkImpl(urlEncoder);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public ObjectInputStream decodeClientData(String clientData)
    {
        // The clientData is Base64 that's been gzip'ed (i.e., this matches
        // what ClientDataSinkImpl does.

        try
        {
            BufferedInputStream buffered = new BufferedInputStream(
                    new GZIPInputStream(new Base64InputStream(clientData)));

            return new ObjectInputStream(buffered);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    public ObjectInputStream decodeEncodedClientData(String clientData) throws IOException
    {
        return decodeClientData(urlEncoder.decode(clientData));
    }
}
