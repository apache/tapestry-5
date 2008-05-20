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

package org.apache.tapestry.integration.app1.base;

import org.apache.tapestry.annotation.OnEvent;
import org.apache.tapestry.annotation.Persist;
import org.apache.tapestry.annotation.Property;
import org.apache.tapestry.ioc.internal.util.CollectionFactory;

import java.util.List;

public abstract class BaseEventHandlerDemo
{
    @Persist
    @Property
    private List<String> methodNames;

    protected final void addMethodName(String name)
    {
        List<String> methodNames = this.methodNames;

        if (methodNames == null) methodNames = CollectionFactory.newList();

        methodNames.add(name);

        this.methodNames = methodNames;
    }

    void onActivate(String placeholder)
    {
        methodNames = null;
    }

    @SuppressWarnings("unused")
    private void onAction()
    {
        addMethodName("parent.onAction()");
    }

    @SuppressWarnings("unused")
    private void onAction(String value)
    {
        addMethodName("parent.onAction(String)");

    }

    @OnEvent("action")
    void eventHandlerZero()
    {
        addMethodName("parent.eventHandlerZero()");
    }

    @OnEvent(value = "action")
    void eventHandlerOne(String value)
    {
        addMethodName("parent.eventHandlerOne(String)");
    }

}
