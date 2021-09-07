// Copyright 2011 The Apache Software Foundation
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
package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.StaticActivationContextValue;

public class StaticActivationContextValueDemo
{
    @Property
    private String state;
    
    @Property
    final private String COMPLETED = "completed"; 

    @Property
    final private String CLOSED = "closed"; 

    @Property
    final private String RESET = "reset"; 

    @Property
    final private String NONE = "none";
    
    @OnEvent(EventConstants.ACTIVATE)
    void completed(@StaticActivationContextValue(COMPLETED) String ignored)
    {
        state = COMPLETED;
    }

    @OnEvent(EventConstants.ACTIVATE)
    void closed(@StaticActivationContextValue(CLOSED) String ignored)
    {
        state = CLOSED;
    }

    @OnEvent(EventConstants.ACTIVATE)
    void reset(@StaticActivationContextValue(RESET) String ignored)
    {
        state = NONE;
    }
    
    void onActivate()
    {
        if (state == null)
        {
            state = NONE;
        }
    }
    
    public Object onPassivate()
    {
        return state != null && !NONE.equals(state) ? new String[] {state} : null;
    }
    
}
