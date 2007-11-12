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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ioc.AnnotationProvider;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.ioc.ObjectProvider;
import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;
import org.apache.tapestry.services.Alias;
import org.apache.tapestry.services.AliasManager;

import java.util.Map;

public class AliasImpl implements Alias, ObjectProvider
{
    // Derived from the managers when first needed

    private final Map<Class, Object> _properties = newMap();

    private final String _mode;

    private boolean _initialized = false;

    private AliasManager _masterManager;

    private AliasManager _overridesManager;

    public AliasImpl(AliasManager masterManager, String mode, AliasManager overridesManager)
    {
        _masterManager = masterManager;
        _mode = mode;
        _overridesManager = overridesManager;
    }

    public ObjectProvider getObjectProvider()
    {
        return this;
    }

    private synchronized void initialize()
    {
        if (_initialized) return;

        _properties.putAll(_masterManager.getAliasesForMode(_mode));
        _properties.putAll(_overridesManager.getAliasesForMode(_mode));

        _masterManager = null;
        _overridesManager = null;

        _initialized = true;
    }

    public <T> T provide(Class<T> objectType, AnnotationProvider annotationProvider,
                         ObjectLocator locator)
    {
        initialize();

        Object object = _properties.get(objectType);

        // Let another provider handle this (probably the default object provider)
        if (object == null) return null;

        // A ClassCastException should never occur.

        return objectType.cast(object);
    }
}
