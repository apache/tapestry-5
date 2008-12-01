// Copyright 2007, 2008 The Apache Software Foundation
//
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

package org.apache.tapestry5.internal.hibernate;

import org.dom4j.Document;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Mappings;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.cfg.Settings;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.engine.Mapping;
import org.hibernate.event.EventListeners;
import org.hibernate.mapping.AuxiliaryDatabaseObject;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.xml.sax.EntityResolver;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Delegates all method calls to another instance. Any calls that modify state or return objects that are meant to be
 * used to change state will throw an {@link UnsupportedOperationException}. This class is specifically final because
 * there are protected methods that cannot be called on the contained instance (because they are protected).
 * <p/>
 * Note that this class does not guarantee that the objects returned are mutable thus changes to the configuration are
 * still possible.
 */
@SuppressWarnings("unchecked")
final class ImmutableConfiguration extends Configuration
{
    private static final long serialVersionUID = -4039250481581260132L;

    private final Configuration config;

    public ImmutableConfiguration(Configuration configuration)
    {
        config = configuration;
    }

    @Override
    protected void add(Document doc) throws MappingException
    {
        unsupported();
    }

    /**
     * Throws an exception. Has a return value for convenience.
     *
     * @return nothing because it always throws an exception
     */
    private <T> T unsupported()
    {
        throw new UnsupportedOperationException(HibernateCoreMessages.configurationImmutable());
    }

    @Override
    public void addAuxiliaryDatabaseObject(AuxiliaryDatabaseObject object)
    {
        unsupported();
    }

    @Override
    public Configuration addCacheableFile(File xmlFile) throws MappingException
    {
        return unsupported();
    }

    @Override
    public Configuration addCacheableFile(String xmlFile) throws MappingException
    {
        return unsupported();
    }

    @Override
    public Configuration addClass(Class persistentClass) throws MappingException
    {
        return unsupported();
    }

    @Override
    public Configuration addDirectory(File dir) throws MappingException
    {
        return unsupported();
    }

    @Override
    public Configuration addDocument(org.w3c.dom.Document doc) throws MappingException
    {
        return unsupported();
    }

    @Override
    public Configuration addFile(File xmlFile) throws MappingException
    {
        return unsupported();
    }

    @Override
    public Configuration addFile(String xmlFile) throws MappingException
    {
        return unsupported();
    }

    @Override
    public void addFilterDefinition(FilterDefinition definition)
    {
        unsupported();
    }

    @Override
    public Configuration addInputStream(InputStream xmlInputStream) throws MappingException
    {
        return unsupported();
    }

    @Override
    public Configuration addJar(File jar) throws MappingException
    {
        return unsupported();
    }

    @Override
    public Configuration addProperties(Properties extraProperties)
    {
        return unsupported();
    }

    @Override
    public Configuration addResource(String resourceName, ClassLoader classLoader) throws MappingException
    {
        return unsupported();
    }

    @Override
    public Configuration addResource(String resourceName) throws MappingException
    {
        return unsupported();
    }

    @Override
    public void addSqlFunction(String functionName, SQLFunction function)
    {
        unsupported();
    }

    @Override
    public Configuration addURL(URL url) throws MappingException
    {
        return unsupported();
    }

    @Override
    public Configuration addXML(String xml) throws MappingException
    {
        return unsupported();
    }

    /* Since this is called from the constructor of the superclass, it calls
      * the superclass method rather than delegating to the contained instance.
      * We could also just not override the method but for completeness I'll
      * leave it in.
      * It's unfortunate that Configuration isn't an interface.
      */
    @Override
    public Mapping buildMapping()
    {
        return super.buildMapping();
    }

    @Override
    public void buildMappings()
    {
        config.buildMappings();
    }

    @Override
    public SessionFactory buildSessionFactory() throws HibernateException
    {
        return config.buildSessionFactory();
    }

    @Override
    public Settings buildSettings() throws HibernateException
    {
        return config.buildSettings();
    }

    @Override
    public Settings buildSettings(Properties props) throws HibernateException
    {
        return config.buildSettings(props);
    }

    @Override
    public Configuration configure() throws HibernateException
    {
        return unsupported();
    }

    @Override
    public Configuration configure(org.w3c.dom.Document document) throws HibernateException
    {
        return unsupported();
    }

    @Override
    public Configuration configure(File configFile) throws HibernateException
    {
        return unsupported();
    }

    @Override
    public Configuration configure(String resource) throws HibernateException
    {
        return unsupported();
    }

    @Override
    public Configuration configure(URL url) throws HibernateException
    {
        return unsupported();
    }

    @Override
    public Mappings createMappings()
    {
        return unsupported();
    }

