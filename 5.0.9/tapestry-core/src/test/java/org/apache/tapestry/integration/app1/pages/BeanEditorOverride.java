// Copyright 2008 The Apache Software Foundation
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
import org.apache.tapestry.corelib.components.BeanEditor;
import org.apache.tapestry.corelib.components.Form;
import org.apache.tapestry.integration.app1.data.RegistrationData;

public class BeanEditorOverride
{
    @Component
    private Form _form;

    @Component(parameters = {"object=registrationData"})
    private BeanEditor _editor;

    @ApplicationState
    private RegistrationData _data;

    public RegistrationData getRegistrationData()
    {
        return _data;
    }

    Object onSuccess()
    {
        return ViewRegistration.class;
    }

    void onActionFromClear()
    {
        _data = null;
        _form.clearErrors();
    }
}
