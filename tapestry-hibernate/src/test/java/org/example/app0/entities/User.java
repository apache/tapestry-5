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

package org.example.app0.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class User
{
    @Id
    private Long _id;

    private String _firstName;

    private String _lastName;

    private String _email;

    private String _encodedPassword;

    @Version
    private int _version;

    public String getEmail()
    {
        return _email;
    }

    public String getEncodedPassword()
    {
        return _encodedPassword;
    }

    public String getFirstName()
    {
        return _firstName;
    }

    public Long getId()
    {
        return _id;
    }

    public String getLastName()
    {
        return _lastName;
    }

    public int getVersion()
    {
        return _version;
    }

    public void setEmail(String email)
    {
        _email = email;
    }

    public void setEncodedPassword(String encodedPassword)
    {
        _encodedPassword = encodedPassword;
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
