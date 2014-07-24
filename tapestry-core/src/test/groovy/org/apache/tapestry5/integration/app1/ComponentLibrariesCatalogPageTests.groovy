package org.apache.tapestry5.integration.app1

import org.apache.tapestry5.integration.TapestryCoreTestCase
import org.apache.tapestry5.test.TapestryTestConfiguration
import org.testng.annotations.Test

@TapestryTestConfiguration(webAppFolder = "src/test/app1")
class ComponentLibrariesCatalogPageTests extends TapestryCoreTestCase
{
    @Test(enabled = false)
    void component_libraries_page()
    {
        open("${baseURL}t5dashboard/componentlibraries")
        
        // header
        assertEquals "2 component libraries used", getText("//h1")
        
        // component library list
        assertEquals 2, getXpathCount("//ul[@id='libraryList']/li")
        assertEquals getText("//ul[@id='libraryList']/li[1]/a"), "core : Tapestry 5 Core Library"
        assertEquals getText("//ul[@id='libraryList']/li[1]/p"), "The set of components, pages and mixins provided by Tapestry out-of-the-box."
        assertEquals getText("//ul[@id='libraryList']/li[2]/a"), "lib/alpha"
        assertFalse isElementPresent("//ul[@id='libraryList']/li[2]/p")
        assertEquals getText("//ul[@id='libraryList']/li[1]/p[@class='tags']"), "Tags: core out-of-the-box"
        assertFalse isElementPresent("//ul[@id='libraryList']/li[2]/p[@class='tags']")
        
        // component library information
        
        // with ComponentLibraryInfo
        assertEquals getText("css=#core h2"), "core : Tapestry 5 Core Library"
        assertInfoLink "http://tapestry.apache.org", "css=#core .homepage"
        assertInfoLink "http://tapestry.apache.org/documentation.html", "css=#core .documentationUrl"
        assertInfoLink "http://tapestry.apache.org/current/apidocs/", "css=#core .javadocUrl"
        assertInfoLink "https://git-wip-us.apache.org/repos/asf?p=tapestry-5.git;a=summary", "css=#core .sourceBrowseUrl"
        assertInfoLink "https://git-wip-us.apache.org/repos/asf?p=tapestry-5.git;a=blob;f=tapestry-core/src/main/java", "css=#core .sourceRootUrl"
        assertInfoLink "https://issues.apache.org/jira/browse/TAP5", "css=#core .issueTrackerUrl"
        assertEquals getText("css=#core .dependencyInformation"), "Group id org.apache.tapestry, artifact id tapestry-core, version. 5.4.0 \n More information at Maven Central Respository"
        assertEquals getAttribute("css=#core .dependencyInformation a@href"), "http://search.maven.org/#artifactdetails|org.apache.tapestry|tapestry-core|version=5.4.0|jar"
        assertEquals getAttribute("//div[@id='core']//td/a[text()='JavaDoc']@href"), "http://tapestry.apache.org/current/apidocs/org/apache/tapestry5/corelib/components/ActionLink.html"
        assertEquals getAttribute("//div[@id='core']//td/a[text()='Source']@href"), "https://git-wip-us.apache.org/repos/asf?p=tapestry-5.git;a=blob;f=tapestry-core/src/main/java/org/apache/tapestry5/corelib/components/ActionLink.java"
        assertFalse isElementPresent("css=#core p.noInformation")
        
        // without ComponentLibraryInfo
        assertEquals "lib/alpha", getText("css=#lib-alpha h2")
        assertEquals getText("css=#lib-alpha p.noInformation"), "No additional information provided for lib/alpha."
        
        // table row
        assertEquals getText("//div[@id='lib-alpha']//table/tbody/tr[2]/td[1]"), "lib/alpha/Root"
        assertEquals getText("//div[@id='lib-alpha']//table/tbody/tr[2]/td[2]"), "Alpha root page"
        assertEquals getText("//div[@id='lib-alpha']//table/tbody/tr[2]/td[3]"), "alpha root page"
        assertEquals getText("//div[@id='lib-alpha']//table/tbody/tr[2]/td[4]"), "Not informed"
        assertEquals getText("//div[@id='lib-alpha']//table/tbody/tr[2]/td[5]"), "Not informed"
        

    }
    
    def assertInfoLink(String url, String locator)
    {
        assertEquals getText(locator), url 
        assertEquals getAttribute(locator + " a@href"), url
    }
    
}
