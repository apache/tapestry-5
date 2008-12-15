// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ContentType;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.services.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TextStreamResponse implements StreamResponse
{
    private final ContentType contentType;

    private final String text;

    /**
     * Constructor that defaults the character set to "utf-8".
     */
    public TextStreamResponse(String contentType, String text)
    {
        this(contentType, "UTF-8", text);
    }

    /**
     * Constructor allowing the content type and character set to the specified.
     *
     * @param contentType type of content, often "text/xml"
     * @param charset     character set of output, usually "UTF-8"
     * @param text        text to be streamed in the response
     * @see org.apache.tapestry5.SymbolConstants#CHARSET
     */
    public TextStreamResponse(String contentType, String charset, String text)
    {
        this(new ContentType(Defense.notBlank(contentType, "contentType"),
                             Defense.notBlank(charset, "charset")), text);
    }

    public TextStreamResponse(ContentType contentType, String text)
    {
        Defense.notNull(contentType, "contentType");
        Defense.notNull(text, "text");

        this.contentType = contentType;
        this.text = text;
    }

    public String getContentType()
    {
        return contentType.toString();
    }

    /**
     * Converts the text to a byte array (as per the character set, which is usually "UTF-8"), and returns a stream for
     * that byte array.
     *
     * @return the text as a byte array stram
     * @throws IOException
     */
    public InputStream getStream() throws IOException
    {
        byte[] textBytes = text.getBytes(contentType.getCharset());

        return new ByteArrayInputStream(textBytes);
    }

    /**
     * Does nothing; subclasses may override.
     */
    public void prepareResponse(Response response)
    {

    }
}
