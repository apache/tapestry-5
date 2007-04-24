// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal.util;

import org.apache.tapestry.ioc.MessageFormatter;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.test.IOCTestCase;
import org.testng.annotations.Test;

public class MessagesImplTest extends IOCTestCase
{
    private final Messages _messages = MessagesImpl.forClass(TargetMessages.class);

    @Test
    public void contains_key()
    {
        assertTrue(_messages.contains("no-args"));
        assertFalse(_messages.contains("xyzzyx"));
    }

    @Test
    public void get_message_from_catalog()
    {
        assertEquals(_messages.get("no-args"), "No arguments.");
        assertEquals(_messages.get("something-failed"), "Something failed: %s");
    }

    @Test
    public void get_unknown_message_from_catalog()
    {
        assertEquals(_messages.get("does-not-exist"), "[[missing key: does-not-exist]]");
    }

    @Test
    public void format_message()
    {
        assertEquals(_messages.format("result", "good"), "The result is 'good'.");
    }

    @Test
    public void get_formatter()
    {
        MessageFormatter mf = _messages.getFormatter("result");

        assertEquals(mf.format("great"), "The result is 'great'.");
    }

    @Test
    public void formatters_are_cached()
    {
        MessageFormatter mf1 = _messages.getFormatter("result");
        MessageFormatter mf2 = _messages.getFormatter("result");

        assertSame(mf2, mf1);
    }

    @Test
    public void format_unknown_key()
    {
        assertEquals(_messages.format("rezult", "good"), "[[missing key: rezult]]");
    }
}
