// Copyright 2013 The Apache Software Foundation
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

package org.apache.tapestry5.integration.activationctx2.services;

import org.apache.tapestry5.MetaDataConstants;
import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;

/**
 *
 */
public class AppModule
{
    public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en");
        configuration.add(TapestryHttpSymbolConstants.PRODUCTION_MODE, "false");
        configuration.add(SymbolConstants.COMPRESS_WHITESPACE, "false");

        // Enable by default the check on activation context parameters
        configuration.add(MetaDataConstants.UNKNOWN_ACTIVATION_CONTEXT_CHECK, "true");
    }
}
