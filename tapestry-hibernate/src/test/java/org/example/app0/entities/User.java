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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class User
{
	// NOTE: Hibernate doesn't understand the '_' syntax. It will end up putting underscores on all the properties
    @Id @GeneratedValue
    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String encodedPassword;

    @Version
    private int version;

    public String getEmail()
    {
        return email;
    }

    public String getEncodedPassword()
    {
        return encodedPassword;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public Long getId()
    {
        return id;
    }

    public String getLastName()
    {
        return lastName;
    }

    public int getVersion()
    {
        return version;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public void setEncodedPassword(String encodedPassword)
    {
        this.encodedPassword = encodedPassword;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }
}
