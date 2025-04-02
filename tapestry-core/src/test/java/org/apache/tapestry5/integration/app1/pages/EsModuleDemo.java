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

package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.integration.app1.EsModuleTests;
import org.apache.tapestry5.integration.app1.services.AppModule;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.EsModuleConfigurationCallback;
import org.apache.tapestry5.services.javascript.ImportPlacement;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

@Import(esModule = {"root-folder"})
public class EsModuleDemo
{
    public static final String DEFAULT_EXPORT_MESSAGE = "default-export-message";

    public static final String DEFAULT_EXPORT_PARAMETER = "Importing module exporting single function!";

    public static final String REQUEST_OVERRIDEN_MODULE_URL = "/overridenAgainURL";

    @Inject
    private JavaScriptSupport javaScriptSupport;
    
    @Property
    Boolean overrideEsModuleImportAgain;

    @SetupRender
    void importEsModule()
    {
        // Checking each module is only imported once.
        javaScriptSupport.importEsModule("foo/bar")
            .withDefer()
            .withAsync()
            .withAttribute("foo")
            .withAttribute("something", "else");
        javaScriptSupport.importEsModule("foo/bar");

        javaScriptSupport.importEsModule("placement/body-bottom")
            .placement(ImportPlacement.BODY_BOTTOM);
        javaScriptSupport.importEsModule("placement/body-top")
            .placement(ImportPlacement.BODY_TOP);
        javaScriptSupport.importEsModule("placement/head")
            .placement(ImportPlacement.HEAD);
        javaScriptSupport.importEsModule("outside-metainf");        
        javaScriptSupport.importEsModule("show-import-map");
        
        javaScriptSupport.importEsModule("default-export")
            .with(EsModuleDemo.DEFAULT_EXPORT_MESSAGE, EsModuleDemo.DEFAULT_EXPORT_PARAMETER);
        
        javaScriptSupport.importEsModule("non-default-export")
            .invoke("setMessage");
        
        // Both .with() and .invoke() cause the function to be invoked
        javaScriptSupport.importEsModule("parameterless-default-export")
            .with();

        if (overrideEsModuleImportAgain != null && overrideEsModuleImportAgain)
        {
            javaScriptSupport.addEsModuleConfigurationCallback(
                    o -> EsModuleConfigurationCallback.setImport(o, 
                            AppModule.OVERRIDDEN_ES_MODULE_ID, REQUEST_OVERRIDEN_MODULE_URL));
        }
        
    }

    void onActivate(boolean overrideEsModuleImportAgain)
    {
        this.overrideEsModuleImportAgain = overrideEsModuleImportAgain;
    }
    
    Object onEnableOverride()
    {
        overrideEsModuleImportAgain = true;
        return this;
    }
    
    void onDisableOverride()
    {
        overrideEsModuleImportAgain = false;
    }
    
    Object[] onPassivate()
    {
        return overrideEsModuleImportAgain != null && overrideEsModuleImportAgain
                ? new Object[] { overrideEsModuleImportAgain } 
                : null;
    }
    
}
