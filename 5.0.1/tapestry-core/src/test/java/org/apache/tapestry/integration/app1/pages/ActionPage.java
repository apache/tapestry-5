// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.integration.app1.pages;

import org.apache.tapestry.annotations.ComponentClass;
import org.apache.tapestry.annotations.Persist;

@ComponentClass
public class ActionPage
{
    private int _index;

    // Must be persistent, to survive from one request to the next.
    // An action request is always followed by a redirect request.
    @Persist
    private int _value;

    void onActionFromChoose(int value)
    {
        _value = value;
    }

    public int getIndex()
    {
        return _index;
    }

    public void setIndex(int index)
    {
        _index = index;
    }

    public int getValue()
    {
        return _value;
    }

    public String getLinkClass()
    {
        return _index == _value ? "selected" : null;
    }
}
