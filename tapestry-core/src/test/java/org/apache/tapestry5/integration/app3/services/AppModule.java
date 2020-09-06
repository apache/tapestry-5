// Copyright 2008, 2009, 2013 The Apache Software Foundation
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

package org.apache.tapestry5.integration.app3.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.Configuration;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.integration.app3.components.OverrideComponent;
import org.apache.tapestry5.integration.app3.components.OverridenComponent;
import org.apache.tapestry5.integration.app3.mixins.OverrideMixin;
import org.apache.tapestry5.integration.app3.mixins.OverridenMixin;
import org.apache.tapestry5.integration.app3.pages.OverridePage;
import org.apache.tapestry5.integration.app3.pages.OverridenPage;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.services.ComponentOverride;
import org.apache.tapestry5.services.DisplayBlockContribution;
import org.apache.tapestry5.services.compatibility.Compatibility;
import org.apache.tapestry5.services.compatibility.Trait;
import org.apache.tapestry5.services.security.ClientWhitelist;
import org.apache.tapestry5.services.security.WhitelistAnalyzer;

public class AppModule
{
    public static final String FORM_GROUP_LABEL_CSS_CLASS_VALUE = "control-label col-sm-2";
    
    public static final String FORM_GROUP_WRAPPER_CSS_CLASS_VALUE = "something form-group";
    
    public static final String FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_NAME_VALUE = "div";
    
    public static final String FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_CSS_CLASS_VALUE = "col-sm-10";
    
    public static final String FORM_FIELD_CSS_CLASS_VALUE = "form-control control-form";

    public static void contributeBeanBlockOverrideSource(Configuration<Object> configuration)
    {
        configuration.add(new DisplayBlockContribution("boolean", "PropertyDisplayBlockOverrides", "boolean"));
    }

    public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(TapestryHttpSymbolConstants.GZIP_COMPRESSION_ENABLED, "false");

        configuration.add(TapestryHttpSymbolConstants.PRODUCTION_MODE, "false");

        configuration.add(SymbolConstants.JAVASCRIPT_INFRASTRUCTURE_PROVIDER, "jquery");
        
        configuration.add(SymbolConstants.FORM_GROUP_LABEL_CSS_CLASS, FORM_GROUP_LABEL_CSS_CLASS_VALUE);
        configuration.add(SymbolConstants.FORM_GROUP_WRAPPER_CSS_CLASS, FORM_GROUP_WRAPPER_CSS_CLASS_VALUE);
        configuration.add(SymbolConstants.FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_NAME, FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_NAME_VALUE);
        configuration.add(SymbolConstants.FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_CSS_CLASS, FORM_GROUP_FORM_FIELD_WRAPPER_ELEMENT_CSS_CLASS_VALUE);
        configuration.add(SymbolConstants.FORM_FIELD_CSS_CLASS, FORM_FIELD_CSS_CLASS_VALUE);
        
        configuration.add(SymbolConstants.ENABLE_HTML5_SUPPORT, "true");
        
    }
    
    @Contribute(Compatibility.class)
    public static void disableBackwardsCompatibleFeatures(MappedConfiguration<Trait, Boolean> configuration)
    {
        configuration.add(Trait.INITIALIZERS, false);
        configuration.add(Trait.SCRIPTACULOUS, false);
    }

    @Contribute(ClientWhitelist.class)
    public static void provideWhitelistAnalyzer(OrderedConfiguration<WhitelistAnalyzer> configuration)
    {
        configuration.add("TestAnalyzer", new WhitelistAnalyzer()
        {

            public boolean isRequestOnWhitelist(Request request)
            {
                return true;
            }
        }, "before:*");
    }
    
    @Contribute(ComponentOverride.class)
    public static void overridePageAndComponentAndMixin(MappedConfiguration<Class, Class> configuration) {
        configuration.add(OverridenPage.class, OverridePage.class);
        configuration.add(OverridenComponent.class, OverrideComponent.class);
        configuration.add(OverridenMixin.class, OverrideMixin.class);
    }

}
