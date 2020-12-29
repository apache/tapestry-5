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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.DisableStrictChecks;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.http.Link;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ActionViaLinkDemo
{
    @Persist(PersistenceConstants.FLASH)
    private String message;

    @Inject
    private ComponentResources resources;

    Object[]
    onPassivate()
    {
        return new Object[]{};
    }

    public String getMessage()
    {
        return message;
    }

    void onUpdateMessage(String message)
    {
        this.message = message;
    }

    public String getActionURL()
    {
        Link link = resources.createEventLink("UpdateMessage", "from getActionURL()");

        return link.toURI();
    }
    
    @DisableStrictChecks
    void onActionFromNonExistent() 
    {
        
    }
    
    @DisableStrictChecks
    @OnEvent(value = "test", component = "nonExistent")
    void someEventHandlerMethod() 
    {
        
    }

}
