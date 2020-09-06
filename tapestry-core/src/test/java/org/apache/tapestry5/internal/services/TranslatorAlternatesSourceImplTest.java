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

package org.apache.tapestry5.internal.services;

import java.util.Map;

import org.apache.tapestry5.Translator;
import org.apache.tapestry5.commons.util.CollectionFactory;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.testng.annotations.Test;

@SuppressWarnings("unchecked")
public class TranslatorAlternatesSourceImplTest extends InternalBaseTestCase
{
    @Test
    public void name_mismatch()
    {
        Translator t = mockTranslator();

        train_getName(t, "barney");

        Map<String, Translator> configuration = CollectionFactory.newMap();

        configuration.put("Fred", t);

        replay();

        try
        {
            new TranslatorAlternatesSourceImpl(configuration);
            unreachable();
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(),
                    "Contribution key 'Fred' does not match 'barney' (the name of the corresponding Translator).");
        }
    }
}
