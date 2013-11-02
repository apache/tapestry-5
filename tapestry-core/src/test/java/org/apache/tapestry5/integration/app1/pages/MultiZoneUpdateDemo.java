// Copyright 2009-2013 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.Block;
import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.ajax.MultiZoneUpdate;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
import org.apache.tapestry5.services.ajax.JavaScriptCallback;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import java.util.Date;

public class MultiZoneUpdateDemo
{
    @Inject
    private Block fredBlock, barneyBlock;

    @Property
    private String wilmaMessage = "Wilma Flintstone";

    @InjectComponent
    private Zone wilmaZone;

    @Inject
    private AjaxResponseRenderer ajaxResponseRenderer;

    public Date getNow()
    {
        return new Date();
    }

    Object onActionFromUpdate()
    {
        wilmaMessage = "His Wife, Wilma.";

        // Do one the new way
        ajaxResponseRenderer.addRender("fred", fredBlock);

        ajaxResponseRenderer.addCallback(new JavaScriptCallback()
        {
            public void run(JavaScriptSupport javascriptSupport)
            {
                javascriptSupport.require("app/multi-zone-update").with("message", "Updated");
            }
        });

        // Do the rest the old way, to test backwards compatibility

        return new MultiZoneUpdate("barney", barneyBlock).add("dino", "His dog, Dino.")
                .add(wilmaZone);
    }

    public String[] getOptions()
    {
        return new String[]
                {"Red", "Green", "Blue"};
    }

    public ValueEncoder getEncoder()
    {
        return new StringValueEncoder();
    }
}
