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

import org.apache.tapestry.annotation.Retain;
import org.apache.tapestry.ioc.annotation.Inject;
import org.apache.tapestry.ioc.internal.util.InternalUtils;
import org.example.testapp.services.Upcase;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

public class Start
{
    @Retain
    private String input;

    // We're matching on type here, just as we would a service provided in a T5 IoC module.
    @Inject
    private Upcase upcaseBean;

    @Inject
    private WebApplicationContext context;

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
        return InternalUtils.join(Arrays.asList(context.getBeanDefinitionNames()));
    }

}
