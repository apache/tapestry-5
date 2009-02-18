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

package org.apache.tapestry5.integration.app2.pages;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Submit;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.ioc.annotations.Inject;

public class TestPageForSubmit
{
    @SuppressWarnings("unused")
    @Component
    private Form form1;

    @SuppressWarnings("unused")
    @Component
    private Form form2;

    @SuppressWarnings("unused")
    @Component
    private Submit capitalize1;

    @SuppressWarnings("unused")
    @Component
    private Submit capitalize2;

    @SuppressWarnings("unused")
    @Component(parameters = "value=value")
    private TextField t1;

    @SuppressWarnings("unused")
    @Component(parameters = "value=value")
    private TextField t2;
    
    @Property
    @Inject
    @Path("${tapestry.spacer-image}")
    private Asset spacerImage;

    @Persist
    private String value;

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    void onSelectedFromCapitalize1()
    {
        value = value.toUpperCase();
    }

    void onSelectedFromCapitalize2()
    {
        onSelectedFromCapitalize1();
    }

    void afterRender()
    {
        // Force the generation of client ids.
        capitalize1.getClientId();
        capitalize2.getClientId();
    }
}