    @Override
    protected Configuration doConfigure(Document doc) throws HibernateException
    {
        return unsupported();
    }

    @Override
    protected Configuration doConfigure(InputStream stream, String resourceName) throws HibernateException
    {
        return unsupported();
    }

    @Override
    protected Document findPossibleExtends()
    {
        return unsupported();
    }

    @Override
    public String[] generateDropSchemaScript(Dialect dialect) throws HibernateException
    {
        return config.generateDropSchemaScript(dialect);
    }

    @Override
    public String[] generateSchemaCreationScript(Dialect dialect) throws HibernateException
    {
        return config.generateSchemaCreationScript(dialect);
    }

    @Override
    public String[] generateSchemaUpdateScript(Dialect dialect, DatabaseMetadata databaseMetadata)
            throws HibernateException
    {
        return config.generateSchemaUpdateScript(dialect, databaseMetadata);
    }

    @Override
    public PersistentClass getClassMapping(String entityName)
    {
        return config.getClassMapping(entityName);
    }

    @Override
    public Iterator getClassMappings()
    {
        return config.getClassMappings();
    }

    @Override
    public Collection getCollectionMapping(String role)
    {
        return config.getCollectionMapping(role);
    }

    @Override
    public Iterator getCollectionMappings()
    {
        return config.getCollectionMappings();
    }

    @Override
    public EntityNotFoundDelegate getEntityNotFoundDelegate()
    {
        return config.getEntityNotFoundDelegate();
    }

    @Override
    public EntityResolver getEntityResolver()
    {
        return config.getEntityResolver();
    }

    @Override
    public EventListeners getEventListeners()
    {
        return config.getEventListeners();
    }

    @Override
    public Map getFilterDefinitions()
    {
        return config.getFilterDefinitions();
    }

    @Override
    public Map getImports()
    {
        return config.getImports();
    }

    @Override
    public Interceptor getInterceptor()
    {
        return config.getInterceptor();
    }

    @Override
    public Map getNamedQueries()
    {
        return config.getNamedQueries();
    }

    @Override
    public Map getNamedSQLQueries()
    {
        return config.getNamedSQLQueries();
    }

    @Override
    public NamingStrategy getNamingStrategy()
    {
        return config.getNamingStrategy();
    }

    @Override
    public Properties getProperties()
    {
        return config.getProperties();
    }

    @Override
    public String getProperty(String propertyName)
    {
        return config.getProperty(propertyName);
    }

    @Override
    public Map getSqlFunctions()
    {
        return config.getSqlFunctions();
    }

    @Override
    public Map getSqlResultSetMappings()
    {
        return config.getSqlResultSetMappings();
    }

    @Override
    public Iterator getTableMappings()
    {
        return config.getTableMappings();
    }

    @Override
    public Configuration mergeProperties(Properties properties)
    {
        return unsupported();
    }

    @Override
    public void setCacheConcurrencyStrategy(String clazz, String concurrencyStrategy, String region)
            throws MappingException
    {
        unsupported();
    }

    @Override
    public Configuration setCacheConcurrencyStrategy(String clazz, String concurrencyStrategy) throws MappingException
    {
        return unsupported();
    }

    @Override
    public void setCollectionCacheConcurrencyStrategy(String collectionRole, String concurrencyStrategy, String region)
            throws MappingException
    {
        unsupported();
    }

    @Override
    public Configuration setCollectionCacheConcurrencyStrategy(String collectionRole, String concurrencyStrategy)
            throws MappingException
    {
        return unsupported();
    }

    @Override
    public void setEntityNotFoundDelegate(EntityNotFoundDelegate entityNotFoundDelegate)
    {
        unsupported();
    }

    @Override
    public void setEntityResolver(EntityResolver entityResolver)
    {
        unsupported();
    }

    @Override
    public Configuration setInterceptor(Interceptor interceptor)
    {
        return unsupported();
    }

    @Override
    public void setListener(String type, Object listener)
    {
        unsupported();
    }

    @Override
    public void setListeners(String type, Object[] listeners)
    {
        unsupported();
    }

    @Override
    public void setListeners(String type, String[] listenerClasses)
    {
        unsupported();
    }

    @Override
    public Configuration setNamingStrategy(NamingStrategy namingStrategy)
    {
        return unsupported();
    }

    @Override
    public Configuration setProperties(Properties properties)
    {
        return unsupported();
    }

    @Override
    public Configuration setProperty(String propertyName, String value)
    {
        return unsupported();
    }

    @Override
    public void validateSchema(Dialect dialect, DatabaseMetadata databaseMetadata) throws HibernateException
    {
        config.validateSchema(dialect, databaseMetadata);
    }

    @Override
    public String toString()
    {
        return "ImmutableConfiguration[" + config + "]";
    }
}
