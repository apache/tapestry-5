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

package org.apache.tapestry5.integration.app1.components;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Parameter;

import java.text.Format;

/**
 * component that formats a value and outputs it.
 */
public class Output
{
    @Parameter(required = true)
    private Object value;

    @Parameter(required = true)
    private Format format;

    void beginRender(MarkupWriter writer)
    {
        String formatted = format.format(value);

        writer.write(formatted);
    }
}
