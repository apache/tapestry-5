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

import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.annotations.Component;
import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.corelib.components.Form;
import org.apache.tapestry.integration.app1.data.RolePath;
import org.apache.tapestry.integration.app1.data.UserCredentials;

public class MultiBeanEditDemo
{
    @ApplicationState
    private UserCredentials _credentials;

    @ApplicationState
    private RolePath _rolePath;

    @InjectPage
    private MultiBeanDemoResult _resultPage;

    @Component
    private Form _form;

    public UserCredentials getCredentials()
    {
        return _credentials;
    }

    public RolePath getRolePath()
    {
        return _rolePath;
    }

    public void setCredentials(UserCredentials credentials)
    {
        _credentials = credentials;
    }

    public void setRolePath(RolePath rolePath)
    {
        _rolePath = rolePath;
    }

    Object onSuccess()
    {
        return _resultPage;
    }

    void onActionFromClear()
    {
        // Force these to be re-created.
        _credentials = null;
        _rolePath = null;

        _form.clearErrors();
    }
}
