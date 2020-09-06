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

package org.example.testapp.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.beanvalidator.BeanValidatorConfigurer;
import org.apache.tapestry5.beanvalidator.modules.BeanValidatorModule;
import org.apache.tapestry5.commons.Configuration;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.ioc.annotations.ImportModule;

@ImportModule(BeanValidatorModule.class)
public class AppModule
{

    public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(TapestryHttpSymbolConstants.PRODUCTION_MODE, "false");
        configuration.add(SymbolConstants.HMAC_PASSPHRASE, "u93490jhsprf2904rh29-3uj");
    }

    public static void contributeBeanValidatorSource(
            final OrderedConfiguration<BeanValidatorConfigurer> configuration)
    {
        configuration.add("Test", new BeanValidatorConfigurer()
        {

            @Override
            public void configure(javax.validation.Configuration<?> configuration)
            {
                configuration.ignoreXmlConfiguration();
            }
        });
    }


    public static void contributeBeanValidatorGroupSource(
            final Configuration<Class> configuration)
    {
        configuration.add(Foo.class);
    }

}
