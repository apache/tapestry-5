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

package org.apache.tapestry.integration.app1.pages;

import org.apache.tapestry.annotations.Service;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.services.PersistentLocale;
import org.apache.tapestry.services.Request;

import java.util.Locale;

public class Localization
{
    @Inject
    private Messages _messages;

    @Inject
    @Service("ClassFactory")
    private ClassFactory _iocClassFactory;

    @Inject
    @Service("ComponentClassFactory")
    private ClassFactory _componentClassFactory;

    @Inject
    private Locale _locale;

    @Inject
    private Request _request;

    @Inject
    private PersistentLocale _persistentLocale;

    public Locale getLocale()
    {
        return _locale;
    }

    public Request getRequest()
    {
        return _request;
    }

    public String getInjectedMessage()
    {
        return _messages.get("via-inject");
    }

    public ClassFactory getComponentClassFactory()
    {
        return _componentClassFactory;
    }

    public ClassFactory getIocClassFactory()
    {
        return _iocClassFactory;
    }

    public void onActionFromFrench()
    {
        _persistentLocale.set(Locale.FRENCH);
    }

    public void onActionFromEnglish()
    {
        _persistentLocale.set(Locale.ENGLISH);
    }

}
