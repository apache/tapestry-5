// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.PersistentLocale;
import org.apache.tapestry5.services.Request;

import java.util.Locale;

public class Localization
{
    @Inject
    private Messages messages;


    @Inject
    private Locale locale;

    @Inject
    private Request request;

    @Inject
    private PersistentLocale persistentLocale;

    public Locale getLocale()
    {
        return locale;
    }

    public Request getRequest()
    {
        return request;
    }

    public String getInjectedMessage()
    {
        return messages.get("via-inject");
    }


    public void onActionFromFrench()
    {
        persistentLocale.set(Locale.FRENCH);
    }

    public void onActionFromEnglish()
    {
        persistentLocale.set(Locale.ENGLISH);
    }

}
