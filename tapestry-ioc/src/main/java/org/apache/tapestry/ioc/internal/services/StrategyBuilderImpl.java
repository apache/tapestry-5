// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.ioc.internal.services;

import java.lang.reflect.Modifier;

import org.apache.tapestry.ioc.services.ClassFab;
import org.apache.tapestry.ioc.services.ClassFactory;
import org.apache.tapestry.ioc.services.MethodIterator;
import org.apache.tapestry.ioc.services.MethodSignature;
import org.apache.tapestry.ioc.services.StrategyBuilder;
import org.apache.tapestry.ioc.util.BodyBuilder;
import org.apache.tapestry.ioc.util.StrategyRegistry;

public class StrategyBuilderImpl implements StrategyBuilder
{
    private final ClassFactory _classFactory;

    public StrategyBuilderImpl(ClassFactory classFactory)
    {
        _classFactory = classFactory;
    }

    public <S> S build(StrategyRegistry<S> registry)
    {
        Class<S> interfaceClass = registry.getAdapterType();

        // TODO: Could cache the implClass by interfaceClass ... 
        
        Class implClass = createImplClass(interfaceClass);

        try
        {
            Object raw = implClass.getConstructors()[0].newInstance(registry);

            return interfaceClass.cast(raw);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private Class createImplClass(Class interfaceClass)
    {
        ClassFab cf = _classFactory.newClass(interfaceClass);

        String interfaceClassName = interfaceClass.getName();

        cf.addField("_registry", StrategyRegistry.class);
        cf.addConstructor(new Class[]
        { StrategyRegistry.class }, null, "_registry = $1;");

        BodyBuilder builder = new BodyBuilder();

        MethodIterator mi = new MethodIterator(interfaceClass);

        while (mi.hasNext())
        {
            MethodSignature sig = mi.next();

            // TODO: Checks for methods that don't have at least one parameter, or don't have a
            // compatible 1st parameter.  Currently, we'll get a Javassist exception.

            builder.clear();
            builder.begin();

            builder.addln("Object selector = $1;");
            builder.addln(
                    "%s adapter = (%<s) _registry.getByInstance(selector);",
                    interfaceClassName);
            builder.addln("return ($r) adapter.%s($$);", sig.getName());

            builder.end();

            cf.addMethod(Modifier.PUBLIC, sig, builder.toString());

        }

        if (!mi.getToString())
            cf.addToString(String.format("<Strategy for %s>", interfaceClassName));

        return cf.createClass();
    }
}
