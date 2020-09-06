//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.http.internal.services.ResponseImpl;
import org.apache.tapestry5.http.services.Response;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.testng.annotations.Test;

public class ResponseImplTest extends InternalBaseTestCase
{
    @Test
    public void addHeader()
    {
        final String HEADER_NAME = "Header-Name";
        final String[] HEADER_VALUES = {"asdfasdf", "FHDFGH", "####"};
        
        final HttpServletResponse httpServletResponse = mockHttpServletResponse();
        final HttpServletRequest httpServletRequest = mockHttpServletRequest();
        
        Response response = new ResponseImpl(httpServletRequest, httpServletResponse);
        for (String value : HEADER_VALUES)
        {
            httpServletResponse.addHeader(HEADER_NAME, value);
        }
        
        replay();
    
        for (String value : HEADER_VALUES)
        {
            response.addHeader(HEADER_NAME, value);
        }
        
        verify();
        
    }
    
}
