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

import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.ObjectLocator;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.model.MutableComponentModel;
import org.apache.tapestry5.services.ClassTransformation;
import org.apache.tapestry5.services.InjectionProvider;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.Map;

/**
 * Allows for a number of anonymous injections based on the type of field that is to be injected.
 */
public class CommonResourcesInjectionProvider implements InjectionProvider
{
    private static final Map<Class, String> configuration = CollectionFactory.newMap();

    {
        configuration.put(Messages.class, "getMessages");
        configuration.put(Locale.class, "getLocale");
        configuration.put(Logger.class, "getLogger");
        configuration.put(String.class, "getCompleteId");
    }

    public boolean provideInjection(String fieldName, Class fieldType, ObjectLocator locator,
                                    ClassTransformation transformation, MutableComponentModel componentModel)
    {
        String implementationMethodName = configuration.get(fieldType);

        if (implementationMethodName == null) return false;

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
