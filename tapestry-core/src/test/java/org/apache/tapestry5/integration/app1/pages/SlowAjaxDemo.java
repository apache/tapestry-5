// Copyright 2009 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.util.TextStreamResponse;

public class SlowAjaxDemo
{
    @Property
    private String zoneMessage;

    @InjectComponent
    private Zone zone;

    @Inject
    private ComponentResources resources;

    Object onActionFromLink()
    {
        zoneMessage = "Updated via an ActionLink";

        return zone.getBody();
    }

    Object onSuccessFromForm()
    {
        zoneMessage = "Updated via form submission.";

        return zone.getBody();
    }

    public Link getSlowScriptLink()
    {
        return resources.createEventLink("slowScript");
    }

    Object onSlowScript()
    {
        try
        {
            Thread.sleep(2 * 1000);
        }
        catch (Exception ex)
        {
        }

        return new TextStreamResponse("text/javascript",
                                      "document.write(\"<p id='slow'>Slow script loaded.</p>\");");

    }
}
