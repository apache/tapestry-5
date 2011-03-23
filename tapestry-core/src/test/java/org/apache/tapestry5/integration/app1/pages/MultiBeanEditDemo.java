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

import org.apache.tapestry5.annotations.ApplicationState;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.integration.app1.data.RolePath;
import org.apache.tapestry5.integration.app1.data.UserCredentials;

public class MultiBeanEditDemo
{
    @ApplicationState
    private UserCredentials credentials;

    @ApplicationState
    private RolePath rolePath;

    @InjectPage
    private MultiBeanDemoResult resultPage;

    @Component
    private Form form;

    public UserCredentials getCredentials()
    {
        return credentials;
    }

    public RolePath getRolePath()
    {
        return rolePath;
    }

    public void setCredentials(UserCredentials credentials)
    {
        this.credentials = credentials;
    }

    public void setRolePath(RolePath rolePath)
    {
        this.rolePath = rolePath;
    }

    Object onSuccess()
    {
        return resultPage;
    }

    void onActionFromClear()
    {
        // Force these to be re-created.
        credentials = null;
        rolePath = null;

        form.clearErrors();
    }
}
