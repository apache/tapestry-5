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

import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.integration.app1.data.Department;
import org.apache.tapestry.internal.TapestryInternalUtils;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.annotations.Inject;

public class RadioDemo
{
    @Persist
    private Department _department;

    @Persist
    private String _position;

    private Department _loopValue;

    @Inject
    private Messages _messages;

    public Department[] getDepartments()
    {
        return Department.values();
    }

    public Department getDepartment()
    {
        return _department;
    }

    public String getPosition()
    {
        return _position;
    }

    public Department getLoopValue()
    {
        return _loopValue;
    }

    public void setDepartment(Department department)
    {
        _department = department;
    }

    public void setPosition(String position)
    {
        _position = position;
    }

    public void setLoopValue(Department loopValue)
    {
        _loopValue = loopValue;
    }

    public String getLabel()
    {
        return TapestryInternalUtils.getLabelForEnum(_messages, _loopValue);
    }
}
