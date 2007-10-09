// Copyright 2006, 2007 The Apache Software Foundation
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

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.annotations.InjectPage;
import org.apache.tapestry.annotations.OnEvent;
import org.apache.tapestry.integration.app1.services.French;
import org.apache.tapestry.integration.app1.services.Greeter;
import org.apache.tapestry.ioc.annotations.Inject;
import org.apache.tapestry.ioc.annotations.Symbol;
import org.apache.tapestry.services.BindingSource;
import org.apache.tapestry.services.Request;

public class InjectDemo
{
    // Named --- now demonstrating case insensitivity
    // Now vestigial!
    @Inject
    private Request _request;

    @Inject
    @Symbol("app.injected-symbol")
    private String _injectedSymbol;

    // Via ComponentResourcesInjectionProvider
    @Inject
    private ComponentResources _resources;

    // Via ??? -- have to ensure that BindingSource
    // stays unique.
    @Inject
    private BindingSource _bindingSource;

    @InjectPage
    private Fred _fred;

    // Again, demonstrates case insensitivity
    @InjectPage("barney")
    private Runnable _barney;

    @Inject
    @French
    private Greeter _greeter;

    public String getGreeting()
    {
        return _greeter.getGreeting();
    }

    public BindingSource getBindingSource()
    {
        return _bindingSource;
    }

    public Request getRequest()
    {
        return _request;
    }

    public ComponentResources getResources()
    {
        return _resources;
    }

    @OnEvent(component = "fred")
    Object clickFred()
    {
        return _fred;
    }

    @OnEvent(component = "barney")
    Object clickBarney()
    {
        return _barney;
    }

    @OnEvent(component = "wilma")
    String clickWilma()
    {
        return "Wilma";
    }

    public String getInjectedSymbol()
    {
        return _injectedSymbol;
    }
}
