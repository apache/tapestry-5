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

public class PageContextInForm
{
    private Object[] _activationContext;

    private Object _object;

    void onActivate(Object[] context)
    {
        _activationContext = context;
    }

    Object[] onPassivate()
    {
        if (_activationContext != null)
        {
            return _activationContext;
        }
        else
        {
            return new Object[]{"betty", "wilma", "context with spaces", "context/with/slashes"};
        }
    }

    public Object[] getActivationContext()
    {
        return _activationContext;
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
