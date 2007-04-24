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
import org.apache.tapestry.ValidationException;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.Messages;
import org.testng.annotations.Test;

public class IntegerTranslatorTest extends InternalBaseTestCase
{
    @Test
    public void parse_invalid_format()
    {
        Messages messages = validationMessages();

        Translator<Integer> translator = new IntegerTranslator();

        try
        {
            translator.parseClient("xyz", messages);
            unreachable();
        }
        catch (ValidationException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "The input value 'xyz' is not parseable as an integer value.");
        }
    }

    @Test
    public void blank_parses_to_null() throws Exception
    {
        Messages messages = validationMessages();

        Translator<Integer> translator = new IntegerTranslator();

        assertNull(translator.parseClient("", messages));
    }

    @Test
    public void null_converts_to_client_as_blank()
    {

        Translator<Integer> translator = new IntegerTranslator();

        assertEquals(translator.toClient(null), "");
    }

    @Test
    public void convert_non_null()
    {

        Translator<Integer> translator = new IntegerTranslator();

        assertEquals(translator.toClient(37), "37");
    }

    @Test
    public void successful_parse_from_client() throws Exception
    {
        Messages messages = validationMessages();

        Translator<Integer> translator = new IntegerTranslator();

        assertEquals(translator.parseClient("-23823", messages), new Integer(-23823));
    }

    @Test
    public void parse_ignores_trimmed_whitespace() throws Exception
    {
        Messages messages = validationMessages();

        Translator<Integer> translator = new IntegerTranslator();

        assertEquals(translator.parseClient(" -123 ", messages), new Integer(-123));
    }
}
