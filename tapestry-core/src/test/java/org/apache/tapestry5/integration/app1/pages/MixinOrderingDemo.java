// Copyright 2009 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.corelib.components.TextField;
import org.apache.tapestry5.integration.app1.mixins.EchoValue;
import org.apache.tapestry5.integration.app1.mixins.EchoValue2;
import org.apache.tapestry5.integration.app1.components.TextOnlyOnDisabledTextField;

/**
 * Page for demonstrating/testing the use of the BindParameter mixin demonstration.
 */
public class MixinOrderingDemo
{

    @Property
    private String myproperty;

    @Component
    @Mixins("echovalue2::before:echovalue3")
    @MixinClasses(value={EchoValue.class},order={"after:echovalue2;after:echovalue3"})
    private TextField order3;

    @Component
    @Mixins("echovalue2::after:echovalue")
    @MixinClasses(EchoValue.class)
    private TextField order4;

    @Component
    @Mixins("echovalue2")
    @MixinClasses(value=EchoValue.class,order={"after:echovalue2"})
    private TextField order5;


    @Component
    @Mixins("echovalue3::before:echovalue2")
    @MixinClasses(value=EchoValue2.class,order="after:echovalue")
    private TextOnlyOnDisabledTextField order6;

    @SetupRender
    void initMyprop()
    {
        myproperty="batman";
    }
}