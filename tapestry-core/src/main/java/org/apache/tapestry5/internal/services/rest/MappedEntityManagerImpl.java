// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.tapestry5.internal.services.rest;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.tapestry5.ioc.services.ClassNameLocator;
import org.apache.tapestry5.services.rest.MappedEntityManager;

/**
 * Default {@link MappedEntityManager} implementation.
 */
public class MappedEntityManagerImpl implements MappedEntityManager
{
    
    private final Set<Class<?>> entities;
    
    public MappedEntityManagerImpl(Collection<String> packages, final ClassNameLocator classNameLocator) 
    {
        
        Set<Class<?>> classes = new HashSet<>();
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        
        for (String packageName : packages)
        {
            for (String className : classNameLocator.locateClassNames(packageName))
            {
                try
                {
                    Class<?> entityClass = contextClassLoader.loadClass(className);
                    classes.add(entityClass);
                }
                catch (ClassNotFoundException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        }
        
        entities = Collections.unmodifiableSet(new HashSet<>(classes));
        
    }

    @Override
    public Set<Class<?>> getEntities() 
    {
        return entities;
    }

}
