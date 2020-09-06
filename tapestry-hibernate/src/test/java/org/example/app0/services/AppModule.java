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

package org.example.app0.services;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.OrderedConfiguration;
import org.apache.tapestry5.hibernate.HibernateSymbols;
import org.apache.tapestry5.hibernate.HibernateTransactionDecorator;
import org.apache.tapestry5.hibernate.modules.HibernateCoreModule;
import org.apache.tapestry5.hibernate.web.modules.HibernateModule;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.http.services.Request;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.ServiceResources;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.annotations.Match;
import org.apache.tapestry5.services.security.ClientWhitelist;
import org.apache.tapestry5.services.security.WhitelistAnalyzer;

// @ImportModule just needed for developers running these tests within the IDE
@ImportModule({HibernateModule.class, HibernateCoreModule.class})
public class AppModule
{
    public static void bind(ServiceBinder binder)
    {
        binder.bind(UserDAO.class, UserDAOImpl.class);
    }

    public static void contributeApplicationDefaults(MappedConfiguration<String, String> configuration)
    {
        configuration.add(TapestryHttpSymbolConstants.PRODUCTION_MODE, "false");
        configuration.add(HibernateSymbols.ENTITY_SESSION_STATE_PERSISTENCE_STRATEGY_ENABLED, "true");
    }

    @Match("*DAO")
    public static <T> T decorateTransactionally(HibernateTransactionDecorator decorator, Class<T> serviceInterface,
                                                T delegate, ServiceResources resources)
    {
        return decorator.build(serviceInterface, delegate, resources.getServiceId());
    }


    @Contribute(ClientWhitelist.class)
    public static void provideWhitelistAnalyzer(OrderedConfiguration<WhitelistAnalyzer> configuration)
    {
        configuration.add("TestAnalyzer", new WhitelistAnalyzer()
        {

            @Override
            public boolean isRequestOnWhitelist(Request request)
            {
                return true;
            }
        }, "before:*");
    }
}
