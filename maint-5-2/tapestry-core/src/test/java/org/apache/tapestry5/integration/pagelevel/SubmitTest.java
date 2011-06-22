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
import org.apache.tapestry5.dom.Element;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.apache.tapestry5.test.PageTester;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

public class SubmitTest extends Assert
{
    private PageTester tester;

    private Document doc;

    private Map<String, String> fieldValues;

    @Test
    public void submit_form()
    {
        Element submitButton = doc.getElementById("capitalize1");
        assertEquals("submit", submitButton.getAttribute("type"));
        
        fieldValues.put("t1", "hello");
        doc = tester.clickSubmit(submitButton, fieldValues);
        assertTrue(doc.toString().contains("Value is: HELLO"));
    }

    @Test
    public void access_following_fields()
    {
        Element submitButton = doc.getElementById("capitalize2");
        fieldValues.put("t2", "world");
        doc = tester.clickSubmit(submitButton, fieldValues);
        assertTrue(doc.toString().contains("Value is: WORLD"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void not_a_submit()
    {
        Element submitButton = doc.getElementById("t1");

        tester.clickSubmit(submitButton, fieldValues);
    }

    @Test
    public void not_in_form()
    {
        try
        {
            Element submitButton = doc.getElementById("orphanedSubmit");

            tester.clickSubmit(submitButton, fieldValues);

            throw new RuntimeException("Should not be reachable.");
        }
        catch (RuntimeException ex)
        {
            assertEquals(ex.getMessage(), "Could not locate an ancestor element of type 'form'.");
        }
    }
    
    @Test
    public void render_image_type()
    {
        Element submitButton = doc.getElementById("submitImage");
        
        assertEquals("image", submitButton.getAttribute("type"));
        
        SymbolSource service = tester.getService(SymbolSource.class);
        
        String symbolValue = service.valueForSymbol("tapestry.spacer-image");
        
        String iconName = symbolValue.substring(symbolValue.lastIndexOf("/"));
        
        assertTrue(submitButton.getAttribute("src").contains(iconName));

    }

    @BeforeMethod
    public void before()
    {
        tester = new PageTester(TestConstants.APP2_PACKAGE, TestConstants.APP2_NAME);

        doc = tester.renderPage("TestPageForSubmit");
        fieldValues = CollectionFactory.newMap();
    }

    @AfterMethod
    public void after()
    {
        if (tester != null)
        {
            tester.shutdown();
        }
    }
}
