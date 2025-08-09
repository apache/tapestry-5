// Copyright 2017, 2025 The Apache Software Foundation
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

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.PublishEvent;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

public class PublishEventDemo
{

    @Inject
    private JavaScriptSupport javaScriptSupport;
    
    @Inject
    @Symbol(SymbolConstants.REQUIRE_JS_ENABLED)
    private boolean requireJsEnabled;
    
    @Inject
    @Path("PublishEventDemo.js")
    private Asset publishEventDemoAmdAsset;
    
    void beginRender()
    {
        if (requireJsEnabled)
        {
            javaScriptSupport.importStack("core");
            javaScriptSupport.importJavaScriptLibrary(publishEventDemoAmdAsset);
        }
        else
        {
            javaScriptSupport.importEsModule("publish-event-demo");
        }
    }

    @PublishEvent
    JSONObject onAction()
    {
        return new JSONObject("origin", "pageAction");
    }
    
    @OnEvent("answer")
    @PublishEvent
    JSONObject answer() {
        return new JSONObject("origin", "pageAnswer");
    }
    
}
