// Copyright 2006, 2007, 2008, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import java.util.Locale;

import org.apache.tapestry5.ioc.ScopeConstants;
import org.apache.tapestry5.ioc.annotations.Scope;
import org.apache.tapestry5.ioc.services.ThreadLocale;

@Scope(ScopeConstants.PERTHREAD)
public class ThreadLocaleImpl implements ThreadLocale
{
    private Locale locale = Locale.getDefault();

    @Override
    public Locale getLocale()
    {
        return locale;
    }

    @Override
    public void setLocale(Locale locale)
    {
        assert locale != null;

        this.locale = locale;
    }
}
