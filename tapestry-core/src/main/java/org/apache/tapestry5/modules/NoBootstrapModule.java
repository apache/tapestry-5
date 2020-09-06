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
 * Module which defines the options needed to run Tapestry without Bootstrap. You'll need
 * to define the CSS file to be included in all pages by setting the 
 * <code>tapestry.default-stylesheet</code> configuration symbol 
 * ({@link SymbolConstants#DEFAULT_STYLESHEET}). Notice Tapestry will not provide any 
 * CSS, so you're on your own regarding stylesheets.
 * 
 * @since 5.5
 */
public class NoBootstrapModule
{
    @Contribute(Compatibility.class)
    public static void setupCompatibilityDefaults(MappedConfiguration<Trait, Boolean> configuration)
    {
        configuration.add(Trait.BOOTSTRAP_3, false);
    }
}
