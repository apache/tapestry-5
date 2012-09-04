package org.apache.tapestry5.test;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PageTesterTest extends Assert
{

    @Test
    public void setupRequestFromURI_accepts_query_strings()
    {
        PageTester pageTester = new PageTester("org.example.app1", "app1");

        pageTester.setupRequestFromURI("/foo/somePage?param=value");

    }

}
