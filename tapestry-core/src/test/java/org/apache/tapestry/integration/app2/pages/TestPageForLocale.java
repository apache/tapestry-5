// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry.integration.app2.pages;

import org.apache.tapestry.annotations.OnEvent;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.services.PersistentLocale;

import java.util.Locale;

public class TestPageForLocale
{
    @Inject
    private PersistentLocale persistentLocale;

    @OnEvent(component = "changeLocale")
    public void changeLocaleToFrench()
    {
        persistentLocale.set(Locale.FRENCH);
    }

}
