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

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.integration.app1.data.Department;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;

public class RadioDemo
{
    @Persist
    private Department department;

    @Persist
    private String position;

    private Department loopValue;

    @Inject
    private Messages messages;

    public Department[] getDepartments()
    {
        return Department.values();
    }

    public Department getDepartment()
    {
        return department;
    }

    public String getPosition()
    {
        return position;
    }

    public Department getLoopValue()
    {
        return loopValue;
    }

    public void setDepartment(Department department)
    {
        this.department = department;
    }

    public void setPosition(String position)
    {
        this.position = position;
    }

    public void setLoopValue(Department loopValue)
    {
        this.loopValue = loopValue;
    }

    public String getLabel()
    {
        return TapestryInternalUtils.getLabelForEnum(messages, loopValue);
    }
}
