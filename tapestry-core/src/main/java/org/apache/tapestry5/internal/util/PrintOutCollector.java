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

package org.apache.tapestry5.internal.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Utility for collecting the output of a {@link java.io.PrintWriter}.
 */
public class PrintOutCollector
{
    private final StringWriter stringWriter;

    private PrintWriter printWriter;

    public PrintOutCollector()
    {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }

    public PrintWriter getPrintWriter()
    {
        return printWriter;
    }

    /**
     * Closes the {@link PrintWriter} and returns the accumulated text output.
     */
    public String getPrintOut()
    {
        printWriter.close();
        return stringWriter.toString();

    }

}
