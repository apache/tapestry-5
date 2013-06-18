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
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.services.DisplayBlockContribution;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.compatibility.Compatibility;
import org.apache.tapestry5.services.compatibility.Trait;
import org.apache.tapestry5.services.security.ClientWhitelist;
import org.apache.tapestry5.services.security.WhitelistAnalyzer;

public class AppModule
{
    public static void contributeBeanBlockOverrideSource(Configuration<Object> configuration)
    {
        configuration.add(new DisplayBlockContribution("boolean", "PropertyDisplayBlockOverrides", "boolean"));
    }

    public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(SymbolConstants.GZIP_COMPRESSION_ENABLED, "false");

        configuration.add(SymbolConstants.PRODUCTION_MODE, "false");

        configuration.add(SymbolConstants.JAVASCRIPT_INFRASTRUCTURE_PROVIDER, "jquery");
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

}
