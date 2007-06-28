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

package org.apache.tapestry.internal.services;

import java.util.Collections;
import java.util.Map;

import org.apache.tapestry.Translator;
import org.apache.tapestry.internal.test.InternalBaseTestCase;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;
import org.apache.tapestry.services.TranslatorSource;
import org.testng.annotations.Test;

public class TranslatorSourceImplTest extends InternalBaseTestCase
{
    @Test
    public void found_translator_by_name()
    {
        Translator translator = mockTranslator();

        Map<String, Translator> configuration = Collections.singletonMap("mock", translator);

        replay();

        TranslatorSource source = new TranslatorSourceImpl(configuration);

        assertSame(source.get("mock"), translator);

        verify();
    }

    @Test
    public void unknown_translator_is_failure()
    {
        Translator fred = mockTranslator();
        Translator barney = mockTranslator();

        Map<String, Translator> configuration = CollectionFactory.newMap();

        configuration.put("fred", fred);
        configuration.put("barney", barney);

        replay();

        TranslatorSource source = new TranslatorSourceImpl(configuration);

        try
        {
            source.get("wilma");
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(
                    ex.getMessage(),
                    "Unknown translator type 'wilma'.  Configured translators are barney, fred.");
        }

    }
}
