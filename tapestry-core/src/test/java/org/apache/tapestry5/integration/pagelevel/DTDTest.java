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
import org.apache.tapestry5.test.PageTester;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DTDTest extends Assert
{
    private static final String FRAMESET = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Frameset//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\">";

    private static final String TRANSITIONAL = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">";

    private static final String STRICT = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";

    @DataProvider
    public Object[][] dtd_page_provider()
    {
        return new Object[][]
                {
                        { "DTDFromPage", FRAMESET, "slagheap", },
                        { "DTDFromComponent", TRANSITIONAL, "flubber", },
                        { "MultipleDTD", STRICT, "blubber", },
                        { "NoDTD", "", "no_dtd_loser", } };
    }

    @Test(dataProvider = "dtd_page_provider")
    public void verify_correct_dtds(String pageName, String expectedDTD, String checkText)
    {
        PageTester tester = new PageTester(TestConstants.APP2_PACKAGE, TestConstants.APP2_NAME);

        Document doc = tester.renderPage(pageName);
        String txt = doc.toString();
        // use startsWith to make sure the DTD is getting into the right spot.
        assertTrue(txt.startsWith(expectedDTD));
        // we should also make sure that the other DTD's don't appear anywhere else...
        checkOtherDTD(txt, expectedDTD);
        // spot check the body of the pages to make sure they correctly rendered...
        // they should have, based on the unit tests for template rendering, but...
        assertTrue(txt.contains(checkText));

    }

    private void checkOtherDTD(String txt, String expected)
    {
        if (expected.equals(TRANSITIONAL))
        {
            check(txt, FRAMESET, STRICT);
        }
        else if (expected.equals(FRAMESET))
        {
            check(txt, STRICT, TRANSITIONAL);
            ;
        }
        else if (expected.equals(STRICT))
        {
            check(txt, FRAMESET, TRANSITIONAL);
        }
        else if (expected.equals(""))
        {
            check(txt, FRAMESET, STRICT, TRANSITIONAL);
        }
        else
        {
            throw new RuntimeException("Unknown expected string: " + expected);
        }
    }

    private void check(String txt, String... invalids)
    {
        for (String invalid : invalids)
        {
            assertFalse(txt.contains(invalid));
        }
    }

}
