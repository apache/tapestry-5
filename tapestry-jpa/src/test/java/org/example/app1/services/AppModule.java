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

package org.example.app1.services;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.http.TapestryHttpSymbolConstants;
import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.annotations.Match;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.jpa.EntityManagerSource;
import org.apache.tapestry5.jpa.JpaTransactionAdvisor;
import org.apache.tapestry5.jpa.PersistenceUnitConfigurer;
import org.apache.tapestry5.jpa.TapestryPersistenceUnitInfo;
import org.apache.tapestry5.jpa.modules.JpaModule;
import org.example.app1.AppConstants;
import org.example.app1.entities.Thang;
import org.example.app1.entities.User;
import org.example.app1.services.impl.UserDAOImpl;

@ImportModule(JpaModule.class)
public class AppModule
{

    public static void bind(final ServiceBinder binder)
    {
        binder.bind(UserDAO.class, UserDAOImpl.class);
    }

    @Contribute(SymbolProvider.class)
    @ApplicationDefaults
    public static void provideApplicationDefaults(
            final MappedConfiguration<String, String> configuration)
    {
        configuration.add(TapestryHttpSymbolConstants.PRODUCTION_MODE, "false");
    }

    @Contribute(EntityManagerSource.class)
    public static void configurePersistenceUnitInfos(
            final MappedConfiguration<String, PersistenceUnitConfigurer> configuration)
    {
        final PersistenceUnitConfigurer configurer = new PersistenceUnitConfigurer()
        {
            @Override
            public void configure(final TapestryPersistenceUnitInfo unitInfo)
            {
                unitInfo.addManagedClass(User.class);
            }
        };
        configuration.add(AppConstants.TEST_PERSISTENCE_UNIT, configurer);

        final PersistenceUnitConfigurer configurer2 = new PersistenceUnitConfigurer()
        {
            @Override
            public void configure(final TapestryPersistenceUnitInfo unitInfo)
            {
                unitInfo.addProperty("javax.persistence.jdbc.driver", "org.h2.Driver")
                        .addProperty("javax.persistence.jdbc.url", "jdbc:h2:mem:test")
                        .addProperty("eclipselink.ddl-generation", "create-tables")
                        .addProperty("eclipselink.logging.level", "fine")
                        .addManagedClass(Thang.class);
            }
        };

        configuration.add(AppConstants.TEST_PERSISTENCE_UNIT_2, configurer2);

    }

    @Match("*DAO")
    public static void adviseTransactionally(final JpaTransactionAdvisor advisor,
                                             final MethodAdviceReceiver receiver)
    {
        advisor.addTransactionCommitAdvice(receiver);
    }
}
