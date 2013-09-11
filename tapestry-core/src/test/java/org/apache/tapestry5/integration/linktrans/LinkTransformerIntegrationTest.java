// Copyright 2010-2013 The Apache Software Foundation
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

package org.apache.tapestry5.integration.linktrans;

import org.apache.tapestry5.integration.app1.App1TestCase;
import org.testng.annotations.Test;

public class LinkTransformerIntegrationTest extends App1TestCase
{
    @Test
    public void page_render_links()
    {
        openLinks("View Toys");

        assertText("content", "toys");
        assertText("count", "0");

        clickAndWait("link=back to index");
        clickAndWait("link=View Games");

        assertText("content", "games");
    }
    
    @Test
    public void component_event_links()
    {
        openLinks("en");
        
        assertText("currentLocale", "en");
        
        clickAndWait("link=View Games");
        assertText("content", "games");
        
        clickAndWait("link=increment");
        
        assertText("content", "games");
        assertText("count", "1");
    }
}
