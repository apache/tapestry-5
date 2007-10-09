// Copyright 2007 The Apache Software Foundation
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

import org.apache.tapestry.annotations.Component;
import org.apache.tapestry.annotations.Retain;
import org.apache.tapestry.corelib.components.Form;
import org.apache.tapestry.corelib.components.PasswordField;
import org.apache.tapestry.integration.app1.services.UserAuthenticator;
import org.apache.tapestry.ioc.annotations.Inject;

public class PasswordFieldDemo
{
    @Retain
    private String _userName;

    // Normally not retained, just want to prove that the output value is always the blank string.
    @Retain
    private String _password;

    @Inject
    private UserAuthenticator _authenticator;

    @Component(id = "password")
    private PasswordField _passwordField;

    @Component
    private Form _form;

    String onSuccess()
    {
        if (!_authenticator.isValid(_userName, _password))
        {
            _form.recordError(_passwordField, "Invalid user name or password.");
            return null;
        }

        return "PostLogin";
    }

    public String getPassword()
    {
        return _password;
    }

    public void setPassword(String password)
    {
        _password = password;
    }

    public String getUserName()
    {
        return _userName;
    }

    public void setUserName(String userName)
    {
        _userName = userName;
    }

}
