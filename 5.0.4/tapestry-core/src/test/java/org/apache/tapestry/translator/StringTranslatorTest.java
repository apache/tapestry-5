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

package org.apache.tapestry.translator;

import org.apache.tapestry.Translator;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.Messages;
import org.testng.annotations.Test;

/**
 * Tests for the {@link StringTranslator} class.
 */
public class StringTranslatorTest extends InternalBaseTestCase
{
    @Test
    public void from_client_normal() throws Exception
    {
        Messages messages = validationMessages();

        Translator<String> translator = new StringTranslator();

        assertEquals(translator.parseClient("abc", messages), "abc");
    }

    @Test
    public void from_client_blank_to_null() throws Exception
    {
        Messages messages = validationMessages();

        Translator<String> translator = new StringTranslator();

        assertNull(translator.parseClient("", messages));
    }

    @Test
    public void to_client_normal()
    {
        Translator<String> translator = new StringTranslator();

        assertEquals(translator.toClient("abc"), "abc");
    }

    @Test
    public void to_client_null_to_blank()
    {
        Translator<String> translator = new StringTranslator();

        assertEquals(translator.toClient(null), "");
    }
}
