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

package org.apache.tapestry.internal;

import org.apache.tapestry.beaneditor.Order;

public class PropertyOrderBean
{
    private String _first;

    private String _second;

    private String _third;

    public String getFirst()
    {
        return _first;
    }

    public String getSecond()
    {
        return _second;
    }

    @Order(-1)
    public String getThird()
    {
        return _third;
    }

    public void setFirst(String first)
    {
        _first = first;
    }

    public void setSecond(String second)
    {
        _second = second;
    }

    public void setThird(String third)
    {
        _third = third;
    }
}
