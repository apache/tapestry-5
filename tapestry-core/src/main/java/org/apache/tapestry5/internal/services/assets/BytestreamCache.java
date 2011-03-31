// Copyright 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services.assets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A wrapper around a byte-stream, represented internally as a byte array. Part of the fix
 * to TAP5-1116, avoiding a live lock due to ByteArrayOutputStream.writeTo() being a synchronized
 * method.
 * 
 * @since 5.2.0
 */
public class BytestreamCache
{
    private final byte[] streamData;

    public BytestreamCache(byte[] streamData)
    {
        this.streamData = streamData;
    }

    public BytestreamCache(ByteArrayOutputStream os)
    {
        this(os.toByteArray());
    }

    public void writeTo(OutputStream os) throws IOException
    {
        os.write(streamData, 0, streamData.length);
    }

    public int size()
    {
        return streamData.length;
    }

    public InputStream openStream()
    {
        return new ByteArrayInputStream(streamData);
    }
}
