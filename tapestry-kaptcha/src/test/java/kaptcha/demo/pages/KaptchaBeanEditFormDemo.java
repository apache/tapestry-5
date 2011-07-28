// Copyright 2011 The Apache Software Foundation
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

package kaptcha.demo.pages;

import kaptcha.demo.model.RegistrationData;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.BeanEditForm;
import org.apache.tapestry5.kaptcha.annotations.Kaptcha;

public class KaptchaBeanEditFormDemo
{
    @Component(parameters = {"object=registrationData", "clientValidation=none"})
    private BeanEditForm form;

    @Property
    @Kaptcha
    private RegistrationData registrationData;

    @Property
    @Persist(PersistenceConstants.FLASH)
    private String message;

    void onSuccessFromForm()
    {
        message = "Kaptcha passed.";
    }
}
