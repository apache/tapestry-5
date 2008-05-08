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

import java.util.Date;

public class BirthdayReminder
{
    private String name;

    private Date date;

    @Validate("required")
    public String getName()
    {
        return name;
    }

    @Validate("required")
    public Date getDate()
    {
        return date;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

}
