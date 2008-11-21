//  Copyright 2008 The Apache Software Foundation
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

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.services.Response;
import org.testng.annotations.Test;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class JSONArrayEventResultProcessorTest extends InternalBaseTestCase
{
    @Test
    public void response_sent() throws IOException
    {
        String encoding = "UTF-8";
        Response response = mockResponse();

        CharArrayWriter writer = new CharArrayWriter();
        PrintWriter pw = new PrintWriter(writer);

        expect(response.getPrintWriter("application/json;charset=UTF-8")).andReturn(pw);

        replay();

        JSONArray array = new JSONArray("   [ \"fred\", \"barney\" \n\n]");

        JSONArrayEventResultProcessor p = new JSONArrayEventResultProcessor(response, encoding);

        p.processResultValue(array);

        verify();

        assertEquals(writer.toString(), "[\"fred\",\"barney\"]");
    }
}
