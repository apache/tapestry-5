// Copyright 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.test.TestBase;
import org.apache.tapestry5.services.PersistentLocale;
import org.testng.annotations.Test;

import java.util.Locale;

public class PersistentLocaleImplTest extends TestBase
{
    /**
     * TAP5-537
     */
    @Test
    public void set_to_unsupported_locale()
    {
        PersistentLocale pl = new PersistentLocaleImpl(null, "en,fr");

        try
        {
            pl.set(Locale.CHINESE);
            unreachable();
        }
        catch (IllegalArgumentException ex)
        {
            assertEquals(ex.getMessage(),
                         "Locale 'zh' is not supported by this application. Supported locales are 'en,fr'; this is configured via the tapestry.supported-locales symbol.");
        }

    }

}
