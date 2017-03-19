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
package org.apache.tapestry5.integration.app1.components;

import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.PublishEvent;
import org.apache.tapestry5.json.JSONObject;

public class PublishEventDemoComponent
{
    @OnEvent("answer")
    @PublishEvent
    JSONObject answer() {
        return new JSONObject("origin", "componentAnswer");
    }
    
    @PublishEvent
    JSONObject onAction()
    {
        return new JSONObject("origin", "componentAction");
    }
    
}
