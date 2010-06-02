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

package org.apache.tapestry5.integration.linktrans.components;

import java.util.Locale;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Flow;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.LocalizationSetter;

public class Layout
{
    @Inject
    private LocalizationSetter ls;

    @Property
    private String localeName;

    public Flow<String> getLocaleNames()
    {
        return F.flow(ls.getSupportedLocales()).map(F.<Locale> stringValueOf());
    }

    void onLocale(String localeName)
    {
        ls.setLocaleFromLocaleName(localeName);
    }
}
