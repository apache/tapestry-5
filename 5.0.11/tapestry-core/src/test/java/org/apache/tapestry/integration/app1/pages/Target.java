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

import org.apache.tapestry.annotations.OnEvent;
import org.apache.tapestry.annotations.Persist;

public class Target
{
    private Object[] _activationContext;

    @Persist("flash")
    private Object[] _componentContext;

    private Object _object;

    @OnEvent("passivate")
    public Object[] getActivationContext()
    {
        return _activationContext;
    }

    public Object[] getComponentContext()
    {
        return _componentContext;
    }

    @OnEvent("activate")
    public void setActivationContext(Object[] activationContext)
    {
        _activationContext = activationContext;
    }

    void onAction(Object[] componentContext)
    {
        _componentContext = componentContext;
    }

    public Object[] getContextToEncode()
    {
        return new Object[]{"fred", "barney", "clark kent", "fred/barney", "\u592A\u90CE"};
    }

    public Object getObject()
    {
        return _object;
    }

    public void setObject(Object object)
    {
        _object = object;
    }
}
