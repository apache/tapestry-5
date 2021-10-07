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
package org.apache.tapestry5.openapiviewer.test.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.Configuration;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.services.LibraryMapping;
import org.apache.tapestry5.services.rest.MappedEntityManager;

public class AppModule
{
    public static void contributeComponentClassResolver(Configuration<LibraryMapping> configuration)
    {
        configuration.add(new LibraryMapping("app1", "org.apache.tapestry5.integration.app1"));
    }
    
    public static void contributeApplicationDefaults(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(SymbolConstants.PUBLISH_OPENAPI_DEFINITON, true);
        configuration.add(SymbolConstants.PRODUCTION_MODE, false);
    }
    
    @Contribute(MappedEntityManager.class)
    public static void provideMappedEntities(Configuration<String> configuration)
    {
//        org.apache.tapestry5.integration.app1.services.AppModule.provideMappedEntities(configuration);
        configuration.add("org.apache.tapestry5.integration.app1.data.rest.entities");
    }
    
}
