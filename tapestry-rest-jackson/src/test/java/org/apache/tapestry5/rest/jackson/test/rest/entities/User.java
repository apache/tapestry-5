// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.apache.tapestry5.rest.jackson.test.rest.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User 
{

    private String email;

    private String name;
    
    private List<Attribute> attributes = new ArrayList<>();

    public String getEmail() 
    {
        return email;
    }

    public void setEmail(String email) 
    {
        this.email = email;
    }

    public String getName() 
    {
        return name;
    }

    public void setName(String name) 
    {
        this.name = name;
    }
    
    public List<Attribute> getAttributes() 
    {
        return attributes;
    }
    
    public void setAttributes(List<Attribute> attributes) 
    {
        this.attributes = attributes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof User)) {
            return false;
        }
        User other = (User) obj;
        return Objects.equals(email, other.email) && Objects.equals(name, other.name);
    }
    
}
