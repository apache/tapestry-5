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

package org.apache.tapestry5.modules;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.services.compatibility.Compatibility;
import org.apache.tapestry5.services.compatibility.Trait;

/**
 * Module which defines the options needed to run Tapestry using Bootstrap 4 instead of the default
 * Bootstrap 3.
 *
 * @since 5.5
 */
public class Bootstrap4Module
{
    public static void contributeApplicationDefaults(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(SymbolConstants.BOOTSTRAP_ROOT, "${tapestry.asset.root}/bootstrap4");
//        configuration.add(SymbolConstants.BEAN_DISPLAY_CSS_CLASS, "card");
    }
    
    @Contribute(Compatibility.class)
    public static void setupCompatibilityDefaults(MappedConfiguration<Trait, Boolean> configuration)
    {
        configuration.override(Trait.BOOTSTRAP_4, true);
        configuration.add(Trait.BOOTSTRAP_3, false);
    }
    
}
