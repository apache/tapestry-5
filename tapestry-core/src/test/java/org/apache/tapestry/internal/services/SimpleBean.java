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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.beaneditor.Width;

public class SimpleBean
{
    private String _firstName;

    private String _lastName;

    private int _age;

    public String getFirstName()
    {
        return _firstName;
    }

    public String getLastName()
    {
        return _lastName;
    }

    @Width(2)
    public int getAge()
    {
        return _age;
    }

    public void setAge(int age)
    {
        _age = age;
    }

    public void setFirstName(String firstName)
    {
        _firstName = firstName;
    }

    public void setLastName(String lastName)
    {
        _lastName = lastName;
    }

}
