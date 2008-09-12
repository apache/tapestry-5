// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.pages.nested;

import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;

public class ActionDemo
{
    @Property
    private Long number;

    @OnEvent(component = "actionlink", value = "action")
    public void onAction(Long number)
    {
        this.number = number;
    }

    public void onActivate(Long number)
    {
        this.number = number;
    }

    public Long onPassivate()
    {
        return number;
    }
}
