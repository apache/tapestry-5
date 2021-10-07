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
package org.apache.tapestry5.integration.app1.base;

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.ActivationContextParameter;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.RequestBody;
import org.apache.tapestry5.annotations.RequestParameter;
import org.apache.tapestry5.annotations.RestInfo;
import org.apache.tapestry5.annotations.StaticActivationContextValue;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.HttpStatus;
import org.apache.tapestry5.util.TextStreamResponse;

public class BaseRestDemoPage extends AbstractRestDemoPage {
    
    public static final String EXTRA_HTTP_HEADER = "X-Event";
    
    public static final String SUBPATH = "something";
    
    final protected static TextStreamResponse createResponse(String eventName, String body, String parameter)
    {
        String content = eventName + ":" + parameter + (body == null ? "" : ":" + body);
        return new TextStreamResponse("text/plain", content) 
        {
            @Override
            public void prepareResponse(Response response) {
                super.prepareResponse(response);
                response.addHeader(EXTRA_HTTP_HEADER, eventName);
            }
            
        };
    }
    
    @OnEvent(EventConstants.HTTP_GET)
    public Object superclassEndpoint(@StaticActivationContextValue("superclassEndpoint") String pathParameter)
    {
        final JSONArray jsonArray = new JSONArray();
        jsonArray.add(pathParameter);
        return jsonArray;
    }
    
    @OnEvent(EventConstants.HTTP_GET)
    public Object withParameters(
            @StaticActivationContextValue("parametersTest") String staticParameter,
            @RequestParameter(value = "fromQueryString", allowBlank = true) String queryParameter,
            @ActivationContextParameter("pathParameter") String pathParameter)
    {
        return new JSONObject("staticParameter", staticParameter, 
                "pathParameter", pathParameter, 
                "queryParameter", queryParameter);
    }
    
    @OnEvent(EventConstants.HTTP_PUT)
    @RestInfo(produces = "text/plain")
    public Object returningHttpStatus(
            @StaticActivationContextValue("returningHttpStatus") String ignored,
            @RequestBody String parameter)
    {
        return new HttpStatus(HttpServletResponse.SC_CREATED, parameter)
                .withContentLocation(parameter + ".txt")
                .withHttpHeader("ETag", parameter + ".etag");
    }
    
    @OnEvent(EventConstants.HTTP_PUT)
    @RestInfo(consumes = "text/plain")
    public Object returningHttpStatusSimple(
            @StaticActivationContextValue("returningHttpStatusSimple") String ignored,
            @RequestBody String parameter)
    {
        return new HttpStatus(HttpServletResponse.SC_CREATED);
    }
    
}
