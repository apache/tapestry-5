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

package org.apache.tapestry.integration.app1.pages;

import org.apache.tapestry.Block;
import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.Component;
import org.apache.tapestry.corelib.components.BeanEditForm;
import org.apache.tapestry.integration.app1.data.RegistrationData;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.json.JSONObject;
import org.slf4j.Logger;

public class ZoneDemo
{
    @Component
    private BeanEditForm _form;

    @Inject
    private Logger _logger;

    private String _name;

    @ApplicationState
    private RegistrationData _registration;

    private static final String[] NAMES = { "Fred & Wilma", "Mr. <Roboto>", "Grim Fandango", "Registration" };

    @Inject
    private Block _showName;

    @Inject
    private Block _registrationForm;

    @Inject
    private Block _registrationOutput;

    public String[] getNames()
    {
        return NAMES;
    }


    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    Object onActionFromSelect(String name)
    {
        _name = name;

        _logger.info("Selected: '" + _name + "'");


        if (name.equals("Registration")) return _registrationForm;

        return _showName;
    }

    Object onSuccess()
    {
        return _registrationOutput;
    }

    Object onActionFromClear()
    {
        _form.clearErrors();
        _registration = null;

        return _registrationForm;
    }

    public RegistrationData getRegistration()
    {
        return _registration;
    }

    Object onActionFromJSON()
    {
        JSONObject response = new JSONObject();

        response.put("content", "Directly coded JSON content");

        return response;
    }
}
