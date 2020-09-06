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

import org.apache.tapestry5.http.internal.services.BaseURLSourceImpl;
import org.apache.tapestry5.http.services.BaseURLSource;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.internal.test.InternalBaseTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


public class BaseURLSourceImplTest extends InternalBaseTestCase
{
    private Request request;

    @BeforeMethod
    public void setUp()
    {
        request = mockRequest();
    }

    @Test
    public void server_name_from_request_object()
    {
        expect(request.getServerName()).andReturn("localhost").once();
        expect(request.getServerPort()).andReturn(80).once();
        expect(request.isSecure()).andReturn(false);

        replay();

        BaseURLSource baseURLSource = new BaseURLSourceImpl(request, "", 0, 0);
        assertEquals(baseURLSource.getBaseURL(false), "http://localhost");

        verify();
    }

    @Test
    public void contributed_hostname()
    {
        expect(request.getServerPort()).andReturn(80).once();
        expect(request.isSecure()).andReturn(false);

        replay();

        BaseURLSource baseURLSource = new BaseURLSourceImpl(request, "my.server.com", 0, 0);

        assertEquals(baseURLSource.getBaseURL(false), "http://my.server.com");

        verify();
    }

    @Test
    public void hostname_from_environment_variable()
    {
        expect(request.getServerPort()).andReturn(80).once();
        expect(request.isSecure()).andReturn(false);

        replay();

        BaseURLSource baseURLSource = new BaseURLSourceImpl(request, "$HOSTNAME", 0, 0);

        assertEquals(baseURLSource.getBaseURL(false), "http://" + System.getenv("HOSTNAME"));

        verify();
    }

    @Test
    public void insecure_url_using_default_port()
    {
        replay();

        BaseURLSource baseURLSource = new BaseURLSourceImpl(request, "localhost", 80, 443);

        assertEquals(baseURLSource.getBaseURL(false), "http://localhost");

        verify();
    }

    @Test
    public void secure_url_using_default_port()
    {
        replay();

        BaseURLSource baseURLSource = new BaseURLSourceImpl(request, "localhost", 80, 443);

        assertEquals(baseURLSource.getBaseURL(true), "https://localhost");

        verify();
    }

    @Test
    public void getBaseURLWithContributedNonStandardSecurePort()
    {
        replay();

        BaseURLSource baseURLSource = new BaseURLSourceImpl(request, "localhost", 80, 8443);

        assertEquals(baseURLSource.getBaseURL(true), "https://localhost:8443");

        verify();
    }

    @Test
    public void secure_url_without_configured_hostports()
    {
        expect(request.isSecure()).andReturn(false).once();

        replay();

        BaseURLSource baseURLSource = new BaseURLSourceImpl(request, "localhost", 0, 0);

        // In other words, in the absense of any other configuration, it assumes that you have SSL on port 443
        // and there's no need for that in the URL, since that's what the browser is going to do.
        assertEquals(baseURLSource.getBaseURL(true), "https://localhost");

        verify();
    }

}
