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
    private double _sum;

    private double _value;

    @Inject
    private Block _newRow;

    @Inject
    private PageRenderSupport _pageRenderSupport;

    @Component
    private FormInjector _formInjector;

    public double getSum()
    {
        return _sum;
    }

    public double getValue()
    {
        return _value;
    }

    public void setValue(double value)
    {
        _value = value;
    }

    void onPrepareForSubmit()
    {
        _sum = 0;
    }

    void onAfterSubmit()
    {
        _sum += _value;
    }


    void afterRender()
    {
        _pageRenderSupport.addScript(
                "$('addnewrow').observe('click', function() { $('%s').trigger(); return false; });",
                _formInjector.getClientId());
    }

    Object onActionFromFormInjector()
    {
        return _newRow;
    }
}
