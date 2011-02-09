// Copyright 2008, 2010, 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.apache.tapestry5.services.BaseURLSource;
import org.apache.tapestry5.services.Request;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class BaseURLSourceImplTest extends InternalBaseTestCase {
    private Request request;

    @BeforeMethod
    public void setUp() {
	request = mockRequest();
    }
    
    @Test
    public void getBaseURLFromRequest() {
        expect(request.getServerName()).andReturn("localhost").once();
        expect(request.getServerPort()).andReturn(80).once();
        expect(request.isSecure()).andReturn(false).once();
        replay();
        BaseURLSource baseURLSource = new BaseURLSourceImpl(request, "localhost", 0, 0);
        assertEquals(baseURLSource.getBaseURL(false), "http://localhost");
    }

    @Test
    public void getBaseURLWithContributedHostname() {
        expect(request.getServerPort()).andReturn(80).once();
        expect(request.isSecure()).andReturn(false).once();
        replay();
        BaseURLSource baseURLSource = new BaseURLSourceImpl(request, "my.server.com", 0, 0);
        assertEquals(baseURLSource.getBaseURL(false), "http://my.server.com");
    }
    
    @Test
    public void getBaseURLWithEnvHostname() {
        expect(request.getServerPort()).andReturn(80).once();
        expect(request.isSecure()).andReturn(false).once();
        replay();
        BaseURLSource baseURLSource = new BaseURLSourceImpl(request, "$HOSTNAME", 0, 0);
        assertEquals(baseURLSource.getBaseURL(false), "http://" + System.getenv("HOSTNAME"));
    }
    
    @Test
    public void getBaseURLWithContributedValuesDontUseRequest() {
        replay();
        BaseURLSource baseURLSource = new BaseURLSourceImpl(request, "localhost", 80, 443);
        assertEquals(baseURLSource.getBaseURL(false), "http://localhost");
    }    
    
    @Test
    public void getBaseURLWithContributedNonStandardSecurePort() {
        replay();
        BaseURLSource baseURLSource = new BaseURLSourceImpl(request, "localhost", 80, 8443);
        assertEquals(baseURLSource.getBaseURL(true), "https://localhost:8443");
    }    
    
}
