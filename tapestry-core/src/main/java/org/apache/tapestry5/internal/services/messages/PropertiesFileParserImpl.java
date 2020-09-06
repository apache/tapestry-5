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

package org.apache.tapestry5.internal.services.messages;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.messages.PropertiesFileParser;

public class PropertiesFileParserImpl implements PropertiesFileParser
{
    /**
     * Charset used when reading a properties file.
     */
    private static final String CHARSET = "UTF-8";

    /**
     * Buffer size used when reading a properties file.
     */
    private static final int BUFFER_SIZE = 2000;

    public Map<String, String> parsePropertiesFile(Resource resource) throws IOException
    {
        Map<String, String> result = CollectionFactory.newCaseInsensitiveMap();

        Properties p = new Properties();
        InputStream is = null;

        try
        {

            is = readUTFStreamToEscapedASCII(resource.openStream());

            // Ok, now we have the content read into memory as UTF-8, not ASCII.

            p.load(is);

            is.close();

            is = null;
        }
        finally
        {
            InternalUtils.close(is);
        }

        for (Map.Entry e : p.entrySet())
        {
            String key = e.getKey().toString();

            String value = p.getProperty(key);

            result.put(key, value);
        }

        return result;
    }

    /**
     * Reads a UTF-8 stream, performing a conversion to ASCII (i.e., ISO8859-1 encoding). Characters outside the normal
     * range for ISO8859-1 are converted to unicode escapes. In effect, Tapestry is performing native2ascii on the
     * files, on the fly.
     */
    private static InputStream readUTFStreamToEscapedASCII(InputStream is) throws IOException
    {
        Reader reader = new InputStreamReader(is, CHARSET);

        StringBuilder builder = new StringBuilder(BUFFER_SIZE);
        char[] buffer = new char[BUFFER_SIZE];

        while (true)
        {
            int length = reader.read(buffer);

            if (length < 0)
                break;

            for (int i = 0; i < length; i++)
            {
                char ch = buffer[i];

                if (ch <= '\u007f')
                {
                    builder.append(ch);
                    continue;
                }

                builder.append(String.format("\\u%04x", (int) ch));
            }
        }

        reader.close();

        byte[] resourceContent = builder.toString().getBytes();

        return new ByteArrayInputStream(resourceContent);
    }
}
