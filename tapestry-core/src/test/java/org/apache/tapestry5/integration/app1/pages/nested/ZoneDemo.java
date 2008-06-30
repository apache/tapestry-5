// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.pages.nested;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.annotations.ApplicationState;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.integration.app1.data.RegistrationData;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;

import java.util.Date;

public class ZoneDemo
{
    @Component
    private BeanEditForm form;

    private String name;

    @ApplicationState
    private RegistrationData registration;

    private static final String[] NAMES = { "Fred & Wilma", "Mr. <Roboto>", "Grim Fandango", "Registration" };

    @Inject
    private Block showName;

    @Inject
    private Block registrationForm;

    @Inject
    private Block registrationOutput;

    public String[] getNames()
    {
        return NAMES;
    }


    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Log
    Object onActionFromSelect(String name)
    {
        this.name = name;

        if (name.equals("Registration")) return registrationForm;

        return showName;
    }

    Object onSuccess()
    {
        return registrationOutput;
    }

    Object onActionFromClear()
    {
        form.clearErrors();
        registration = null;

        return registrationForm;
    }

    public RegistrationData getRegistration()
    {
        return registration;
    }

    Object onActionFromJSON()
    {
        JSONObject response = new JSONObject();

        response.put("content", "Directly coded JSON content");

        return response;
    }

    public Date getCurrentTime()
    {
        return new Date();
    }

    public ValueEncoder<String> getEncoder()
    {
        return new StringValueEncoder();
    }
}
