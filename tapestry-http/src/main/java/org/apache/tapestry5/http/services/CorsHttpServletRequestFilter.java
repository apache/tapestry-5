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

package org.apache.tapestry5.http.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.http.CorsHandlerResult;
import org.apache.tapestry5.ioc.annotations.UsesOrderedConfiguration;

/**
 * {@linkplain HttpServletRequestFilter} that handles CORS. Uses an ordered configuration of
 * {@linkplain CorsHandler} instances.
 * @see CorsHandler
 * @since 5.8.2
 */
@UsesOrderedConfiguration(CorsHandler.class)
public class CorsHttpServletRequestFilter implements HttpServletRequestFilter
{

    final List<CorsHandler> handlers;
    
    public CorsHttpServletRequestFilter(List<CorsHandler> handlers) 
    {
        this.handlers = new ArrayList<>(handlers);
    }

    @Override
    public boolean service(HttpServletRequest request, HttpServletResponse response, HttpServletRequestHandler handler) throws IOException 
    {
        
        boolean serviced = false;
        for (CorsHandler corsHandler : handlers)
        {
            
            final CorsHandlerResult result = corsHandler.handle(request, response);
            if (result.equals(CorsHandlerResult.CONTINUE_REQUEST_PROCESSING)) 
            {
                break;
            }
            else if (result.equals(CorsHandlerResult.STOP_REQUEST_PROCESSING)) 
            {
                serviced = true;
                break;
            }
            
        }
        
        if (!serviced) 
        {
            serviced = handler.service(request, response);
        }
        
        return serviced;
    }

}
