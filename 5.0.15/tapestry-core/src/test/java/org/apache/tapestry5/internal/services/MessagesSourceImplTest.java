// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.util.Map;

public class MessagesSourceImplTest extends Assert
{
    @Test
    public void read_utf() throws Exception
    {
        InputStream stream = getClass().getResourceAsStream("utf8.properties");

        Map<String, String> properties = MessagesSourceImpl.readPropertiesFromStream(stream);

        // Tapestry in Japanese is =??????

        assertEquals(properties.get("tapestry"), "\u30bf\u30da\u30b9\u30c8\u30ea\u30fc");
    }
}
