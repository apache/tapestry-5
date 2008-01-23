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

import org.apache.tapestry.ValueEncoder;
import org.apache.tapestry.internal.services.StringValueEncoder;

import java.util.Date;
import java.util.List;

public class DisabledFields
{
    private String _stringValue;

    private boolean _flag;

    private Date _date;

    private List<String> _values;

    public String getStringValue()
    {
        return _stringValue;
    }

    public void setStringValue(String stringValue)
    {
        _stringValue = stringValue;
    }

    public boolean isFlag()
    {
        return _flag;
    }

    public void setFlag(boolean flag)
    {
        _flag = flag;
    }

    public Date getDate()
    {
        return _date;
    }

    public void setDate(Date date)
    {
        _date = date;
    }

    public List<String> getValues()
    {
        return _values;
    }

    public void setValues(List<String> values)
    {
        _values = values;
    }

    public ValueEncoder getEncoder()
    {
        return new StringValueEncoder();
    }
}
