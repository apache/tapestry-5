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

package org.apache.tapestry5.json;

import java.io.PrintWriter;

/**
 * Prints the JSON content compactly, with no indentation or extra whitespace.
 * 
 * @since 5.2.0
 */
class CompactSession implements JSONPrintSession
{
    private final PrintWriter writer;

    public CompactSession(PrintWriter writer)
    {
        this.writer = writer;
    }

    @Override
    public JSONPrintSession indent()
    {
        return this;
    }

    @Override
    public JSONPrintSession newline()
    {
        return this;
    }

    @Override
    public JSONPrintSession outdent()
    {
        return this;
    }

    @Override
    public JSONPrintSession print(String value)
    {
        writer.print(value);

        return this;
    }

    @Override
    public JSONPrintSession printQuoted(String value)
    {
        return print(JSONObject.quote(value));
    }

    @Override
    public JSONPrintSession printSymbol(char symbol)
    {
        writer.print(symbol);

        return this;
    }

}
