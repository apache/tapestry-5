// Copyright 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.kaptcha.integration;

import org.apache.tapestry5.test.SeleniumTestCase;
import org.testng.annotations.Test;

public class KaptchaIntegrationTest extends SeleniumTestCase
{

    @Test
    public void kaptcha()
    {
        open("/KaptchaDemo");

        type("kf", "invalid input");

        clickAndWait(SUBMIT);

        assertTextPresent("Enter the text displayed in the image.");

        type("kf", "i8cookies");

        clickAndWait(SUBMIT);

        assertTextPresent("Kaptcha passed.");

    }


    @Test
    public void bean_editor()
    {
        open("/kaptchabeaneditformdemo");

        type("kaptcha", "invalid input");

        clickAndWait(SUBMIT);

        assertTextPresent("You must provide a value for Name", "You must provide a value for Password", "Enter the text displayed in the image.");

        type("name", "admin");
        type("password", "secret");
        type("kaptcha", "i8cookies");

        clickAndWait(SUBMIT);

        assertTextPresent("Kaptcha passed.");

    }
}
