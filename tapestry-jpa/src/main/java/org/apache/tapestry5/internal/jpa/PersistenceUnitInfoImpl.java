// Copyright 2011 The Apache Software Foundation
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

package org.apache.tapestry5.internal.jpa;

import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.jpa.TapestryPersistenceUnitInfo;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class PersistenceUnitInfoImpl implements TapestryPersistenceUnitInfo
{
    private String persistenceUnitName;

    private String persistenceProviderClassName;

    private String persistenceXMLSchemaVersion;

    private PersistenceUnitTransactionType transactionType = PersistenceUnitTransactionType.RESOURCE_LOCAL;

    private DataSource nonJtaDataSource;

    private DataSource jtaDataSource;

    private ValidationMode validationMode;

    private SharedCacheMode sharedCacheMode;

    private boolean excludeUnlistedClasses = true;

    private final Set<String> managedClassNames = CollectionFactory.newSet();

    private final Set<String> mappingFilesNames = CollectionFactory.newSet();

    private final List<URL> jarFileUrls = CollectionFactory.newList();

    private final Properties properties = new Properties();

		private Map entityManagerProperties;

    public PersistenceUnitInfoImpl(String persistenceUnitName)
    {
        this.persistenceUnitName = persistenceUnitName;
    }

    /**
     * {@inheritDoc}
     */
    public String getPersistenceUnitName()
    {
        return persistenceUnitName;
    }

    /**
     * {@inheritDoc}
     */
    public String getPersistenceProviderClassName()
    {
        return persistenceProviderClassName;
    }

    /**
     * {@inheritDoc}
     */
    public TapestryPersistenceUnitInfo persistenceProviderClassName(final String persistenceProviderClassName)
    {
        this.persistenceProviderClassName = persistenceProviderClassName;

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public PersistenceUnitTransactionType getTransactionType()
    {
        return transactionType;
    }

    /**
     * {@inheritDoc}
     */
    public TapestryPersistenceUnitInfo transactionType(final PersistenceUnitTransactionType transactionType)
    {
        this.transactionType = transactionType;

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public DataSource getJtaDataSource()
    {
        return jtaDataSource;
    }

    /**
     * {@inheritDoc}
     */
    public DataSource getNonJtaDataSource()
    {
        return nonJtaDataSource;
    }

    /**
     * {@inheritDoc}
     */
    public TapestryPersistenceUnitInfo nonJtaDataSource(final String nonJtaDataSource)
    {
        this.nonJtaDataSource = lookupDataSource(nonJtaDataSource);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public TapestryPersistenceUnitInfo jtaDataSource(final String jtaDataSource)
    {
        this.jtaDataSource = lookupDataSource(jtaDataSource);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getMappingFileNames()
    {
        List<String> tmp = CollectionFactory.newList(mappingFilesNames);
        return Collections.unmodifiableList(tmp);
    }

    /**
     * {@inheritDoc}
     */
    public TapestryPersistenceUnitInfo addMappingFileName(final String fileName)
    {
        mappingFilesNames.add(fileName);

        return this;

    }

    /**
     * {@inheritDoc}
     */
    public TapestryPersistenceUnitInfo addJarFileUrl(URL url)
    {
        jarFileUrls.add(url);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public TapestryPersistenceUnitInfo addJarFileUrl(String url)
    {
        try
        {
            return addJarFileUrl(new URL(getPersistenceUnitRootUrl(), url));
        } catch (MalformedURLException e)
        {
            throw new RuntimeException(e);
        }
    }


    /**
     * {@inheritDoc}
     */
    public TapestryPersistenceUnitInfo addProperty(String name, String value)
    {
        getProperties().put(name, value);

        return this;
    }


    /**
     * {@inheritDoc}
     */
    public TapestryPersistenceUnitInfo excludeUnlistedClasses(boolean exclude)
    {
        this.excludeUnlistedClasses = exclude;

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public List<URL> getJarFileUrls()
    {
        return Collections.unmodifiableList(jarFileUrls);
    }

    /**
     * {@inheritDoc}
     */
    public URL getPersistenceUnitRootUrl()
    {
        return getClass().getResource("/");
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getManagedClassNames()
    {
        List<String> tmp = CollectionFactory.newList(managedClassNames);
        return Collections.<String>unmodifiableList(tmp);
    }

    /**
     * {@inheritDoc}
     */
    public TapestryPersistenceUnitInfo addManagedClassName(final String className)
    {
        managedClassNames.add(className);

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public TapestryPersistenceUnitInfo addManagedClass(final Class<?> clazz)
    {
        addManagedClassName(clazz.getName());

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public boolean excludeUnlistedClasses()
    {
        return excludeUnlistedClasses;
    }

    /**
     * {@inheritDoc}
     */
    public SharedCacheMode getSharedCacheMode()
    {
        return sharedCacheMode;
    }

    /**
     * {@inheritDoc}
     */
    public TapestryPersistenceUnitInfo sharedCacheMode(final SharedCacheMode cacheMode)
    {
        sharedCacheMode = cacheMode;

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public ValidationMode getValidationMode()
    {
        return validationMode;
    }

    /**
     * {@inheritDoc}
     */
    public TapestryPersistenceUnitInfo validationMode(final ValidationMode validationMode)
    {
        this.validationMode = validationMode;

        return this;
    }

    /**
     * {@inheritDoc}
     */
    public Properties getProperties()
    {
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    public String getPersistenceXMLSchemaVersion()
    {
        return persistenceXMLSchemaVersion;
    }

    public void setPersistenceXMLSchemaVersion(final String version)
    {
        persistenceXMLSchemaVersion = version;
    }

    /**
     * {@inheritDoc}
     */
    public ClassLoader getClassLoader()
    {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * {@inheritDoc}
     */
    public void addTransformer(final ClassTransformer transformer)
    {

    }

    /**
     * {@inheritDoc}
     */
    public ClassLoader getNewTempClassLoader()
    {
        return getClassLoader();
    }


    private DataSource lookupDataSource(final String name)
    {
        try
        {
            // TODO: Create InitialContext with environment properties?
            final Context initContext = new InitialContext();

            final Context envContext = (Context) initContext.lookup("java:comp/env");

            return (DataSource) envContext.lookup(name);
        } catch (final NamingException e)
        {
            throw new RuntimeException(e);
        }

    }

    public TapestryPersistenceUnitInfo setEntityManagerProperties(Map properties) {
    	entityManagerProperties = properties;
    	return this;
    }

    public Map getEntityManagerProperties() {
    	return entityManagerProperties;
    }


}
