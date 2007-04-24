// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.internal.transform.pages;

import java.util.Date;

import org.apache.tapestry.annotations.ComponentClass;

@ComponentClass
public class FindFieldClass
{
    private boolean _booleanValue;

    private int[] _intArrayValue;

    private String _stringValue;

    private Date[] _dateArrayValue;

    public boolean isBooleanValue()
    {
        return _booleanValue;
    }

    public void setBooleanValue(boolean booleanValue)
    {
        _booleanValue = booleanValue;
    }

    public Date[] getDateArrayValue()
    {
        return _dateArrayValue;
    }

    public void setDateArrayValue(Date[] dateArrayValue)
    {
        _dateArrayValue = dateArrayValue;
    }

    public int[] getIntArrayValue()
    {
        return _intArrayValue;
    }

    public void setIntArrayValue(int[] intArrayValue)
    {
        _intArrayValue = intArrayValue;
    }

    public String getStringValue()
    {
        return _stringValue;
    }

    public void setStringValue(String stringValue)
    {
        _stringValue = stringValue;
    }

}
