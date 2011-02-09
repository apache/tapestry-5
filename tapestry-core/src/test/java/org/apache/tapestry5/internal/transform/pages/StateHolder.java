// Copyright 2007, 2008, 2011 The Apache Software Foundation
//
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

package org.apache.tapestry5.internal.transform.pages;

import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.internal.services.SimpleASO;

public class StateHolder
{
    @SessionState
    private SimpleASO bean;

    private boolean beanExists;

    public SimpleASO getBean()
    {
        return bean;
    }

    public void setBean(SimpleASO bean)
    {
        this.bean = bean;
    }

    public boolean getBeanExists()
    {
        return beanExists;
    }

}
