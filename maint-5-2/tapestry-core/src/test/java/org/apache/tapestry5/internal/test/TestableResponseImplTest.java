// Copyright 2010 The Apache Software Foundation
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
package org.apache.tapestry5.internal.test;

import javax.servlet.http.HttpServletResponse;

import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.integration.app2.pages.TestPageForActionLinkWithStream;
import org.apache.tapestry5.integration.app2.pages.TestPageForHttpError;
import org.apache.tapestry5.integration.app2.pages.TestPageForHttpHeaders;
import org.apache.tapestry5.integration.app2.pages.TestPageForRedirectURL;
import org.apache.tapestry5.integration.app2.pages.TestPageForServletOutputStream;
import org.apache.tapestry5.integration.pagelevel.TestConstants;
import org.apache.tapestry5.test.PageTester;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestableResponseImplTest extends Assert
{
    private PageTester tester;
    
    @Test
    public void servlet_output_stream()
    {
        TestableResponse response = tester.renderPageAndReturnResponse(TestPageForServletOutputStream.class.getSimpleName());
        
        assertEquals(response.getOutput(), "<html><body>Rendered with TextStreamResponse</body></html>");
    }

    @Test
    public void http_headers()
    {
        Document document = tester.renderPage(TestPageForHttpHeaders.class.getSimpleName());
        
        assertTrue(document.toString().contains("Test page for HTTP headers"));
        
        TestableResponse response = tester.getService(TestableResponse.class);
        
        assertEquals(response.getHeader(TestPageForHttpHeaders.DATE_HEADER_NAME), 12345L);
        assertEquals(response.getHeader(TestPageForHttpHeaders.INT_HEADER_NAME), 6789);
        assertEquals(response.getHeader(TestPageForHttpHeaders.STRING_HEADER_NAME), "foo-bar-baz-barney");
    }
    
    
    @Test
    public void http_error()
    {
        TestableResponse response = tester.renderPageAndReturnResponse(TestPageForHttpError.class.getSimpleName());
        
        assertEquals(response.getStatus(), HttpServletResponse.SC_MOVED_TEMPORARILY);
        assertEquals(response.getErrorMessage(), "Oups! Resource moved. Try again later.");
    }
    
    @Test
    public void redirect_url()
    {
        TestableResponse response = tester.renderPageAndReturnResponse(TestPageForRedirectURL.class.getSimpleName());
        
        assertEquals(response.getRedirectURL(), TestPageForRedirectURL.REDIRECT_URL);
    }
    
    @Test
    public void action_link()
    {
        Document document = tester.renderPage(TestPageForActionLinkWithStream.class.getSimpleName());
        
        Element link = document.getElementById("mylink");
        
        assertNotNull(link);
        
        TestableResponse response = tester.clickLinkAndReturnResponse(link);
        assertEquals(response.getOutput(), "<html><body>Rendered with TextStreamResponse</body></html>");
    }
    
    @BeforeMethod
    public void before()
    {
        tester = new PageTester(TestConstants.APP2_PACKAGE, TestConstants.APP2_NAME);
    }
}
