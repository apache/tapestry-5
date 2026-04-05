// Copyright 2011, 2026 The Apache Software Foundation
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

package org.apache.tapestry5.internal.jpa.core;

import org.apache.tapestry5.ioc.services.ClassNameLocator;
import org.apache.tapestry5.jpa.core.JpaEntityPackageManager;
import org.apache.tapestry5.jpa.core.PersistenceUnitConfigurer;
import org.apache.tapestry5.jpa.core.TapestryPersistenceUnitInfo;

public class PackageNamePersistenceUnitConfigurer implements PersistenceUnitConfigurer
{
    private final JpaEntityPackageManager packageManager;

    private final ClassNameLocator classNameLocator;

    public PackageNamePersistenceUnitConfigurer(JpaEntityPackageManager packageManager,
            ClassNameLocator classNameLocator)
    {
        this.packageManager = packageManager;
        this.classNameLocator = classNameLocator;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TapestryPersistenceUnitInfo unitInfo)
    {
        for (String packageName : packageManager.getPackageNames())
        {
            for (String className : classNameLocator.locateClassNames(packageName))
            {
                unitInfo.addManagedClassName(className);
            }
        }

    }

}
