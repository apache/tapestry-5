// Copyright 2026 The Apache Software Foundation
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

package org.apache.tapestry5.json;

import java.io.PrintWriter;

/**
 * The destination a {@link JSONPrintSession} (and {@link JSONObject#escapeInto}) writes
 * characters to: either an in-memory buffer (for {@code toString()}/{@code toCompactString()})
 * or a caller-supplied {@link PrintWriter} (for {@code print(PrintWriter, boolean)}).
 *
 * <p>Deliberately narrower than {@link Appendable}: neither implementation below can actually throw
 * {@link java.io.IOException}, so this avoids forcing checked-exception handling onto every
 * append call in the hot print path.
 */
interface JSONPrintSink
{
    JSONPrintSink append(String value);

    JSONPrintSink append(char value);
}

/**
 * Sink backed by an in-memory {@link StringBuilder}, used to build a {@code String} directly
 * without an intermediate {@code CharArrayWriter}/{@code PrintWriter} pair.
 */
class StringBuilderPrintSink implements JSONPrintSink
{
    private final StringBuilder out;

    StringBuilderPrintSink(StringBuilder out)
    {
        this.out = out;
    }

    @Override
    public JSONPrintSink append(String value)
    {
        out.append(value);

        return this;
    }

    @Override
    public JSONPrintSink append(char value)
    {
        out.append(value);

        return this;
    }
}

/**
 * Sink backed by a caller-supplied {@link PrintWriter}, used by {@code print(PrintWriter, boolean)}.
 */
class WriterPrintSink implements JSONPrintSink
{
    private final PrintWriter writer;

    WriterPrintSink(PrintWriter writer)
    {
        this.writer = writer;
    }

    @Override
    public JSONPrintSink append(String value)
    {
        writer.print(value);

        return this;
    }

    @Override
    public JSONPrintSink append(char value)
    {
        writer.write(value);

        return this;
    }
}
