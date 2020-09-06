// Copyright 2010, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.commons.ObjectLocator;
import org.apache.tapestry5.commons.services.PlasticProxyFactory;
import org.apache.tapestry5.ioc.OperationTracker;
import org.slf4j.Logger;

/**
 * Reloadable object creator for non-service objects.
 */
public class ReloadableObjectCreator extends AbstractReloadableObjectCreator
{
    private final ObjectLocator locator;

    public ReloadableObjectCreator(PlasticProxyFactory proxyFactory, ClassLoader baseClassLoader, String implementationClassName, Logger logger,
            OperationTracker tracker, ObjectLocator locator)
    {
        super(proxyFactory, baseClassLoader, implementationClassName, logger, tracker);

        this.locator = locator;
    }

    @Override
    protected Object createInstance(Class clazz)
    {
        return locator.autobuild(clazz);
    }

}
