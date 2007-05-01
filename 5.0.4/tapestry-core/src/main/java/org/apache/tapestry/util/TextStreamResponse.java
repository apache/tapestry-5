// Copyright 2007 The Apache Software Foundation
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

package org.apache.tapestry.util;

import static org.apache.tapestry.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tapestry.StreamResponse;

public class TextStreamResponse implements StreamResponse
{
    private final String _contentType;

    private final String _text;

    public TextStreamResponse(final String contentType, final String text)
    {
        notBlank(contentType, "contentType");
        notNull(text, "text");

        _contentType = contentType;
        _text = text;
    }

    public String getContentType()
    {
        return _contentType;
    }

    public InputStream getStream() throws IOException
    {
        return new ByteArrayInputStream(_text.getBytes());
    }

}
