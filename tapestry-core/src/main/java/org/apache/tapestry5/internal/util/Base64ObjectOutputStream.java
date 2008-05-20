// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry5.internal.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Wraps a {@link Base64OutputStream} with a {@link GZIPOutputStream} as an {@link ObjectOutputStream}. This allows an
 * object (or objects) to be encoded into a Base64 string (accessed via {@link #toBase64()}).
 *
 * @see Base64ObjectInputStream
 */
public class Base64ObjectOutputStream extends ObjectOutputStream
{
    private final Base64OutputStream output;

    private Base64ObjectOutputStream(Base64OutputStream output) throws IOException
    {
        super(new BufferedOutputStream(new GZIPOutputStream(output)));

        this.output = output;
    }

    public Base64ObjectOutputStream() throws IOException
    {
        this(new Base64OutputStream());
    }

    public String toBase64()
    {
        return output.toBase64();
    }
}
