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

import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;

public class Target
{
    private Object[] activationContext;

    @Persist("flash")
    private Object[] componentContext;

    private Object object;

    @OnEvent("passivate")
    public Object[] getActivationContext()
    {
        return activationContext;
    }

    public Object[] getComponentContext()
    {
        return componentContext;
    }

    @OnEvent("activate")
    public void setActivationContext(Object[] activationContext)
    {
        this.activationContext = activationContext;
    }

    void onAction(Object[] componentContext)
    {
        this.componentContext = componentContext;
    }

    public Object[] getContextToEncode()
    {
        return new Object[] { "fred", "barney", "clark kent", "fred/barney", "\u592A\u90CE" };
    }

    public Object getObject()
    {
        return object;
    }

    public void setObject(Object object)
    {
        this.object = object;
    }
}
