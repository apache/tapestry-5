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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newCaseInsensitiveMap;
import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.util.Map;

import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.ioc.internal.util.OneShotLock;
import org.apache.tapestry.services.Alias;
import org.apache.tapestry.services.AliasManager;

public class AliasImpl implements Alias, ObjectProvider
{
    // Derived from the managers when first needed

    private final Map<String, Object> _properties = newCaseInsensitiveMap();

    private AliasManager _masterManager;

    private AliasManager _overridesManager;

    private String _mode;

    private final OneShotLock _lock = new OneShotLock();

    public AliasImpl(AliasManager masterManager, AliasManager overridesManager)
    {
        _masterManager = masterManager;
        _overridesManager = overridesManager;
    }

    public ObjectProvider getObjectProvider()
    {
        return this;
    }

    // Probably don't need to make this concurrent, since it executes at startup,
    // before multithreading takes hold.

    public synchronized void setMode(String mode)
    {
        _mode = notNull(mode, "mode");

        // This method may only be invoked once.

        _lock.lock();

        _properties.putAll(_masterManager.getAliasesForMode(_mode));
        _properties.putAll(_overridesManager.getAliasesForMode(_mode));

        _masterManager = null;
        _overridesManager = null;
    }

    public <T> T provide(String expression, Class<T> objectType, ServiceLocator locator)
    {
        if (_properties.isEmpty())
            throw new RuntimeException(ServicesMessages.aliasModeNotSet());

        Object object = _properties.get(expression);

        if (object == null)
            throw new RuntimeException(ServicesMessages.aliasPropertyNotFound(
                    expression,
                    _properties.keySet()));

        try
        {
            return objectType.cast(object);
        }
        catch (ClassCastException ex)
        {
            throw new RuntimeException(ServicesMessages.aliasPropertyWrongType(
                    expression,
                    object,
                    objectType), ex);
        }
    }

}
