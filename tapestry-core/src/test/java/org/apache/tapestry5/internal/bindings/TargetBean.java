// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.bindings;

import org.apache.tapestry5.annotations.BeforeRenderBody;
import org.apache.tapestry5.beaneditor.ReorderProperties;
import org.apache.tapestry5.beaneditor.Validate;

@ReorderProperties("readonly,foobar,writeonly,objectvalue")
public class TargetBean extends DefaultComponent
{
    private String objectValue;

    private int intValue;

    String writeOnly;

    private StringHolder stringHolder = new StringHolderImpl();

    public StringHolder getStringHolder()
    {
        return stringHolder;
    }

    @BeforeRenderBody
    public StringHolder stringHolderMethod()
    {
        return stringHolder;
    }

    public void voidMethod()
    {

    }

    public int getIntValue()
    {
        return intValue;
    }

    public void setIntValue(int intValue)
    {
        this.intValue = intValue;
    }

    @Validate("getObjectValue")
    public String getObjectValue()
    {
        return objectValue;
    }

    @Validate("setObjectValue")
    public void setObjectValue(String objectValue)
    {
        this.objectValue = objectValue;
    }

    @Validate("writeonly")
    public void setWriteOnly(String value)
    {
        writeOnly = value;
    }

    @Validate("readonly")
    public String getReadOnly()
    {
        return "ReadOnly";
    }
}
