// Copyright 2007, 2009 The Apache Software Foundation
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

package org.apache.tapestry5.integration.pagelevel;

import org.apache.tapestry5.dom.Document;
import org.apache.tapestry5.integration.app2.services.LocaleAppModule;
import org.apache.tapestry5.test.PageTester;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PrefixMethodTest extends Assert
{
    private PageTester tester;

    @Test
    public void prefix_method() throws Exception
    {
        tester = new PageTester(TestConstants.APP2_PACKAGE, TestConstants.APP2_NAME, PageTester.DEFAULT_CONTEXT_PATH,
                                LocaleAppModule.class);

        Document doc = tester.renderPage("TestPrefixMethod");

        // make sure you can use on methods that have injected fields
        assertEquals(doc.getElementById("value2").getChildMarkup(), "42");
        assertEquals(doc.getElementById("value3").getChildMarkup(), "1");

        // should override the method in the superclass
        doc = tester.renderPage("TestPrefixMethod2");
        assertEquals(doc.getElementById("value").getChildMarkup(), "42");
    }
}
