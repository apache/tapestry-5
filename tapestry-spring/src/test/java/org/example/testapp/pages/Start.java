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

package org.example.testapp.pages;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Retain;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.example.testapp.services.SpringStatusProvider;
import org.example.testapp.services.Upcase;
import org.example.testapp.services.ViaFactory;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

public class Start
{
    @Retain
    private String input;

    // We're matching on type here, just as we would a service provided in a T5 IoC module.
    @Inject
    private Upcase upcaseBean;

    @Inject
    private ApplicationContext applicationContext;

    @Inject
    @Property
    private SpringStatusProvider statusProvider;

    @Inject
    @Property
    private ViaFactory viaFactory;

    void onSuccess()
    {
        input = upcaseBean.toUpperCase(input);
    }

    public String getInput()
    {
        return input;
    }

    public void setInput(String input)
    {
        this.input = input;
    }

    public String getSpringBeans()
    {
        return InternalUtils.join(Arrays.asList(applicationContext.getBeanDefinitionNames()));
    }
}
