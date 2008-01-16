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

package org.apache.tapestry.internal.services;

import org.apache.tapestry.ioc.internal.util.TapestryException;
import org.apache.tapestry.runtime.Component;
import org.apache.tapestry.services.ComponentEventResultProcessor;

import java.io.IOException;
import java.util.Collection;

/**
 * A catch-all for type Object that reports the return value as an error.
 */
public class ObjectComponentEventResultProcessor implements ComponentEventResultProcessor<Object>
{
    private final Collection<Class> _configuredClasses;

    public ObjectComponentEventResultProcessor(Collection<Class> configuredClasses)
    {
        _configuredClasses = configuredClasses;
    }

    public void processResultValue(Object value, Component component, String methodDescripion) throws IOException
    {
        String message = ServicesMessages.invalidComponentEventResult(component, value, methodDescripion,
                                                                      _configuredClasses);

        throw new TapestryException(message, component, null);
    }

}
