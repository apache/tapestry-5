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

package org.apache.tapestry.integration.app1.data;

import org.apache.tapestry.beaneditor.Validate;

public class UserCredentials
{
    private String _lastName;

    private String _firstName;

    @Validate("required")
    public String getFirstName()
    {
        return _firstName;
    }

    @Validate("required")
    public String getLastName()
    {
        return _lastName;
    }

    public void setLastName(String lastName)
    {
        _lastName = lastName;
    }

    public void setFirstName(String firstName)
    {
        _firstName = firstName;
    }

}
