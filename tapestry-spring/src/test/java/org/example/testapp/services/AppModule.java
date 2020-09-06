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

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.spring.ApplicationContextCustomizer;
import org.apache.tapestry5.spring.modules.SpringModule;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import javax.servlet.ServletContext;

@ImportModule(SpringModule.class)
public class AppModule
{
    public static final void bind(ServiceBinder binder)
    {
        binder.bind(StringTransformer.class, UpcaseStringTransformerImpl.class);
    }

    public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(TapestryHttpSymbolConstants.PRODUCTION_MODE, "false");
    }

    public static void contributeApplicationContextCustomizer(
            OrderedConfiguration<ApplicationContextCustomizer> configuration)
    {
        configuration.add("WasInvoked", new ApplicationContextCustomizer()
        {
            @Override
            public void customizeApplicationContext(ServletContext servletContext,
                                                    ConfigurableWebApplicationContext applicationContext)
            {
                servletContext.setAttribute("status-message", "Pipeline Was Invoked");
            }
        });
    }
}
