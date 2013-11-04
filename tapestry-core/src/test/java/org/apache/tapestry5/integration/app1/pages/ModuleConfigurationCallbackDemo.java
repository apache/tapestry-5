//  Copyright 2013 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.services.javascript.ModuleConfigurationCallback;

/**
 * Demonstrates the use of {@link ModuleConfigurationCallback} and
 * {@link JavaScriptSupport#addModuleConfigurationCallback(ModuleConfigurationCallback)}.
 * Based on http://www.requirejs.org/jqueryui-amd/example/webapp/app.html.
 */
public class ModuleConfigurationCallbackDemo
{

    @Inject
    private JavaScriptSupport javaScriptSupport;
    
    void afterRender() {
        javaScriptSupport.addModuleConfigurationCallback(new Callback());
    }

    private static final class Callback implements ModuleConfigurationCallback
    {
        public JSONObject configure(JSONObject configuration)
        {
            return configuration.put("waitSeconds", "13");
        }
    }

}
