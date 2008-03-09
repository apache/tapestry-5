// Copyright 2007, 2008 The Apache Software Foundation
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

import org.apache.tapestry.beaneditor.*;

public class RegistrationData
{
    private String _lastName;

    private String _firstName;

    private int _birthYear;

    private Sex _sex = Sex.MALE;

    private boolean _citizen;

    private String _password;

    private String _notes;

    @OrderAfter("lastName")
    @Validate("min=1900,max=2007")
    @Width(4)
    public int getBirthYear()
    {
        return _birthYear;
    }

    @OrderAfter("lastname,birthyear")
    public Sex getSex()
    {
        return _sex;
    }

    @OrderBefore("lastname")
    public String getFirstName()
    {
        return _firstName;
    }

    @Validate("required,minlength=5")
    public String getLastName()
    {
        return _lastName;
    }

    @Validate("required,minlength=6")
    @DataType("password")
    public String getPassword()
    {
        return _password;
    }

    public void setPassword(String password)
    {
        _password = password;
    }

    public boolean isCitizen()
    {
        return _citizen;
    }

    public void setBirthYear(int birthYear)
    {
        _birthYear = birthYear;
    }

    @Validate("required,minlength=3")
    public void setFirstName(String firstName)
    {
        _firstName = firstName;
    }

    public void setLastName(String lastName)
    {
        _lastName = lastName;
    }

    public void setSex(Sex sex)
    {
        _sex = sex;
    }

    public void setCitizen(boolean citizen)
    {
        _citizen = citizen;
    }

    @DataType("longtext")
    @Width(50)
    public String getNotes()
    {
        return _notes;
    }

    public void setNotes(String notes)
    {
        _notes = notes;
    }
}
