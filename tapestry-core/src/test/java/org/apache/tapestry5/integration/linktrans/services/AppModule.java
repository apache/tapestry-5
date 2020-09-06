// Copyright 2010 The Apache Software Foundation
//
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

package org.apache.tapestry5.integration.linktrans.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.services.linktransform.ComponentEventLinkTransformer;
import org.apache.tapestry5.services.linktransform.PageRenderLinkTransformer;

public class AppModule
{
    public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en,fr,de");
        configuration.add(TapestryHttpSymbolConstants.PRODUCTION_MODE, "false");
        configuration.add(SymbolConstants.COMPRESS_WHITESPACE, "false");
        configuration.add(SymbolConstants.COMBINE_SCRIPTS, "true");
    }

    public static void contributePageRenderLinkTransformer(OrderedConfiguration<PageRenderLinkTransformer> configuration)
    {
        configuration.addInstance("App", AppPageRenderLinkTransformer.class);
    }

    public static void contributeComponentEventLinkTransformer(
            OrderedConfiguration<ComponentEventLinkTransformer> configuration)
    {
        configuration.addInstance("App", AppComponentEventLinkTransformer.class);
    }
}
