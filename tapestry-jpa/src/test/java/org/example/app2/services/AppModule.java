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

package org.example.app2.services;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.jpa.JpaSymbols;
import org.apache.tapestry5.jpa.modules.JpaModule;
import org.example.app2.services.impl.UserDAOImpl;

@ImportModule(JpaModule.class)
public class AppModule
{

    public static void bind(final ServiceBinder binder)
    {
        binder.bind(UserDAO.class, UserDAOImpl.class);
    }

    @Contribute(SymbolProvider.class)
    @ApplicationDefaults
    public static void provideFactoryDefaults(
            final MappedConfiguration<String, String> configuration)
    {
        configuration.add(JpaSymbols.PERSISTENCE_DESCRIPTOR, "/single-persistence-unit.xml");
    }
}
