// Copyright 2013 The Apache Software Foundation
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
package org.apache.tapestry5.cdi.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.enterprise.inject.spi.Extension;

import org.antlr.runtime.Lexer;
import org.apache.commons.codec.StringEncoder;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.tapestry5.TapestryFilter;
import org.apache.tapestry5.cdi.CDIInjectModule;
import org.apache.tapestry5.cdi.extension.BeanManagerHolder;
import org.apache.tapestry5.cdi.extension.TapestryExtension;
import org.apache.tapestry5.cdi.test.components.DumbComponent;
import org.apache.tapestry5.cdi.test.pages.DessertPage;
import org.apache.tapestry5.cdi.test.pages.Index;
import org.apache.tapestry5.cdi.test.pages.InvalidateSessionPage;
import org.apache.tapestry5.cdi.test.pages.RequestScopePage;
import org.apache.tapestry5.cdi.test.pages.SessionScopePage;
import org.apache.tapestry5.cdi.test.pages.SomePage;
import org.apache.tapestry5.cdi.test.pages.StatefulPage;
import org.apache.tapestry5.cdi.test.pages.StereotypePage;
import org.apache.tapestry5.cdi.test.pages.VegetablePage;
import org.apache.tapestry5.cdi.test.pages.WSPage;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.ioc.IOCConstants;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.modules.TapestryModule;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.ziplock.JarLocation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.webapp30.WebAppDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class InjectTest {

    @ArquillianResource
    private static URL indexUrl;

    private static final String TEST_RESOURCES_ROOT_PATH = "src/test/resources/";
    
    private static final String METAINF_PATH = "src/main/resources/META-INF/";
        		
    /**
     * Generate a web archive for arquillian
     * @return a WebArchive object
     */
    @Deployment(testable = false)
    public static WebArchive war() {
    
    	File indexPage = new File(toPath(Index.class.getName()));
    	Package rootPackage = toPackage(indexPage.getParentFile().getParent());
    	WebArchive war =  ShrinkWrap
                .create(WebArchive.class, "inject.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(
                        new StringAsset(createWebXml()),
                        "web.xml")
                 // our test classes (src/test) = the webapp
                .addPackages(true, rootPackage)
                // tapestry dependencies, for real project put it in a helper
                // class: new TapestryArchive(name)...
                .addAsLibraries(JarLocation.jarLocation(HttpClient.class))
                .addAsLibraries(JarLocation.jarLocation(Lexer.class))
                .addAsLibraries(JarLocation.jarLocation(StringEncoder.class))
                .addAsLibraries(JarLocation.jarLocation(IOCConstants.class))
                .addAsLibraries(JarLocation.jarLocation(PlasticClass.class))
                .addAsLibraries(JarLocation.jarLocation(JSONArray.class))
                .addAsLibraries(JarLocation.jarLocation(InjectService.class))
                .addAsLibraries(JarLocation.jarLocation(Mapper.class))
                .addAsLibraries(JarLocation.jarLocation(TapestryModule.class))
                // for jbossAS7 server
                .addAsLibraries(JarLocation.jarLocation(org.jboss.shrinkwrap.api.asset.Asset.class))
                // for Glassfish container
                .addAsLibraries(JarLocation.jarLocation(org.slf4j.Logger.class));
    			
    			// our test resources (src/test) = the webapp
    			// add template resources from package "pages"
    			Package pagePackage = toPackage(indexPage.getParent());
    	    	File pageDirectory = 
		    			new File(TEST_RESOURCES_ROOT_PATH + toPath(pagePackage.getName()));
		    	for (String template : pageDirectory.list()) {
		    		war.addAsResource(pagePackage, template);
				}

    	    	// add template resources from package "components"
    	    	Package componentPackage = DumbComponent.class.getPackage();    	    	
    	    	File componentDirectory = 
		    			new File(TEST_RESOURCES_ROOT_PATH + toPath(componentPackage.getName()));
		    	for (String template : componentDirectory.list()) {
		    		war.addAsResource(componentPackage, template);
				}
		    	
		    	// add tapestry-cdi module to the archive
		    	war.addAsLibraries(createJarArchive("tapestry-cdi.jar"));
		    	war.addAsWebInfResource(new File(TEST_RESOURCES_ROOT_PATH + "log4j.xml"));
    	return war;
    }
    
    @Test
    @InSequence(0)
    public void checkApplicationScope() throws IOException {
    	
    	//get the index page (that increments an applicationScope counter)
    	String output = getResponse(indexUrl);
    	
    	//check that the counter has been incremented
        assertTrue("Injection of Application Scope Bean failed in page Index", output.contains("Counter : 1"));

        //change the page
        output = getResponse(new URL(indexUrl.toString() + "/"+  SomePage.class.getSimpleName()));
        assertNotNull(output);

        //get the index page (that increments an applicationScope counter)
        output = getResponse(indexUrl);

        //check that the counter has been incremented based on previous value (has not been re-initialized)
        assertTrue("Injection of Application Scope Bean failed in page Index", output.contains("Counter : 2"));
    }

    @Test
    @InSequence(1)
    public void checkSessionScope() throws IOException {
    	
    	HttpClient client  = new HttpClient();
    	
    	String output = getResponse(new URL(indexUrl.toString() + "/"+  SessionScopePage.class.getSimpleName()), client );
        assertTrue("Injection of SessionScope pojo failed in page Index 1", output.contains("session:true"));
        
    	output = getResponse(indexUrl, client);
        assertTrue("Injection of SessionScope pojo failed in page Index 2", output.contains("session:true"));

        output = getResponse(new URL(indexUrl.toString() + "/"+  InvalidateSessionPage.class.getSimpleName()), client);

        assertNotNull(output);

        output = getResponse(indexUrl, client);
        assertTrue("Injection of SessionScope pojo failed in page Index 3", output.contains("session:false"));
    }

    @Test
    @InSequence(2)
    public void checkInjectionsPojoFromOutput() throws IOException {

        String output = getResponse(indexUrl);
        
        assertTrue("Injection of Pojo failed in page index",
                output.contains("injected pojo"));
        assertTrue("Injection of NamedPojo failed in page index",
                output.contains("injected named pojo"));
        assertTrue("Injection of Pojo failed in component DumbComponent",
                output.contains("I named pojo into component"));
        assertTrue("Injection of NamedPojo failed in component DumbComponent",
                output.contains("I pojo into component"));

    }

    @Test
    @InSequence(3)
    public void checkInjectionTapestryServices() throws IOException {
        String output = getResponse(indexUrl);
        assertTrue(
                "Injection of Tapestry Service Messages by CDI annotation failed in page Index",
                output.contains("message_cdi"));
        assertTrue(
                "Injection of Tapestry Service Messages by Tapestry annotation failed in page Index",
                output.contains("message_tapestry"));

    }

    @Test
    @InSequence(4)
    public void checkInjectionSessionBeans() throws IOException {

        String output = getResponse(indexUrl);
        assertTrue("Injection of Stateless Session Bean failed in page Index", output.contains("Hello Stateless EJB"));

        HttpClient client = new HttpClient();
        output = getResponse(new URL(indexUrl.toString() + "/"+  StatefulPage.class.getSimpleName()), client);
        assertTrue("Injection of Stateful Session Bean failed in page MyStateful\n" + output, output.contains("011stateful"));

        output = getResponse(new URL(indexUrl.toString() + "/"+  StatefulPage.class.getSimpleName()), client);
        assertTrue("Injection of Stateful Session Bean failed in page MyStateful\n" + output, output.contains("122stateful"));

    }


    @Test
    @InSequence(5)
    public void checkInjectionRequestScope() throws IOException {
    	HttpClient client = new HttpClient();
    	
    	String output = getResponse(indexUrl, client);
        assertTrue("Injection of RequestScope pojo failed in page Index", output.contains("request:true"));

        output = getResponse(new URL(indexUrl.toString() + "/"+  RequestScopePage.class.getSimpleName()), client);
        assertTrue("Injection of RequestScope pojo failed in page Index", output.contains("request:false"));

    }


   /**
     * Todo - Add tests for session state. How  notify cdi about changes in session state objects ?
     *
     */

    @Test
    @InSequence(6)
    public void checkQualifierBasic() throws IOException {

        String output = getResponse(new URL(indexUrl.toString() + "/"+  DessertPage.class.getSimpleName()));
        assertTrue("Injection of pojo with qualifier failed in page Dessert", output.contains("dessert1:true"));
        assertTrue("Injection of pojo with qualifier failed in page Dessert", output.contains("dessert2:true"));
        assertTrue("Injection of pojo with qualifier and produces method failed in page Dessert", output.contains("dessert3:true"));
        assertTrue("Injection of pojo with qualifier and produces method + @new failed in page Dessert", output.contains("dessert4:true"));

        /**
         Todo - Add support to @Inject method | uncomment the line below to test it
         */
        //assertTrue("Injection of pojo with qualifier and inject method in page Dessert",output.contains("dessert5:true"));


    }

    @Test
    @InSequence(7)
    public void checkConversationScope() throws IOException {

        String output = getResponse(new URL(indexUrl.toString() + "/"+  VegetablePage.class.getSimpleName()));
        /**
         Todo - Create a test with drone to play with the conversation scope
         */

    }

    @Test
    @InSequence(8)
    public void checkEventBasic() throws IOException {
        /**
         Todo - find a usecase... issues while fire event in page/ cannot observes in page either
         */
        
    }

    @Test
    @InSequence(9)
    public void checkBindingType() throws IOException {
        /**
         Todo - Use Produces method with parameter to present a great use case
         */


    }

    @Test
    @InSequence(10)
    public void checkWebService() throws IOException {
    	 String output = getResponse(new URL(indexUrl.toString() + "/"+ WSPage.class.getSimpleName()));
    	 assertNotNull(output);
    	 assertTrue("Injection of webservice failed in page WSPage", output.contains("Hello John"));
    }
    
    @Test
    @InSequence(11)
    public void checkStereotype() throws IOException {
    	
    	HttpClient client  = new HttpClient();
    	
    	//Check if injection of specific stereotyped bean is ok 
    	   
    	String output = getResponse(new URL(indexUrl.toString() + "/"+ StereotypePage.class.getSimpleName()), client);
    	assertNotNull(output);
    	assertTrue("Injection of stereotyped bean failed in page StereotypePage", output.contains("Stereotype bean:true"));
    	assertTrue("Stereotype Bean not SessionScoped as expected in page StereotypePage", output.contains("Same instance:true"));
    	
    	//Check if the bean is really SessionScoped as its Stereotype says

    	output = getResponse(indexUrl, client);
    	assertTrue("Stereotype Bean not SessionScoped as expected in page StereotypePage", output.contains("stereotype:true"));
    	output = getResponse(new URL(indexUrl.toString() + "/"+  InvalidateSessionPage.class.getSimpleName()), client);
    	assertNotNull(output);
    	output = getResponse(indexUrl, client);
    	assertTrue("Stereotype Bean not SessionScoped as expected in page StereotypePage", output.contains("stereotype:false"));
    }
    
    /**
     * Create a jar archive for tapestry-cdi
     * @param archiveName the archive name
     * @return a JarArchive object
     */
     private static JavaArchive createJarArchive(String archiveName){
    	JavaArchive jar =  ShrinkWrap
    			// our module (src/main), as we are in the same project building
                // the jar on the fly
                .create(JavaArchive.class,
                		archiveName)
                .addPackages(true,
                        CDIInjectModule.class.getPackage()
                                .getName())
                // do not include test package
                .deletePackages(true,
                        InjectTest.class.getPackage()
                                .getName())
                .addAsManifestResource(
                        new StringAsset(BeanManagerHolder.class
                                .getName()),
                        "services/" + Extension.class.getName());

    	jar.addAsManifestResource(
                    new StringAsset(TapestryExtension.class.getName()),
                    "services/" + Extension.class.getName());
    	jar.addAsManifestResource(
    			new File(METAINF_PATH + "services/" + Extension.class.getName()),
                "services/" + Extension.class.getName());
    	jar.addAsManifestResource(
    			new File(METAINF_PATH + "beans.xml"),
                "beans.xml");
    	jar.addAsManifestResource(
                new StringAsset("Manifest-Version: 1.0\n" + "Tapestry-Module-Classes: org.apache.tapestry5.cdi.CDIInjectModule"),
    			"MANIFEST.MF");
    	return jar;
    }
    
    
    /**
     * Create a web.xml file and return its content as a String
     * @return a String
     */
    private static String createWebXml(){
    	return Descriptors
                .create(WebAppDescriptor.class).version("3.0")
                .createContextParam()
                .paramName("tapestry.app-package")
                .paramValue(InjectTest.class.getPackage().getName())
                .up().createContextParam()
                .paramName("tapestry.production-mode")
                .paramValue("false").up().createFilter()
                .filterName("pojo")
                .filterClass(TapestryFilter.class.getName())
                .up().createFilterMapping().filterName("pojo")
                .urlPattern("/*").up()
                .exportAsString();
    }
    
    /**
     * Convert a package name to a path
     * @param packageName the package name
     * @return a String
     */
    private static String toPath(String packageName) {
		return packageName.replace(".", File.separator);
	}

    /**
     * Convert a file path to a Package
     * @param path the file path
     * @return a Package
     */
    private static Package toPackage(String path) {
		return Package.getPackage(path.replace(File.separator, "."));
	}

    /**
     * Connect to an url and return the response content as a String 
     * @param url an url to connect to
     * @return the response as a String
     */
    private String getResponse(URL url) {
    	return getResponse(url, null);
    }
    
    /**
     * Connect to an url thanks to an HttpClient if provided and return the response content as a String 
     * Use same HttpClient to keep same HttpSession through multiple getResponse method calls  
     * @param url an url to connect to
     * @param client an HTTPClient to use to serve the url
     * @return the response as a String
     */
    private String getResponse(URL url, HttpClient client) {
    	HttpClient newClient = client==null ? new HttpClient() : client;
        HttpMethod get = new GetMethod(url.toString());
        String output = null;
        int out = 200;
    	 try {
             out = newClient.executeMethod(get);
             if (out != 200) {
                 throw new RuntimeException("get " + get.getURI() + " returned " + out);
             }
             output = get.getResponseBodyAsString();
             
         } catch (HttpException e) {
        	 e.printStackTrace();
        	 throw new RuntimeException("get " + url + " returned " + out);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("get " + url + " returned " + out);
		} finally {
             get.releaseConnection();
         }
         return output;
    }

}

