// Copyright 2012 The Apache Software Foundation
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

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that acts like a "tee", copying all provided bytes to two output streams. This is used, for example,
 * to accumulate a hash of content even as it is being written.
 *
 * @since 5.3.5
 */
public class TeeOutputStream extends OutputStream
{
    private final OutputStream left, right;

    public TeeOutputStream(OutputStream left, OutputStream right)
    {
        assert left != null;
        assert right != null;

        this.left = left;
        this.right = right;
    }

    @Override
    public void write(int b) throws IOException
    {
        left.write(b);
        right.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException
    {
        left.write(b);
        right.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        left.write(b, off, len);
        right.write(b, off, len);
    }

    @Override
    public void flush() throws IOException
    {
        left.flush();
        right.flush();
    }

    @Override
    public void close() throws IOException
    {
        left.close();
        right.close();
    }
}
