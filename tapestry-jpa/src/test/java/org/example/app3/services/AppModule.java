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

package org.example.app3.services;

import org.apache.tapestry5.commons.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.services.ApplicationDefaults;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.jpa.EntityManagerSource;
import org.apache.tapestry5.jpa.JpaSymbols;
import org.apache.tapestry5.jpa.PersistenceUnitConfigurer;
import org.apache.tapestry5.jpa.TapestryPersistenceUnitInfo;
import org.apache.tapestry5.jpa.modules.JpaModule;

@ImportModule(JpaModule.class)
public class AppModule
{
    @Contribute(SymbolProvider.class)
    @ApplicationDefaults
    public static void provideFactoryDefaults(
            final MappedConfiguration<String, String> configuration)
    {
        //META-INF/persistence.xml is already on the test classpath,
        //so we need to pretend as if it doesn't exists
        configuration.add(JpaSymbols.PERSISTENCE_DESCRIPTOR, "/does-not-exist.xml");
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
                unitInfo.nonJtaDataSource("jdbc/JPATest")
                        .addMappingFileName("mappings.xml")
                        .addProperty("eclipselink.ddl-generation", "create-tables")
                        .addProperty("eclipselink.logging.level", "fine");
            }
        };

        configuration.add("JndiDataSourcePersistenceUnit", configurer);

    }
}
