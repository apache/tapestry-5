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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.naming.OperationNotSupportedException;
import javax.swing.JFrame;

import org.apache.tapestry5.internal.services.BeanModelSourceImpl;
import org.apache.tapestry5.internal.services.PropertyConduitSourceImpl;
import org.apache.tapestry5.internal.services.StringInterner;
import org.apache.tapestry5.internal.services.StringInternerImpl;
import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.ioc.MessageFormatter;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.internal.BasicDataTypeAnalyzers;
import org.apache.tapestry5.ioc.internal.BasicTypeCoercions;
import org.apache.tapestry5.ioc.internal.services.PlasticProxyFactoryImpl;
import org.apache.tapestry5.ioc.internal.services.PropertyAccessImpl;
import org.apache.tapestry5.ioc.internal.services.TypeCoercerImpl;
import org.apache.tapestry5.ioc.services.CoercionTuple;
import org.apache.tapestry5.ioc.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.PropertyAdapter;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.services.BeanModelSource;
import org.apache.tapestry5.services.DataTypeAnalyzer;
import org.apache.tapestry5.services.PropertyConduitSource;
import org.slf4j.LoggerFactory;

/**
 * Utility class for creating {@link BeanModelSource} instances without
 * Tapestry-IoC. Usage of Tapestry-IoC is still recommended.
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
     * Sets the {@link TypeCoercer} to be used.
     */
    public BeanModelSourceBuilder setTypeCoercer(TypeCoercer typeCoercer)
    {
        this.typeCoercer = typeCoercer;
        return this;
    }

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
        
        return new BeanModelSourceImpl(typeCoercer, propertyAccess, propertyConduitSource, plasticProxyFactory, dataTypeAnalyzer, objectLocator);
        
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

}
