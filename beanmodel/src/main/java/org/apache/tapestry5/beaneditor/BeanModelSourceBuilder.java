// Copyright 2014 The Apache Software Foundation
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
package org.apache.tapestry5.beaneditor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.tapestry5.internal.services.BeanModelSourceImpl;
import org.apache.tapestry5.internal.services.PropertyConduitSourceImpl;
import org.apache.tapestry5.internal.services.StringInterner;
import org.apache.tapestry5.internal.services.StringInternerImpl;
import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.internal.BasicDataTypeAnalyzers;
import org.apache.tapestry5.ioc.internal.BasicTypeCoercions;
import org.apache.tapestry5.ioc.internal.services.PlasticProxyFactoryImpl;
import org.apache.tapestry5.ioc.internal.services.PropertyAccessImpl;
import org.apache.tapestry5.ioc.internal.services.TypeCoercerImpl;
import org.apache.tapestry5.ioc.internal.util.TapestryException;
import org.apache.tapestry5.ioc.services.CoercionTuple;
import org.apache.tapestry5.ioc.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.DataTypeAnalyzer;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.slf4j.LoggerFactory;

/**
 * <p>Utility class for creating {@link BeanModelSource} instances without
 * Tapestry-IoC. Usage of Tapestry-IoC is still recommended.
 * </p>
 * <p>The setter methods can be used to customize the BeanModelSource to be created and can be 
 * (and usually are skipped), so <code>BeanModelSource beanModelSource = new BeanModelSourceBuilder().build();</code>
 * is all you need to do. 
 */
public class BeanModelSourceBuilder {

    private TypeCoercer typeCoercer;
    private PropertyAccess propertyAccess;
    private PropertyConduitSource propertyConduitSource;
    private PlasticProxyFactory plasticProxyFactory;
    private DataTypeAnalyzer dataTypeAnalyzer;
    private ObjectLocator objectLocator;
    private StringInterner stringInterner;

    /**
     * Creates and returns a {@link BeanModelSource} instance.
     */
    public BeanModelSource build() 
    {
        
        if (typeCoercer == null) 
        {
            createTypeCoercer();
        }
        
        if (propertyAccess == null)
        {
            propertyAccess = new PropertyAccessImpl();
        }
        
        if (dataTypeAnalyzer == null)
        {
            dataTypeAnalyzer = BasicDataTypeAnalyzers.createDefaultDataTypeAnalyzer();
        }
        
        if (stringInterner == null)
        {
            stringInterner = new StringInternerImpl();
        }
        
        if (plasticProxyFactory == null)
        {
            plasticProxyFactory = new PlasticProxyFactoryImpl(getClass().getClassLoader(), LoggerFactory.getLogger(PlasticProxyFactory.class));
        }
        
        if (propertyConduitSource == null)
        {
            propertyConduitSource = new PropertyConduitSourceImpl(propertyAccess, plasticProxyFactory, typeCoercer, stringInterner);
        }
        
        if (objectLocator == null)
        {
            objectLocator = new AutobuildOnlyObjectLocator();
        }
        
        return new BeanModelSourceImpl(typeCoercer, propertyAccess, propertyConduitSource, plasticProxyFactory, dataTypeAnalyzer, objectLocator);
        
    }
    
    /**
     * Sets the {@link TypeCoercer} to be used.
     */
    public BeanModelSourceBuilder setTypeCoercer(TypeCoercer typeCoercer)
    {
        this.typeCoercer = typeCoercer;
        return this;
    }

    /**
     * Sets the {@link PropertyAccess} to be used.
     */
    public BeanModelSourceBuilder setPropertyAccess(PropertyAccess propertyAccess)
    {
        this.propertyAccess = propertyAccess;
        return this;
    }

    /**
     * Sets the {@link PropertyConduitSource} to be used.
     */
    public BeanModelSourceBuilder setPropertyConduitSource(PropertyConduitSource propertyConduitSource)
    {
        this.propertyConduitSource = propertyConduitSource;
        return this;
    }

    /**
     * Sets the {@link PlasticProxyFactory} to be used.
     */
    public BeanModelSourceBuilder setPlasticProxyFactory(PlasticProxyFactory plasticProxyFactory)
    {
        this.plasticProxyFactory = plasticProxyFactory;
        return this;
    }

    /**
     * Sets the {@link DataTypeAnalyzer} to be used.
     */
    public BeanModelSourceBuilder setDataTypeAnalyzer(DataTypeAnalyzer dataTypeAnalyzer)
    {
        this.dataTypeAnalyzer = dataTypeAnalyzer;
        return this;
    }

    /**
     * Sets the {@link ObjectLocator} to be used. Actually, the only method of it actually used is
     * {@link ObjectLocator#autobuild(Class)}, for creating objects of the class described by the
     * {@link BeanModel}.
     */
    public BeanModelSourceBuilder setObjectLocator(ObjectLocator objectLocator)
    {
        this.objectLocator = objectLocator;
        return this;
    }

    /**
     * Sets the {@link StringInterner} to be used.
     */
    public BeanModelSourceBuilder setStringInterner(StringInterner stringInterner)
    {
        this.stringInterner = stringInterner;
        return this;
    }
    
    private void createTypeCoercer() 
    {
        CoercionTupleConfiguration configuration = new CoercionTupleConfiguration();
        BasicTypeCoercions.provideBasicTypeCoercions(configuration);
        typeCoercer = new TypeCoercerImpl(configuration.getTuples());
    }

    final private static class CoercionTupleConfiguration implements Configuration<CoercionTuple> 
    {

        final private Collection<CoercionTuple> tuples = new ArrayList<CoercionTuple>();

        @Override
        public void add(CoercionTuple tuble) 
        {
            tuples.add(tuble);
        }

        @Override
        public void addInstance(Class<? extends CoercionTuple> clazz) 
        {
            throw new RuntimeException("Not implemented");
        }

        public Collection<CoercionTuple> getTuples() 
        {
            return tuples;
        }

    }
    
    final private static class AutobuildOnlyObjectLocator implements ObjectLocator {

        @Override
        public <T> T getService(String serviceId, Class<T> serviceInterface)
        {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public <T> T getService(Class<T> serviceInterface)
        {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public <T> T getService(Class<T> serviceInterface,
                Class<? extends Annotation>... markerTypes)
        {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public <T> T getObject(Class<T> objectType, AnnotationProvider annotationProvider)
        {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public <T> T autobuild(Class<T> clazz)
        {
            try
            {
                return clazz.newInstance();
            }
            catch (Exception e)
            {
                throw new TapestryException("Couldn't instantiate class " + clazz.getName(), e);
            }
        }

        @Override
        public <T> T autobuild(String description, Class<T> clazz)
        {
            return autobuild(clazz);
        }

        public <T> T proxy(Class<T> interfaceClass, Class<? extends T> implementationClass)
        {
            throw new RuntimeException("Not implemented");
        }
        
    }

}
