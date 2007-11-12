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

package org.apache.tapestry.integration.app1.data;

import org.apache.tapestry.beaneditor.Validate;

import java.io.Serializable;

public class IncidentData implements Serializable
{
    private static final long serialVersionUID = -321606932140181054L;

    private String _email;

    private String _message;

    private boolean _urgent;

    private String _operatingSystem;

    private int _hours;

    private Department _department;

    public String getEmail()
    {
        return _email;
    }

    public void setEmail(String email)
    {
        _email = email;
    }

    public String getMessage()
    {
        return _message;
    }

    @Validate("required")
    public void setMessage(String message)
    {
        _message = message;
    }

    public boolean isUrgent()
    {
        return _urgent;
    }

    public void setUrgent(boolean urgent)
    {
        _urgent = urgent;
    }

    public String getOperatingSystem()
    {
        return _operatingSystem;
    }

    public void setOperatingSystem(String os)
    {
        _operatingSystem = os;
    }

    public int getHours()
    {
        return _hours;
    }

    public void setHours(int hours)
    {
        _hours = hours;
    }

    public Department getDepartment()
    {
        return _department;
    }

    public void setDepartment(Department department)
    {
        _department = department;
    }

}
