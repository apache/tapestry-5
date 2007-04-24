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

import static org.apache.tapestry.ioc.internal.util.CollectionFactory.newMap;

import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.tapestry.ioc.Messages;
import org.apache.tapestry.ioc.ObjectLocator;
import org.apache.tapestry.model.MutableComponentModel;
import org.apache.tapestry.services.ClassTransformation;
import org.apache.tapestry.services.InjectionProvider;

/**
 * Allows for a number of annonymous injections based on the type of field that is to be injected.
 */
public class CommonResourcesInjectionProvider implements InjectionProvider
{
    private static final Map<String, String> _configuration = newMap();

    public CommonResourcesInjectionProvider()
    {
        add(Messages.class, "getMessages");
        add(Locale.class, "getLocale");
        add(Log.class, "getLog");
        add(String.class, "getCompleteId");
    }

    private void add(Class fieldType, String methodName)
    {
        _configuration.put(fieldType.getName(), methodName);
    }

    public boolean provideInjection(String fieldName, String fieldType, ObjectLocator locator,
            ClassTransformation transformation, MutableComponentModel componentModel)
    {
        String implementationMethodName = _configuration.get(fieldType);

        if (implementationMethodName == null)
            return false;

        String resourcesField = transformation.getResourcesFieldName();

        String body = String.format(
                "%s = %s.%s();",
                fieldName,
                resourcesField,
                implementationMethodName);

        transformation.makeReadOnly(fieldName);

        transformation.extendConstructor(body);

        return true;
    }

}
