// Copyright 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.internal.TapestryHttpInternalConstants;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;

public class ForceDevelopmentModeModule
{
    @Contribute(SymbolProvider.class)
    @ApplicationDefaults
    public static void enableDevelopmentMode(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(TapestryHttpSymbolConstants.PRODUCTION_MODE, false);
        configuration.add(TapestryHttpInternalConstants.TAPESTRY_APP_PACKAGE_PARAM, "app.root.package");
        configuration.add(SymbolConstants.HMAC_PASSPHRASE, "hmac passphrase for testing");
    }
}
