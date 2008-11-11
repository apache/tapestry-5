// Copyright 2006, 2007, 2008 The Apache Software Foundation
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

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newConcurrentMap;
import org.apache.tapestry5.ioc.services.*;
import org.apache.tapestry5.ioc.util.BodyBuilder;

import static java.lang.String.format;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

public class ChainBuilderImpl implements ChainBuilder
{
    private final ClassFactory classFactory;

    /**
     * Map, keyed on service interface, of implementation Class.
     */

    private final Map<Class, Class> cache = newConcurrentMap();

    public ChainBuilderImpl(@Builtin ClassFactory classFactory)
    {
        this.classFactory = classFactory;
    }

    @SuppressWarnings("unchecked")
    public <T> T build(Class<T> commandInterface, List<T> commands)
    {
        Class<T> chainClass = findImplementationClass(commandInterface);

        return createInstance(chainClass, commands);
    }

    private Class findImplementationClass(Class commandInterface)
    {
        Class result = cache.get(commandInterface);

        if (result == null)
        {
            result = constructImplementationClass(commandInterface);
            cache.put(commandInterface, result);
        }

        return result;
    }

    private Class constructImplementationClass(Class commandInterface)
    {
        // In rare, rare cases, a race condition to create an implementation class
        // for the same interface may occur. We just let that happen, and there'll
        // be two different classes corresponding to the same interface.

        String name = ClassFabUtils.generateClassName(commandInterface);

        ClassFab cf = classFactory.newClass(name, Object.class);

        addInfrastructure(cf, commandInterface);

        addMethods(cf, commandInterface);

        return cf.createClass();
    }

    private void addInfrastructure(ClassFab cf, Class commandInterface)
    {
        // Array types are very, very tricky to deal with.
        // Also, generics don't help (<T> new T[]) is still java.lang.Object[].

        String arrayClassName = commandInterface.getCanonicalName() + "[]";
        String jvmName = ClassFabUtils.toJVMBinaryName(arrayClassName);

        Class array;

        try
        {
            ClassLoader loader = commandInterface.getClass().getClassLoader();
            if (loader == null) loader = Thread.currentThread().getContextClassLoader();

            array = Class.forName(jvmName, true, loader);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }

        cf.addInterface(commandInterface);
        cf.addField("_commands", Modifier.PRIVATE | Modifier.FINAL, array);

        BodyBuilder builder = new BodyBuilder();
        builder.addln("_commands = (%s[]) $1.toArray(new %<s[0]);", commandInterface.getName());

        cf.addConstructor(new Class[] { List.class }, null, builder.toString());
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(Class<T> instanceClass, List<T> commands)
    {
        try
        {
            Constructor<T> ctor = (Constructor<T>) instanceClass.getConstructors()[0];

            return instanceClass.cast(ctor.newInstance(commands));
        }
        catch (Exception ex)
        {
            // This should not be reachable!
            throw new RuntimeException(ex);
        }

    }

    private void addMethods(ClassFab cf, Class commandInterface)
    {
        MethodIterator mi = new MethodIterator(commandInterface);

        while (mi.hasNext())
        {
            MethodSignature sig = mi.next();

            addMethod(cf, commandInterface, sig);
        }

        if (!mi.getToString()) cf.addToString(format("<Command chain of %s>", commandInterface.getName()));
    }

    private void addMethod(ClassFab cf, Class commandInterface, MethodSignature sig)
    {
        Class returnType = sig.getReturnType();

        if (returnType.equals(void.class))
        {
            addVoidMethod(cf, commandInterface, sig);
            return;
        }

        String defaultValue = defaultForReturnType(returnType);

        BodyBuilder builder = new BodyBuilder();
        builder.begin();

        builder.addln("%s result = %s;", ClassFabUtils.toJavaClassName(returnType), defaultValue);
        builder.addln("for (int i = 0; i < _commands.length; i++)");

        builder.begin();
        builder.addln("result = _commands[i].%s($$);", sig.getName());

        builder.addln("if (result != %s) break;", defaultValue);

        builder.end();

        builder.addln("return result;");
        builder.end();

        cf.addMethod(Modifier.PUBLIC, sig, builder.toString());
    }

    private String defaultForReturnType(Class returnType)
    {
        // For all object and array types.

        if (!returnType.isPrimitive()) return "null";

        if (returnType.equals(boolean.class)) return "false";

        // Assume, then, that it is a numeric type (this method
        // isn't called for type void). Javassist seems to be
        // able to handle 0 for all numeric types.

        return "0";
    }

    private void addVoidMethod(ClassFab cf, Class commandInterface, MethodSignature sig)
    {
        BodyBuilder builder = new BodyBuilder();

        builder.begin();

        builder.addln("for (int i = 0; i < _commands.length; i++)");
        builder.addln("  _commands[i].%s($$);", sig.getName());

        builder.end();

        cf.addMethod(Modifier.PUBLIC, sig, builder.toString());
    }

}
