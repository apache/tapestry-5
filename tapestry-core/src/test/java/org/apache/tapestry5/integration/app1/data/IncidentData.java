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

package org.apache.tapestry5.integration.app1.data;

import org.apache.tapestry5.beaneditor.Validate;

import java.io.Serializable;

public class IncidentData implements Serializable
{
    private static final long serialVersionUID = -321606932140181054L;

    private String email;

    private String message;

    private boolean urgent;

    private String operatingSystem;

    private int hours;

    private Department department;

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getMessage()
    {
        return message;
    }

    @Validate("required")
    public void setMessage(String message)
    {
        this.message = message;
    }

    public boolean isUrgent()
    {
        return urgent;
    }

    public void setUrgent(boolean urgent)
    {
        this.urgent = urgent;
    }

    public String getOperatingSystem()
    {
        return operatingSystem;
    }

    public void setOperatingSystem(String os)
    {
        operatingSystem = os;
    }

    public int getHours()
    {
        return hours;
    }

    public void setHours(int hours)
    {
        this.hours = hours;
    }

    public Department getDepartment()
    {
        return department;
    }

    public void setDepartment(Department department)
    {
        this.department = department;
    }

}
