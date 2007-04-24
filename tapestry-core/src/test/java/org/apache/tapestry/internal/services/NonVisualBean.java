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

import org.apache.tapestry.beaneditor.NonVisual;

public class NonVisualBean
{
    private int _id;

    private String _name;

    @NonVisual
    public int getId()
    {
        return _id;
    }

    public String getName()
    {
        return _name;
    }

    public void setId(int id)
    {
        _id = id;
    }

    public void setName(String name)
    {
        _name = name;
    }
}
