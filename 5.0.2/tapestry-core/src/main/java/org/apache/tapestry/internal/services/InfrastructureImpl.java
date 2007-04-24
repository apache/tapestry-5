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

import static org.apache.tapestry.ioc.internal.util.Defense.notNull;

import java.util.Map;

import org.apache.tapestry.ioc.ObjectProvider;
import org.apache.tapestry.ioc.ServiceLocator;
import org.apache.tapestry.services.Infrastructure;
import org.apache.tapestry.services.InfrastructureManager;

/**
 * TODO: Extra configuration to support application overrides.
 */
public class InfrastructureImpl implements Infrastructure, ObjectProvider
{
    private final InfrastructureManager _manager;

    // Derived from the manager when first needed

    private Map<String, Object> _properties;

    private String _mode;

    public InfrastructureImpl(InfrastructureManager manager)
    {
        _manager = manager;
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

        _properties = _manager.getContributionsForMode(_mode);
    }

    public <T> T provide(String expression, Class<T> objectType, ServiceLocator locator)
    {
        if (_properties == null)
            throw new RuntimeException(ServicesMessages.infrastructureModeNotSet());

        Object object = _properties.get(expression);

        if (object == null)
            throw new RuntimeException(ServicesMessages.infrastructurePropertyNotFound(
                    expression,
                    _properties.keySet()));

        try
        {
            return objectType.cast(object);
        }
        catch (ClassCastException ex)
        {
            throw new RuntimeException(ServicesMessages.infrastructurePropertyWrongType(
                    expression,
                    object,
                    objectType), ex);
        }
    }

}
