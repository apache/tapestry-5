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
import org.apache.tapestry5.annotations.RequestBody;
import org.apache.tapestry5.annotations.RestInfo;
import org.apache.tapestry5.annotations.StaticActivationContextValue;
import org.apache.tapestry5.integration.app1.base.BaseRestDemoPage;

public class RestWithEventHandlerMethodNameDemo extends BaseRestDemoPage {
    
    Object onHttpGet(@StaticActivationContextValue(SUBPATH) String subpath, String parameter)
    {
        return createResponse(EventConstants.HTTP_GET, null, parameter);
    }

    Object onHttpDelete(@StaticActivationContextValue(SUBPATH) String subpath, String parameter) 
    {
        return createResponse(EventConstants.HTTP_DELETE, null, parameter);
    }

    Object onHttpHead(@StaticActivationContextValue(SUBPATH) String subpath, String parameter)
    {
        return createResponse(EventConstants.HTTP_HEAD, null, parameter);
    }

    @RestInfo(consumes = "text/plain")
    Object onHttpPatch(
            @StaticActivationContextValue(SUBPATH) String subpath, 
            String parameter, 
            @RequestBody String body)
    {
        return createResponse(EventConstants.HTTP_PATCH, body, parameter);
    }

    @RestInfo(consumes = "text/plain")
    Object onHttpPost(
            @StaticActivationContextValue(SUBPATH) String subpath, 
            String parameter, 
            @RequestBody String body)
    {
        return createResponse(EventConstants.HTTP_POST, body, parameter);
    }

    @RestInfo(consumes = "text/plain")
    Object onHttpPut(
            @StaticActivationContextValue(SUBPATH) String subpath, 
            String parameter, 
            @RequestBody String body)
    {
        return createResponse(EventConstants.HTTP_PUT, body, parameter);
    }

}
