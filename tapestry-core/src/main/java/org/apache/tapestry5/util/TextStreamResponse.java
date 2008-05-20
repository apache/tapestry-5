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

package org.apache.tapestry5.util;

import org.apache.tapestry5.StreamResponse;
import static org.apache.tapestry5.ioc.internal.util.Defense.notBlank;
import static org.apache.tapestry5.ioc.internal.util.Defense.notNull;
import org.apache.tapestry5.services.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TextStreamResponse implements StreamResponse
{
    private final String contentType, text;

    public TextStreamResponse(String contentType, String text)
    {
        notBlank(contentType, "contentType");
        notNull(text, "text");

        this.contentType = contentType;
        this.text = text;
    }

    public String getContentType()
    {
        return contentType;
    }

    public InputStream getStream() throws IOException
    {
        return new ByteArrayInputStream(text.getBytes());
    }

    public void prepareResponse(Response response)
    {
        // No-op by default.
    }

}
