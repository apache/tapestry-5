// Copyright 2009 The Apache Software Foundation
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
package org.apache.tapestry5.urlrewriter;

import org.apache.tapestry5.test.AbstractIntegrationTestSuite;
import org.testng.annotations.Test;

/**
 * Note: If these tests fail with BindException when starting Jetty, it could be Skype. At least on my system, Skype is
 * listening on localhost:80.
 * <p/>
 * The commented-out tests needs to have somenicecomain.com and login.somenicedomain.com redirected to localhost in
 * order to work.
 */
@Test(timeOut = 30000, sequential = true)
public class IntegrationTests extends AbstractIntegrationTestSuite
{
    final public static String DOMAIN = "somenicedomain.com";

    final public static String LOGIN = "login";

    final public static String SUBDOMAIN = LOGIN + "." + DOMAIN;

    public IntegrationTests()
    {
        super("src/test/app5", DEFAULT_WEB_BROWSER_COMMAND, SUBDOMAIN, DOMAIN, "localhost");
    }

    @Test
    public void test_link_rewriting_without_virtual_host()
    {

        open(BASE_URL);
        assertAttribute("//a[@class='self']/@href", "/");
        assertAttribute("//a[@class='dummy']/@href", "/notdummy");

//        final String url = String.format("http://%s:%d/", SUBDOMAIN, JETTY_PORT);
//        assertAttribute("//a[@class='subdomain']/@href", url);

    }

//    @Test
//    public void test_url_rewriting_with_virtual_host()
//    {
//
//        final String url = String.format("http://%s:%d", SUBDOMAIN, JETTY_PORT);
//        open(url);
//        assertTextPresent("End of maze. URL rewriting works :).");
//        assertTextPresent("Login: " + LOGIN);
//
//    }

    @Test
    public void test_url_rewriter_without_virtual_host()
    {

        open("struts");
        assertTextPresent("End of maze. URL rewriting works :).");

    }

    @Test
    public void test_url_rewriter_using_context() throws InterruptedException
    {
        open("/RewriteWithContext");
        assertAttribute("//a[@class='page']/@href","/rpage/2sseccusetirwer");
        assertAttribute("//a[@class='event']/@href", "/cevent/txetnochtiwetirwer:kcilc");
        assertAttribute("//a[@class='action']/@href","/cevent/txetnochtiwetirwer.knilgnitcaeht");

        clickAndWait("//a[@class='page']");
        assertTextPresent("URL Rewriting that relies on the RewriteContext evidently works.");

        //note that we should be able to get back to rewritewithcontext via rpage encoding/decoding...
        open("/rpage/txetnochtiwetirwer");
        clickAndWait("//a[@class='event']");
        assertTextPresent("URL Rewriting that relies on the RewriteContext evidently works.");

        open("/rpage/txetnochtiwetirwer");
        clickAndWait("//a[@class='action']");
        assertTextPresent("URL Rewriting that relies on the RewriteContext evidently works.");
    }
}