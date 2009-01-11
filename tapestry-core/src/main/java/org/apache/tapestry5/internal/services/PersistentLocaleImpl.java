// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.services.PersistentLocale;

import java.util.Locale;

@Scope(ScopeConstants.PERTHREAD)
public class PersistentLocaleImpl implements PersistentLocale
{
    private Locale locale;

    public void set(Locale locale)
    {
        this.locale = Defense.notNull(locale, "locale");
    }

    public Locale get()
    {
        return locale;
    }

    public boolean isSet()
    {
        return locale != null;
    }
}
