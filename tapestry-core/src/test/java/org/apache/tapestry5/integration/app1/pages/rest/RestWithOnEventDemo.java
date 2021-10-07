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
package org.apache.tapestry5.integration.app1.pages.rest;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.RequestBody;
import org.apache.tapestry5.annotations.RestInfo;
import org.apache.tapestry5.annotations.StaticActivationContextValue;
import org.apache.tapestry5.integration.app1.base.BaseRestDemoPage;
import org.apache.tapestry5.util.TextStreamResponse;

public class RestWithOnEventDemo extends BaseRestDemoPage {
    
    @OnEvent(EventConstants.HTTP_GET)
    Object get(@StaticActivationContextValue(SUBPATH) String subpath, String parameter)
    {
        return createResponse(EventConstants.HTTP_GET, null, parameter);
    }

    @OnEvent(value = EventConstants.HTTP_DELETE)
    Object delete(@StaticActivationContextValue(SUBPATH) String subpath, String parameter) 
    {
        return createResponse(EventConstants.HTTP_DELETE, null, parameter);
    }

    @OnEvent(EventConstants.HTTP_HEAD)
    Object head(@StaticActivationContextValue(SUBPATH) String subpath, String parameter)
    {
        return createResponse(EventConstants.HTTP_HEAD, null, parameter);
    }

    @OnEvent(EventConstants.HTTP_PATCH)
    @RestInfo(consumes = "application/json")
    Object patch(
            @StaticActivationContextValue(SUBPATH) String subpath, 
            String parameter, 
            @RequestBody String body)
    {
        return createResponse(EventConstants.HTTP_PATCH, body, parameter);
    }

    @OnEvent(EventConstants.HTTP_POST)
    @RestInfo(consumes = "application/json")
    Object post(
            @StaticActivationContextValue(SUBPATH) String subpath, 
            String parameter, 
            @RequestBody String body)
    {
        return createResponse(EventConstants.HTTP_POST, body, parameter);
    }

    @OnEvent(EventConstants.HTTP_PUT)
    @RestInfo(consumes = "application/json")
    Object put(
            @StaticActivationContextValue(SUBPATH) String subpath, 
            String parameter, 
            @RequestBody String body)
    {
        return createResponse(EventConstants.HTTP_PUT, body, parameter);
    }
    
    @OnEvent(EventConstants.HTTP_GET)
    @Override
    public Object superclassEndpoint(@StaticActivationContextValue("superclassEndpoint") String parameter)
    {
        return new TextStreamResponse("text/plain", parameter + " overridden!");
    }

}
