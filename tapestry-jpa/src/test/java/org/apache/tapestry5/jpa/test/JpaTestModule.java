/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tapestry5.jpa.test;

import javax.persistence.spi.PersistenceUnitTransactionType;

import org.apache.tapestry5.commons.Configuration;
import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.http.internal.TapestryHttpInternalConstants;
import org.apache.tapestry5.internal.jpa.TapestryCDIBeanManagerForJPAEntityListeners;
import org.apache.tapestry5.ioc.MethodAdviceReceiver;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Match;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.jpa.EntityManagerSource;
import org.apache.tapestry5.jpa.JpaEntityPackageManager;
import org.apache.tapestry5.jpa.JpaTransactionAdvisor;
import org.apache.tapestry5.jpa.PersistenceUnitConfigurer;
import org.apache.tapestry5.jpa.TapestryPersistenceUnitInfo;
import org.apache.tapestry5.jpa.test.entities.ThingOne;
import org.apache.tapestry5.jpa.test.entities.ThingTwo;
import org.apache.tapestry5.jpa.test.entities.VersionedThing;

public class JpaTestModule
{

    public static void bind(final ServiceBinder binder)
    {
        binder.bind(TopLevelService.class);
        binder.bind(NestedService.class);
    }

    @Contribute(SymbolProvider.class)
    @ApplicationDefaults
    public static void defaultsSymbols(MappedConfiguration<String, Object> configuration)
    {
        configuration.add(TapestryHttpInternalConstants.TAPESTRY_APP_PACKAGE_PARAM, JpaTestModule.class
                .getPackage().getName());
        // configuration.add(InternalSymbols.APP_PACKAGE_PATH, "org/tynamo/model/jpa");
    }

    @Contribute(JpaEntityPackageManager.class)
    public static void addPackages(Configuration<String> configuration)
    {
        configuration.add(JpaTestModule.class.getPackage().getName());
    }

    @Contribute(EntityManagerSource.class)
    public static void configurePersistenceUnit(
            MappedConfiguration<String, PersistenceUnitConfigurer> cfg, final ObjectLocator objectLocator)
    {
        PersistenceUnitConfigurer configurer = new PersistenceUnitConfigurer()
        {
            @Override
            public void configure(TapestryPersistenceUnitInfo unitInfo)
            {
                unitInfo.transactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL)
                        .persistenceProviderClassName(
                                "org.eclipse.persistence.jpa.PersistenceProvider")
                        .excludeUnlistedClasses(true)
                        .addProperty("javax.persistence.jdbc.user", "sa")
                        .addProperty("javax.persistence.jdbc.password", "sa")
                        .addProperty("javax.persistence.jdbc.driver", "org.h2.Driver")
                        .addProperty("javax.persistence.jdbc.url", "jdbc:h2:mem:jpatest")
                        .addProperty("eclipselink.ddl-generation", "create-or-extend-tables")
                        .addProperty("eclipselink.logging.level", "FINE")
                        .addManagedClass(ThingOne.class).addManagedClass(ThingTwo.class)
                        .addManagedClass(VersionedThing.class);
                unitInfo.getProperties().put("javax.persistence.bean.manager",
                        objectLocator.autobuild(TapestryCDIBeanManagerForJPAEntityListeners.class));
            }
        };
        // cfg.add("jpatest", configurer);
        cfg.add("TestUnit", configurer);
    }

    @Match(
    { "*Service" })
    public static void adviseTransactionally(JpaTransactionAdvisor advisor,
            MethodAdviceReceiver receiver)
    {
        advisor.addTransactionCommitAdvice(receiver);
    }

}
