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
import org.apache.tapestry5.integration.app1.base.BaseEventHandlerDemo;

public class EventHandlerDemo extends BaseEventHandlerDemo
{
    @SuppressWarnings("unused")
    private void onAction()
    {
        addMethodName("child.onAction()");
    }

    @SuppressWarnings("unused")
    private void onAction(String value)
    {
        addMethodName("child.onAction(String)");
    }

    @SuppressWarnings("unused")
    private void onActionFromFred()
    {
        addMethodName("child.onActionFromFred()");
    }

    @SuppressWarnings("unused")
    private void onActionFromFred(String value)
    {
        addMethodName("child.onActionFromFred(String)");
    }

    @OnEvent(value = "action")
    void eventHandlerZeroChild()
    {
        addMethodName("child.eventHandlerZeroChild()");
    }

    @OnEvent(value = "action")
    void eventHandlerOneChild(String value)
    {
        addMethodName("child.eventHandlerOneChild()");
    }

    @OnEvent(component = "fred")
    void eventHandlerForFred()
    {
        addMethodName("child.eventHandlerForFred()");
    }

    public Object[] getTwoContext()
    {
        return new Object[] { 1, 2 };
    }
}
