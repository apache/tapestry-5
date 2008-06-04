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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.ioc.services.*;

import static java.lang.String.format;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class PropertyShadowBuilderImpl implements PropertyShadowBuilder
{
    private final ClassFactory classFactory;

    private final PropertyAccess propertyAccess;

    public PropertyShadowBuilderImpl(@Builtin
    ClassFactory classFactory,

                                     PropertyAccess propertyAccess)
    {
        this.classFactory = classFactory;
        this.propertyAccess = propertyAccess;
    }

    public <T> T build(Object source, String propertyName, Class<T> propertyType)
    {
        Class sourceClass = source.getClass();
        PropertyAdapter adapter = propertyAccess.getAdapter(sourceClass).getPropertyAdapter(
                propertyName);

        // TODO: Perhaps extend ClassPropertyAdapter to do these checks?

        if (adapter == null)
            throw new RuntimeException(ServiceMessages.noSuchProperty(sourceClass, propertyName));

        if (!adapter.isRead())
            throw new RuntimeException(ServiceMessages.readNotSupported(source, propertyName));

        if (!propertyType.isAssignableFrom(adapter.getType()))
            throw new RuntimeException(ServiceMessages.propertyTypeMismatch(
                    propertyName,
                    sourceClass,
                    adapter.getType(),
                    propertyType));

        ClassFab cf = classFactory.newClass(propertyType);

        cf.addField("_source", Modifier.PRIVATE | Modifier.FINAL, sourceClass);

        cf.addConstructor(new Class[]
                { sourceClass }, null, "_source = $1;");

        String body = format("return _source.%s();", adapter.getReadMethod().getName());

        MethodSignature sig = new MethodSignature(propertyType, "_delegate", null, null);
        cf.addMethod(Modifier.PRIVATE, sig, body);

        String toString = format("<Shadow: property %s of %s>", propertyName, source);

        cf.proxyMethodsToDelegate(propertyType, "_delegate()", toString);

        Class shadowClass = cf.createClass();

        try
        {
            Constructor cc = shadowClass.getConstructors()[0];

            Object instance = cc.newInstance(source);

            return propertyType.cast(instance);
        }
        catch (Exception ex)
        {
            // Should not be reachable
            throw new RuntimeException(ex);
        }

    }

}
