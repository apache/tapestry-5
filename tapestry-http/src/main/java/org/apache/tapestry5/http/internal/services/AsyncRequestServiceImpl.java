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
package org.apache.tapestry5.http.internal.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.http.AsyncRequestHandler;
import org.apache.tapestry5.http.AsyncRequestHandlerResponse;
import org.apache.tapestry5.http.internal.AsyncRequestService;

/**
 * Service that handles Tapestry's support for asynchronous Servlet API requests.
 */
public class AsyncRequestServiceImpl implements AsyncRequestService
{
    
    final private List<AsyncRequestHandler> handlers;

    public AsyncRequestServiceImpl(List<AsyncRequestHandler> handlers) 
    {
        super();
        this.handlers = handlers;
    }

    public AsyncRequestHandlerResponse handle(HttpServletRequest request, HttpServletResponse response)  
    {
        
        AsyncRequestHandlerResponse handlerResponse = AsyncRequestHandlerResponse.notHandled();
        
        for (AsyncRequestHandler asyncRequestHandler : handlers) 
        {
            handlerResponse = asyncRequestHandler.handle(request, response);
            if (handlerResponse.isAsync())
            {
                break;
            }
        }
        
        return handlerResponse;
    }
    
}
