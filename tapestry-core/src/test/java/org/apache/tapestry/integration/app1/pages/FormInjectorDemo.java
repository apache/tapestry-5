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

import org.apache.tapestry.Block;
import org.apache.tapestry.PageRenderSupport;
import org.apache.tapestry.annotations.Component;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.corelib.components.FormInjector;
import org.apache.tapestry.ioc.annotations.Inject;

public class FormInjectorDemo
{
    @Persist
    private double sum;

    private double value;

    @Inject
    private Block newRow;

    @Inject
    private PageRenderSupport pageRenderSupport;

    @Component
    private FormInjector formInjector;

    public double getSum()
    {
        return sum;
    }

    public double getValue()
    {
        return value;
    }

    public void setValue(double value)
    {
        this.value = value;
    }

    void onPrepareForSubmit()
    {
        sum = 0;
    }

    void onAfterSubmit()
    {
        sum += value;
    }


    void afterRender()
    {
        pageRenderSupport.addScript(
                "$('addnewrow').observe('click', function() { $('%s').trigger(); return false; });",
                formInjector.getClientId());
    }

    Object onActionFromFormInjector()
    {
        return newRow;
    }
}
