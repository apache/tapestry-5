// Copyright 2006, 2007 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ioc.AnnotationProvider;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.ObjectProvider;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.services.Alias;
import org.apache.tapestry5.services.AliasManager;

import java.util.Map;

public class AliasImpl implements Alias, ObjectProvider
{
    // Derived from the managers when first needed

    private final Map<Class, Object> properties = CollectionFactory.newMap();

    private final String mode;

    private boolean initialized = false;

    private AliasManager masterManager;

    private AliasManager overridesManager;

    public AliasImpl(AliasManager masterManager, String mode, AliasManager overridesManager)
    {
        this.masterManager = masterManager;
        this.mode = mode;
        this.overridesManager = overridesManager;
    }

    public ObjectProvider getObjectProvider()
    {
        return this;
    }

    private synchronized void initialize()
    {
        if (initialized) return;

        properties.putAll(masterManager.getAliasesForMode(mode));
        properties.putAll(overridesManager.getAliasesForMode(mode));

        masterManager = null;
        overridesManager = null;

        initialized = true;
    }

    public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider,
                         ObjectLocator locator)
    {
        initialize();

        Object object = properties.get(objectType);

        // Let another provider handle this (probably the default object provider)
        if (object == null) return null;

        // A ClassCastException should never occur.

        return objectType.cast(object);
    }
}
