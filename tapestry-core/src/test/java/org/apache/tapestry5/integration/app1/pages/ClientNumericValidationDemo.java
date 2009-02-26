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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PersistentLocale;

import java.util.Locale;

public class ClientNumericValidationDemo
{
    @Persist
    @Property
    private long longValue;

    @Persist
    @Property
    private double doubleValue;

    @Inject
    private PersistentLocale persistentLocale;

    void onActionFromReset()
    {
        longValue = 1000;
        doubleValue = 1234.67;

        persistentLocale.set(Locale.ENGLISH);
    }

    void onActionFromGerman()
    {
        persistentLocale.set(Locale.GERMAN);
    }
}
