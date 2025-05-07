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

package org.apache.tapestry5.integration.app1;


import static org.apache.tapestry5.integration.app1.services.AppModule.NON_OVERRIDDEN_ES_MODULE_ID;
import static org.apache.tapestry5.integration.app1.services.AppModule.NON_OVERRIDDEN_ES_MODULE_URL;
import static org.apache.tapestry5.integration.app1.services.AppModule.OVERRIDDEN_ES_MODULE_ID;
import static org.apache.tapestry5.integration.app1.services.AppModule.OVERRIDDEN_ES_MODULE_NEW_URL;
import static org.apache.tapestry5.integration.app1.services.AppModule.OVERRIDDEN_GLOBALLY_ES_MODULE_ID;
import static org.apache.tapestry5.integration.app1.services.AppModule.OVERRIDDEN_GLOBALLY_ES_MODULE_NEW_URL;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.integration.app1.pages.EsModuleDemo;
import org.apache.tapestry5.internal.transform.ImportWorker;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.services.javascript.EsModuleConfigurationCallback;
import org.apache.tapestry5.services.javascript.EsModuleInitialization;
import org.apache.tapestry5.services.javascript.EsModuleManager;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.testng.annotations.Test;

/**
 * ES module tests.
 */
public class EsModuleTests extends App1TestCase
{
    private static final String PAGE_NAME = "ES Module Demo";
    
    private static final String REQUEST_CALLBACK_SWITCHER = "css=.switch";
    
    @Inject
    private AssetSource assetSource;

    /**
     * Tests whether ES modules placed in /META-INF/es-modules are automatically
     * added to import maps.
     */
    @Test
    public void automatic_modules()
    {
        openLinks(PAGE_NAME);
        JSONObject importMap = getImportMap();
        assertModuleUrlSuffix("foo/bar", "/es-modules/foo/bar.js", importMap);
        assertModuleUrlSuffix("root-folder", "/es-modules/root-folder.js", importMap);
        assertModuleUrlSuffix("suffix", "/es-modules/suffix.mjs", importMap);
    }
    
    /**
     * Tests whether ES modules added or overriden through global callbacks
     * (i.e. ones contributed to {@link EsModuleManager} configuration)
     * are being actually included in the generated import map.
     */
    @Test
    public void modules_added_by_global_callbacks()
    {
        openLinks(PAGE_NAME);
        JSONObject importMap = getImportMap();
        assertModulesDefinedByGlobalCallbacks(importMap);
    }
    
    /**
     * Tests whether ES modules added or overriden through request callbacks
     * (i.e. ones added through {@link JavaScriptSupport#addEsModuleConfigurationCallback(EsModuleConfigurationCallback)})
     * are being actually included in the generated import map.
     * @throws InterruptedException 
     */
    @Test
    public void modules_added_by_request_callbacks()
    {
        openLinks(PAGE_NAME);
        
        // With import map changed by request callbacks.
        clickAndWait(REQUEST_CALLBACK_SWITCHER);
        JSONObject importMap = getImportMap();
        assertModuleUrl(NON_OVERRIDDEN_ES_MODULE_ID, NON_OVERRIDDEN_ES_MODULE_URL, importMap);
        assertModuleUrl(OVERRIDDEN_ES_MODULE_ID, EsModuleDemo.REQUEST_OVERRIDEN_MODULE_URL, importMap);

        // Module first defined through callback added to JavaScriptSupport,
        // then overriden by a global per-request callback.
        assertModuleUrl(OVERRIDDEN_GLOBALLY_ES_MODULE_ID, OVERRIDDEN_GLOBALLY_ES_MODULE_NEW_URL, importMap);

        
        // Now without import map changed by request callbacks, so we can test
        // the global import map wasn't affected.
        clickAndWait(REQUEST_CALLBACK_SWITCHER);
        importMap = getImportMap();
        assertModulesDefinedByGlobalCallbacks(importMap);
        
    }
    
    /**
     * Tests {@link JavaScriptSupport#importEsModule(String)}.
     */
    @Test
    public void javascript_support_importEsModule() throws InterruptedException
    {

        openLinks(PAGE_NAME);
        
        // Module imported with specified attributes.
        assertTrue(isElementPresent("//script[@type='module'][contains(@src, 'foo/bar.js')][@defer='defer'][@async='async'][@something='else'][@foo='foo']"));
        
        // Module imported with no placement (default body bottom) or
        // BODY_BOTTOM should be after the last <div> in this webapp's template
        assertTrue(isElementPresent("//body/div[last()][following-sibling::script[@type='module'][contains(@src, '/placement/body-bottom.js')]]"));
        
        // Module imported with placement BODY_TOP should be before 
        // the last <div> in this webapp's template (the first one comes from JS).
        assertTrue(isElementPresent("//body/div[@role='navigation'][preceding-sibling::script[@type='module'][contains(@src, '/placement/body-top.js')]]"));   

        // Module imported with placement HEAD
        assertTrue(isElementPresent("//head/script[@type='module'][contains(@src, '/placement/head.js')]"));
        
        // Checking results of running the modules, not just their inclusion in HTML
        assertEquals(getText("message"), "ES module foo/bar imported correctly!");
        assertEquals(getText("head-message"), "ES module imported correctly (<head>)!");
        assertEquals(getText("body-top-message"), "ES module imported correctly (<body> top)!");
        assertEquals(getText("body-bottom-message"), "ES module imported correctly (<body> bottom)!");
        assertEquals(getText("outside-metainf-message"), "ES module correctly imported from outside /META-INF/assets/es-modules!");

    }
    
