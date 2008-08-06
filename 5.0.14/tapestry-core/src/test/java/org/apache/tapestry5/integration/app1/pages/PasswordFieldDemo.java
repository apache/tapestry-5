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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Retain;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.PasswordField;
import org.apache.tapestry5.integration.app1.services.UserAuthenticator;
import org.apache.tapestry5.ioc.annotations.Inject;

public class PasswordFieldDemo
{
    @Retain
    private String userName;

    // Normally not retained, just want to prove that the output value is always the blank string.
    @Retain
    private String password;

    @Inject
    private UserAuthenticator authenticator;

    @Component(id = "password")
    private PasswordField passwordField;

    @Component
    private Form form;

    String onSuccess()
    {
        if (!authenticator.isValid(userName, password))
        {
            form.recordError(passwordField, "Invalid user name or password.");
            return null;
        }

        return "PostLogin";
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

}
