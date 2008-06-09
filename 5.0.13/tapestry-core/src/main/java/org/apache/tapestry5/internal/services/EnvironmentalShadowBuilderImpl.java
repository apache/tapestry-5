// Copyright 2007 The Apache Software Foundation
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

import org.apache.tapestry5.ioc.services.Builtin;
import org.apache.tapestry5.ioc.services.ClassFab;
import org.apache.tapestry5.ioc.services.ClassFactory;
import org.apache.tapestry5.ioc.services.MethodSignature;
import org.apache.tapestry5.services.Environment;
import org.apache.tapestry5.services.EnvironmentalShadowBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class EnvironmentalShadowBuilderImpl implements EnvironmentalShadowBuilder
{
    private final ClassFactory classFactory;

    private final Environment environment;

    /**
     * Construct using the default builtin factory, not the component layer version.
     */
    public EnvironmentalShadowBuilderImpl(@Builtin ClassFactory classFactory,

                                          Environment environment)
    {
        this.classFactory = classFactory;
        this.environment = environment;
    }

    public <T> T build(Class<T> serviceType)
    {
        // TODO: Check that serviceType is an interface?

        Class proxyClass = buildProxyClass(serviceType);

        try
        {
            Constructor cons = proxyClass.getConstructors()[0];

            Object raw = cons.newInstance(environment, serviceType);

            return serviceType.cast(raw);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private Class buildProxyClass(Class serviceType)
    {
        ClassFab classFab = classFactory.newClass(serviceType);

        classFab.addField("environment", Environment.class);
        classFab.addField("_serviceType", Class.class);

        classFab.addConstructor(new Class[] { Environment.class, Class.class }, null,
                                "{ environment = $1; _serviceType = $2; }");

        classFab.addMethod(Modifier.PRIVATE, new MethodSignature(serviceType, "_delegate", null, null),
                           "return ($r) environment.peekRequired(_serviceType); ");

        classFab.proxyMethodsToDelegate(serviceType, "_delegate()",
                                        String.format("<EnvironmentalProxy for %s>", serviceType.getName()));

        return classFab.createClass();
    }

}
