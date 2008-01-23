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

package org.apache.tapestry.internal.transform;

import org.apache.tapestry.annotations.ApplicationState;
import org.apache.tapestry.internal.services.SimpleASO;

public class StateHolder
{
    @ApplicationState
    private SimpleASO _bean;

    private boolean _beanExists;

    public SimpleASO getBean()
    {
        return _bean;
    }

    public void setBean(SimpleASO bean)
    {
        _bean = bean;
    }

    public boolean getBeanExists()
    {
        return _beanExists;
    }

}
