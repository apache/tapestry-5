// Copyright 2021 The Apache Software Foundation
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

package org.apache.tapestry5.services;

import org.apache.tapestry5.http.Link;
import org.easymock.EasyMock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import javax.servlet.http.HttpServletResponse;

public class HttpStatusTest
{
    
    private static final String TEST_URL = "http://example.com/something";
    
    private Link link;
    
    @BeforeMethod
    public void setupLink()
    {
        link = EasyMock.createMock(Link.class);
        EasyMock.expect(link.toRedirectURI()).andReturn(TEST_URL).times(0, 1);
        EasyMock.replay(link);
    }
    
    @AfterMethod
    public void afterTest()
    {
        EasyMock.verify(link);
    }
    
    @Test
    public void ok()
    {
        HttpStatus status = HttpStatus.ok();
        assertEquals(status.getStatusCode(), HttpServletResponse.SC_OK);
    }

    @Test
    public void created()
    {
        HttpStatus status = HttpStatus.created();
        assertEquals(status.getStatusCode(), HttpServletResponse.SC_CREATED);
    }
    
    @Test
    public void accepted()
    {
        HttpStatus status = HttpStatus.accepted();
        assertEquals(status.getStatusCode(), HttpServletResponse.SC_ACCEPTED);
    }

    @Test
    public void notFound()
    {
        HttpStatus status = HttpStatus.notFound();
        assertEquals(status.getStatusCode(), HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    public void forbidden()
    {
        HttpStatus status = HttpStatus.forbidden();
        assertEquals(status.getStatusCode(), HttpServletResponse.SC_FORBIDDEN);
    }
    
    @Test
    public void unauthorized()
    {
        HttpStatus status = HttpStatus.unauthorized();
        assertEquals(status.getStatusCode(), HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void badRequest()
    {
        HttpStatus status = HttpStatus.badRequest();
        assertEquals(status.getStatusCode(), HttpServletResponse.SC_BAD_REQUEST);
    }

    @Test
    public void seeOtherWithString()
    {
        HttpStatus status = HttpStatus.seeOther(TEST_URL);
        assertEquals(status.getStatusCode(), HttpServletResponse.SC_SEE_OTHER);
        assertEquals(getLocation(status), TEST_URL);
    }

    @Test
    public void seeOtherWithLink()
    {
        HttpStatus status = HttpStatus.seeOther(link);
        assertEquals(status.getStatusCode(), HttpServletResponse.SC_SEE_OTHER);
    }
    
    @Test
    public void movedPermanentlyWithString()
    {
        HttpStatus status = HttpStatus.movedPermanently(TEST_URL);
        assertEquals(status.getStatusCode(), HttpServletResponse.SC_MOVED_PERMANENTLY);
        assertEquals(getLocation(status), TEST_URL);
    }

    @Test
    public void movedPermanentlyWithLink()
    {
        HttpStatus status = HttpStatus.movedPermanently(link);
        assertEquals(status.getStatusCode(), HttpServletResponse.SC_MOVED_PERMANENTLY);
    }
    
    @Test
    public void temporaryRedirectWithString()
    {
        HttpStatus status = HttpStatus.temporaryRedirect(TEST_URL);
        assertEquals(status.getStatusCode(), HttpServletResponse.SC_FOUND);
        assertEquals(getLocation(status), TEST_URL);
    }

    @Test
    public void temporaryRedirectWithLink()
    {
        HttpStatus status = HttpStatus.temporaryRedirect(link);
        assertEquals(status.getStatusCode(), HttpServletResponse.SC_FOUND);
    }
    
    @Test
    public void withContentLocation()
    {
        HttpStatus status = HttpStatus.created().withContentLocation(TEST_URL);
        assertEquals(getContentLocation(status), TEST_URL);
    }

    private String getLocation(HttpStatus status) {
        return status.getExtraHttpHeaders().get("Location");
    }
    
    private String getContentLocation(HttpStatus status) {
        return status.getExtraHttpHeaders().get("Content-Location");
    }

}
