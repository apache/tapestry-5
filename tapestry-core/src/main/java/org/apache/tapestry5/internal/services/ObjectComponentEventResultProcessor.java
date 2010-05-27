// Copyright 2007, 2008, 2010 The Apache Software Foundation
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

package org.apache.tapestry5.internal.services;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.tapestry5.func.F;
import org.apache.tapestry5.func.Mapper;
import org.apache.tapestry5.ioc.services.ClassFabUtils;
import org.apache.tapestry5.ioc.util.AvailableValues;
import org.apache.tapestry5.ioc.util.UnknownValueException;
import org.apache.tapestry5.services.ComponentEventResultProcessor;

/**
 * A catch-all for type Object that reports the return value as an error.
 */
@SuppressWarnings("unchecked")
public class ObjectComponentEventResultProcessor implements ComponentEventResultProcessor<Object>
{
    private final Collection<Class> configuredClasses;

    public ObjectComponentEventResultProcessor(Collection<Class> configuredClasses)
    {
        this.configuredClasses = configuredClasses;
    }

    public void processResultValue(Object value) throws IOException
    {
        List<String> names = F.flow(configuredClasses).map(new Mapper<Class, String>()
        {
            public String map(Class input)
            {
                return ClassFabUtils.toJavaClassName(input);
            }
        }).toList();

        String message = String.format(
                "A component event handler method returned the value %s. Return type %s can not be handled.", value,
                ClassFabUtils.toJavaClassName(value.getClass()));

        throw new UnknownValueException(message, new AvailableValues("Configured return types", names));
    }
}
