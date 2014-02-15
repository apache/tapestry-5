// Copyright 2011, 2014 The Apache Software Foundation
//
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

package org.apache.tapestry5.jpa;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.net.URL;
import java.util.Map;

/**
 * Tapestry's mutable extension of {@link PersistenceUnitInfo} interface used for XML-less configuration
 * of persistence units.
 *
 * @since 5.3
 */
public interface TapestryPersistenceUnitInfo extends PersistenceUnitInfo
{
    /**
     * Set the the fully qualified name of the persistence provider implementation class.
     * Corresponds to the <code>provider</code> element in the <code>persistence.xml</code> file.
     *
     * @param persistenceProviderClassName
     *         persistence provider's class name
     */
    TapestryPersistenceUnitInfo persistenceProviderClassName(String persistenceProviderClassName);

    /**
     * Set the transaction type of the entity managers. Corresponds to
     * the <code>transaction-type</code> attribute in the <code>persistence.xml</code> file.
     *
     * @param transactionType
     *         transition type to set
     */
    TapestryPersistenceUnitInfo transactionType(PersistenceUnitTransactionType transactionType);

    /**
     * Set the non-JTA-enabled data source to be used by the persistence provider for accessing data outside a JTA
     * transaction. Corresponds to the named <code>non-jta-data-source</code> element in the
     * <code>persistence.xml</code> file.
     *
     * @param nonJtaDataSource
     *         data source to set
     */
    TapestryPersistenceUnitInfo nonJtaDataSource(String nonJtaDataSource);

    /**
     * Set the JTA-enabled data source to be used by the persistence provider for accessing data outside a JTA
     * transaction. Corresponds to the named <code>jta-data-source</code> element in the
     * <code>persistence.xml</code> file.
     *
     * @param jtaDataSource
     *         data source to set
     */
    TapestryPersistenceUnitInfo jtaDataSource(String jtaDataSource);

    /**
     * Add a managed class name to be used by persistence provider.
     * Corresponds to a named <code>class</code> element in the <code>persistence.xml</code> file.
     *
     * @param className
     *         class name to add
     * @see #addManagedClass(Class)
     */
    TapestryPersistenceUnitInfo addManagedClassName(String className);

    /**
     * Add a managed class to be used by persistence provider.
     * Corresponds to a named <code>class</code> element in the <code>persistence.xml</code> file.
     *
     * @param clazz
     *         class to add
     * @see #addManagedClassName(String)
     */
    TapestryPersistenceUnitInfo addManagedClass(Class<?> clazz);

    /**
     * Defines how the persistence provider must use a second-level cache for the persistence unit.
     * Corresponds to the <code>shared-cache-mode</code> element in the <code>persistence.xml</code> file.
     *
     * @param cacheMode
     *         cache mode to set
     */
    TapestryPersistenceUnitInfo sharedCacheMode(SharedCacheMode cacheMode);

    /**
     * Set the validation mode to be used by the persistence provider for the persistence unit.
     * Corresponds to the <code>validation-mode</code> element in the <code>persistence.xml</code> file.
     *
     * @param validationMode
     *         validation mode to set
     */
    TapestryPersistenceUnitInfo validationMode(ValidationMode validationMode);

    /**
     * Add a mapping file to be loaded by the persistence provider to determine the mappings for
     * the entity classes. Corresponds to a <code>mapping-file</code> element in the <code>persistence.xml</code> file.
     *
     * @param fileName
     *         mapping file name to add
     */
    TapestryPersistenceUnitInfo addMappingFileName(String fileName);

    /**
     * Add a URLs for the jar file or exploded jar file directory that the persistence provider must examine
     * for managed classes of the persistence unit. Corresponds to a <code>jar-file</code> element in the
     * <code>persistence.xml</code> file.
     *
     * @param url
     *         url to add
     */
    TapestryPersistenceUnitInfo addJarFileUrl(URL url);

    /**
     * Add a URLs for the jar file or exploded jar file directory that the persistence provider must examine
     * for managed classes of the persistence unit. Corresponds to a <code>jar-file</code> element in the
     * <code>persistence.xml</code> file.
     *
     * @param url
     *         url to add
     */
    TapestryPersistenceUnitInfo addJarFileUrl(String url);

    /**
     * Add a property. Corresponds to a <code>property</code> element in the <code>persistence.xml</code> file.
     *
     * @param name
     *         property's name
     * @param value
     *         property's value
     */
    TapestryPersistenceUnitInfo addProperty(String name, String value);

    /**
     * Defines whether classes in the root of the persistence unit that have not been explicitly listed
     * are to be included in the set of managed classes. Corresponds to the <code>exclude-unlisted-classes</code>
     * element in the <code>persistence.xml</code> file.
     *
     * @param exclude
     *         defines whether to exclude or not
     */
    TapestryPersistenceUnitInfo excludeUnlistedClasses(boolean exclude);

    /**
     * {@link javax.persistence.spi.PersistenceProvider} allows creating an {@link javax.persistence.EntityManagerFactory}
     * with a default EntityManager properties map. This operation allows contributing default properties for
     * EntityManager.
     *
     * @param properties
     *         properties to initialize EntityManagerFactory with
     *         @since 5.4
     */
    TapestryPersistenceUnitInfo setEntityManagerProperties(Map properties);

    /**
     * @return Returns the supplied EntityManagerFactory properties. Returns null if not set.
     * @since 5.4
     */
    Map getEntityManagerProperties();
}
