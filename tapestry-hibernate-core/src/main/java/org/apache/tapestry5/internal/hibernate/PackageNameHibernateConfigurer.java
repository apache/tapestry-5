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

import org.apache.tapestry5.hibernate.HibernateConfigurer;
import org.apache.tapestry5.hibernate.HibernateEntityPackageManager;
import org.apache.tapestry5.ioc.internal.util.Defense;
import org.apache.tapestry5.ioc.services.ClassNameLocator;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

/**
 * Adds entity classes from a given set of packages to the configuration.
 */
public final class PackageNameHibernateConfigurer implements HibernateConfigurer
{
    private final HibernateEntityPackageManager packageManager;

    private final ClassNameLocator classNameLocator;

    public PackageNameHibernateConfigurer(HibernateEntityPackageManager packageManager,
                                          ClassNameLocator classNameLocator)
    {
        this.packageManager = packageManager;
        this.classNameLocator = classNameLocator;
    }

    public void configure(Configuration configuration)
    {
        Defense.cast(configuration, AnnotationConfiguration.class, "configuration");
        AnnotationConfiguration cfg = (AnnotationConfiguration) configuration;

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        for (String packageName : packageManager.getPackageNames())
        {
            cfg.addPackage(packageName);

            for (String className : classNameLocator.locateClassNames(packageName))
            {
                try
                {
                    Class entityClass = contextClassLoader.loadClass(className);

                    cfg.addAnnotatedClass(entityClass);
                }
                catch (ClassNotFoundException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
}
