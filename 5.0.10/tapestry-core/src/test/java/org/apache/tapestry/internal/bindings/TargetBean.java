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

package org.apache.tapestry.internal.bindings;

import org.apache.tapestry.annotations.BeforeRenderBody;
import org.apache.tapestry.beaneditor.OrderAfter;
import org.apache.tapestry.beaneditor.OrderBefore;

public class TargetBean extends DefaultComponent
{
    private String _objectValue;

    private int _intValue;

    String _writeOnly;

    private StringHolder _stringHolder = new StringHolderImpl();

    public StringHolder getStringHolder()
    {
        return _stringHolder;
    }

    @BeforeRenderBody
    public StringHolder stringHolderMethod()
    {
        return _stringHolder;
    }

    public void voidMethod()
    {

    }

    public int getIntValue()
    {
        return _intValue;
    }

    public void setIntValue(int intValue)
    {
        _intValue = intValue;
    }

    @OrderAfter("readOnly")
    public String getObjectValue()
    {
        return _objectValue;
    }

    @OrderAfter("writeOnly")
    public void setObjectValue(String objectValue)
    {
        _objectValue = objectValue;
    }

    @OrderAfter("foobar")
    public void setWriteOnly(String value)
    {
        _writeOnly = value;
    }

    @OrderBefore("writeOnly")
    public String getReadOnly()
    {
        return "ReadOnly";
    }
}
