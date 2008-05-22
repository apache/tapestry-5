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

package org.apache.tapestry5.internal;

import org.apache.tapestry5.beaneditor.ReorderProperties;

@ReorderProperties("third")
public class PropertyOrderBean
{
    private String first;

    private String second;

    private String third;

    public String getFirst()
    {
        return first;
    }

    public String getSecond()
    {
        return second;
    }

    public String getThird()
    {
        return third;
    }

    public void setFirst(String first)
    {
        this.first = first;
    }

    public void setSecond(String second)
    {
        this.second = second;
    }

    public void setThird(String third)
    {
        this.third = third;
    }
}
