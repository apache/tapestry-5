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

package org.apache.tapestry5.integration.app1.data;

import org.apache.tapestry5.beaneditor.DataType;
import org.apache.tapestry5.beaneditor.ReorderProperties;
import org.apache.tapestry5.beaneditor.Validate;
import org.apache.tapestry5.beaneditor.Width;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;

import java.util.List;

@ReorderProperties("firstname,lastname,birthyear,sex")
public class RegistrationData
{
    private String lastName;

    private String firstName;

    private int birthYear;

    private Sex sex = Sex.MALE;

    private boolean citizen;

    private String password;

    private String notes;

    private List<String> roles = CollectionFactory.newList();

    @Validate("min=1900,max=2007")
    @Width(4)
    public int getBirthYear()
    {
        return birthYear;
    }

    public Sex getSex()
    {
        return sex;
    }

    public String getFirstName()
    {
        return firstName;
    }

    @Validate("required,minlength=5")
    public String getLastName()
    {
        return lastName;
    }

    @Validate("required,minlength=6")
    @DataType("password")
    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public boolean isCitizen()
    {
        return citizen;
    }

    public void setBirthYear(int birthYear)
    {
        this.birthYear = birthYear;
    }

    @Validate("required,minlength=3")
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public void setSex(Sex sex)
    {
        this.sex = sex;
    }

    public void setCitizen(boolean citizen)
    {
        this.citizen = citizen;
    }

    @DataType("longtext")
    @Width(50)
    public String getNotes()
    {
        return notes;
    }

    public void setNotes(String notes)
    {
        this.notes = notes;
    }

    public List<String> getRoles()
    {
        return roles;
    }

    public void setRoles(List<String> roles)
    {
        this.roles = roles;
    }
}
