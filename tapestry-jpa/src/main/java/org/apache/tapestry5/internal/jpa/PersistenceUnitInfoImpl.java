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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.apache.tapestry5.jpa.TapestryPersistenceUnitInfo;

public class PersistenceUnitInfoImpl implements TapestryPersistenceUnitInfo
{
    private String persistenceUnitName;

    private String persistenceProviderClassName;

    private String persistenceXMLSchemaVersion;

    private PersistenceUnitTransactionType transactionType;

    private DataSource nonJtaDataSource;

    private ValidationMode validationMode;

    private SharedCacheMode sharedCacheMode;

    private final List<String> managedClassNames = new ArrayList<String>();

    private final Properties properties = new Properties();

    /**
     * {@inheritDoc}
     */
    public String getPersistenceUnitName()
    {
        return persistenceUnitName;
    }

    public void setPersistenceUnitName(final String persistenceUnitName)
    {
        this.persistenceUnitName = persistenceUnitName;
    }

    /**
     * {@inheritDoc}
     */
    public String getPersistenceProviderClassName()
    {
        return persistenceProviderClassName;
    }

    public void setPersistenceProviderClassName(final String persistenceProviderClassName)
    {
        this.persistenceProviderClassName = persistenceProviderClassName;
    }

    /**
     * {@inheritDoc}
     */
    public PersistenceUnitTransactionType getTransactionType()
    {
        return transactionType;
    }

    public void setTransactionType(final PersistenceUnitTransactionType transactionType)
    {
        this.transactionType = transactionType;
    }

    /**
     * {@inheritDoc}
     */
    public DataSource getJtaDataSource()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public DataSource getNonJtaDataSource()
    {
        return nonJtaDataSource;
    }

    public void setNonJtaDataSource(final DataSource nonJtaDataSource)
    {
        this.nonJtaDataSource = nonJtaDataSource;
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getMappingFileNames()
    {
        return Arrays.asList();
    }

    /**
     * {@inheritDoc}
     */
    public List<URL> getJarFileUrls()
    {
        return Arrays.asList();
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
        return Collections.unmodifiableList(managedClassNames);
    }

    public void addManagedClassName(final String className)
    {
        managedClassNames.add(className);
    }

    public void addManagedClass(final Class<?> clazz)
    {
        addManagedClassName(clazz.getName());
    }

    /**
     * {@inheritDoc}
     */
    public boolean excludeUnlistedClasses()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public SharedCacheMode getSharedCacheMode()
    {
        return sharedCacheMode;
    }

    public void setSharedCacheMode(final SharedCacheMode cacheMode)
    {
        sharedCacheMode = cacheMode;
    }

    /**
     * {@inheritDoc}
     */
    public ValidationMode getValidationMode()
    {
        return validationMode;
    }

    public void setValidationMode(final ValidationMode validationMode)
    {
        this.validationMode = validationMode;
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

}