    /**
     * Tests importing ES modules through <code>@Import(esModule = ...)</code>.
     * @see ImportWorker
     * @see Import#esModule()
     */
    @Test
    public void at_import_esModule() throws InterruptedException
    {
        openLinks(PAGE_NAME);
        assertScriptElement("root-folder");
        assertScriptElement("suffix");
        assertEquals(getText("root-folder-message"), "ES module imported correctly from the root folder!");
        assertEquals(getText("suffix-message"), "ES module imported correctly from .mjs file!");
    }
    
    /**
     * Tests using {@link EsModuleInitialization#with(Object...)} without using
     * {@link EsModuleInitialization#invoke(String)} (i.e. invoking the default
     * exported function with at least one parameter).
     */
    @Test
    public void invoking_default_exported_function() throws InterruptedException
    {
        openLinks(PAGE_NAME);
        assertEquals(
                getText(EsModuleDemo.DEFAULT_EXPORT_MESSAGE), 
                EsModuleDemo.DEFAULT_EXPORT_PARAMETER);
    }
    
    /**
     * Tests using {@link EsModuleInitialization#with(Object...)} without using
     * {@link EsModuleInitialization#invoke(String)} (i.e. invoking the default
     * exported function). In order words,
     * {@code javaScriptSupport.importEsModule("foo").with(...)}
     */
    @Test
    public void invoking_non_default_exported_function() throws InterruptedException
    {
        openLinks(PAGE_NAME);
        assertEquals(
                getText(EsModuleDemo.DEFAULT_EXPORT_MESSAGE), 
                EsModuleDemo.DEFAULT_EXPORT_PARAMETER);
    }
    
    /**
     * Tests using {@code javaScriptSupport.importEsModule("foo").with()}
     * (i.e. invoking the default withot parameters)
     */
    @Test
    public void invoking_non_default_exported_function_without_parameters() throws InterruptedException
    {
        openLinks(PAGE_NAME);
        assertEquals(
                getText("parameterless-default-export-message"), 
                "Parameterless default export!");
    }
    
    /**
     * Tests using whether parameter types are correctly passed to JS.
     */
    @Test
    public void parameter_types() throws InterruptedException
    {
        openLinks(PAGE_NAME);
        assertEquals(
                getText("parameter-type-default-export-message"), 
                "Parameter types passed correctly!");
    }
    
    private void assertModulesDefinedByGlobalCallbacks(JSONObject importMap) {
        assertModuleUrl(NON_OVERRIDDEN_ES_MODULE_ID, NON_OVERRIDDEN_ES_MODULE_URL, importMap);
        assertModuleUrl(OVERRIDDEN_ES_MODULE_ID, OVERRIDDEN_ES_MODULE_NEW_URL, importMap);
    }
    
    private void assertModuleUrlSuffix(String id, String urlSuffix, JSONObject importMap)
    {
        final JSONObject imports = (JSONObject) importMap.get(EsModuleConfigurationCallback.IMPORTS_ATTRIBUTE);
        final String url = imports.getString(id);
        
        assertNotNull(url, String.format("Module %s not found in import map\n%s", id, importMap.toString(false)));
        assertTrue(url.endsWith(urlSuffix), String.format("Unexpected URL %s for module %s (expected %s suffix)", url, id, urlSuffix));
    }
    
    private void assertModuleUrl(String id, String urlSuffix, JSONObject importMap)
    {
        final JSONObject imports = (JSONObject) importMap.get(EsModuleConfigurationCallback.IMPORTS_ATTRIBUTE);
        final String url = imports.getString(id);
        
        assertNotNull(url, String.format("Module %s not found in import map\n%s", id, importMap.toString(false)));
        assertEquals(url, urlSuffix, String.format("Unexpected URL %s for module %s (expected %s suffix)", url, id, urlSuffix));
    }
    
    private void assertScriptElement(String moduleId)
    {
        assertTrue(
                isElementPresent(String.format("//script[@data-module-id='%s']", moduleId)),
                "<script> element for ES module found: " + moduleId);
    }
    
    private JSONObject getImportMap()
    {
        return new JSONObject(getText("import-map-listing"));
    }

}
