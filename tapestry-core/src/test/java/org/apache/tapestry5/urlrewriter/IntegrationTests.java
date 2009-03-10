// Copyright 2006, 2007, 2008, 2009 The Apache Software Foundation
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
 */
@SuppressWarnings({ "JavaDoc" })
@Test(timeOut = 50000000, sequential = true)
public class IntegrationTests extends AbstractIntegrationTestSuite
{
    public IntegrationTests()
    {
        super("src/test/app3");
    }
    
    @Test
    public void test_url_rewriter() {
        
        open("struts");
        assertTextPresent("End of maze. URL rewriting works :).");

    }

}