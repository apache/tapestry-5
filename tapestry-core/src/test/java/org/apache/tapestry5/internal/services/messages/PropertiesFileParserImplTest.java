// Copyright 2008, 2010 The Apache Software Foundation
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

import java.util.Map;

import org.apache.tapestry5.commons.Resource;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.ioc.internal.util.ClasspathResource;
import org.apache.tapestry5.services.messages.PropertiesFileParser;
import org.testng.annotations.Test;

public class PropertiesFileParserImplTest extends InternalBaseTestCase
{
    @Test
    public void read_utf() throws Exception
    {
        Resource utf8 = new ClasspathResource("org/apache/tapestry5/internal/services/messages/utf8.properties");

        PropertiesFileParser parser = getService(PropertiesFileParser.class);

        Map<String, String> properties = parser.parsePropertiesFile(utf8);

        assertEquals(properties.get("tapestry"), "\u30bf\u30da\u30b9\u30c8\u30ea\u30fc");
        assertEquals(properties.get("version"), "5");
    }
}
