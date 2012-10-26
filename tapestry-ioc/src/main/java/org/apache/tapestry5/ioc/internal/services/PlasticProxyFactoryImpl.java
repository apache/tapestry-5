// Copyright 2011, 2012 The Apache Software Foundation
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

package org.apache.tapestry5.ioc.internal.services;

import org.apache.tapestry5.internal.plastic.PlasticInternalUtils;
import org.apache.tapestry5.internal.plastic.asm.Type;
import org.apache.tapestry5.internal.plastic.asm.tree.*;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.ioc.ObjectCreator;
import org.apache.tapestry5.ioc.internal.util.CollectionFactory;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.ioc.services.PlasticProxyFactory;
import org.apache.tapestry5.plastic.*;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class PlasticProxyFactoryImpl implements PlasticProxyFactory
{
    private final PlasticManager manager;

    private final Map<String, Location> memberToLocation = CollectionFactory.newConcurrentMap();

    public PlasticProxyFactoryImpl(ClassLoader parentClassLoader, Logger logger)
    {
        this(PlasticManager.withClassLoader(parentClassLoader).create(), logger);
    }

    public PlasticProxyFactoryImpl(PlasticManager manager, Logger logger)
    {
        assert manager != null;

        this.manager = manager;

        if (logger != null)
        {
            manager.addPlasticClassListener(new PlasticClassListenerLogger(logger));
        }
    }

    public ClassLoader getClassLoader()
    {
        return manager.getClassLoader();
    }

    public <T> ClassInstantiator<T> createProxy(Class<T> interfaceType, PlasticClassTransformer callback)
    {
        return manager.createProxy(interfaceType, callback);
    }

    public PlasticClassTransformation createProxyTransformation(Class interfaceType)
    {
        return manager.createProxyTransformation(interfaceType);
    }

    public <T> T createProxy(final Class<T> interfaceType, final ObjectCreator<T> creator, final String description)
    {
        assert creator != null;
        assert InternalUtils.isNonBlank(description);

        ClassInstantiator<T> instantiator = createProxy(interfaceType, new PlasticClassTransformer()
        {
            public void transform(PlasticClass plasticClass)
            {
                final PlasticField objectCreatorField = plasticClass.introduceField(ObjectCreator.class, "creator")
                        .inject(creator);

                PlasticMethod delegateMethod = plasticClass.introducePrivateMethod(interfaceType.getName(), "delegate",
                        null, null);

                delegateMethod.changeImplementation(new InstructionBuilderCallback()
                {
                    public void doBuild(InstructionBuilder builder)
                    {
                        builder.loadThis().getField(objectCreatorField);
                        builder.invoke(ObjectCreator.class, Object.class, "createObject");
                        builder.checkcast(interfaceType).returnResult();
                    }
                });

                for (Method method : interfaceType.getMethods())
                {
                    plasticClass.introduceMethod(method).delegateTo(delegateMethod);
                }

                plasticClass.addToString(description);
            }
        });

        return interfaceType.cast(instantiator.newInstance());
    }

    private ClassNode readClassNode(Class clazz)
    {
        byte[] bytecode = PlasticInternalUtils.readBytecodeForClass(manager.getClassLoader(), clazz.getName(), false);

        return bytecode == null ? null : PlasticInternalUtils.convertBytecodeToClassNode(bytecode);
    }

    public Location getMethodLocation(final Method method)
    {
        ObjectCreator<String> descriptionCreator = new ObjectCreator<String>()
        {
            public String createObject()
            {
                return InternalUtils.asString(method);
            }
        };

        return getMemberLocation(method, method.getName(), Type.getMethodDescriptor(method),
                descriptionCreator);
    }

    public Location getConstructorLocation(final Constructor constructor)
    {
        ObjectCreator<String> descriptionCreator = new ObjectCreator<String>()
        {
            public String createObject()
            {
                StringBuilder builder = new StringBuilder(constructor.getDeclaringClass().getName()).append("(");
                String sep = "";

                for (Class parameterType : constructor.getParameterTypes())
                {
                    builder.append(sep);
                    builder.append(parameterType.getSimpleName());

                    sep = ", ";
                }

                builder.append(")");

                return builder.toString();
            }
        };

        return getMemberLocation(constructor, "<init>", Type.getConstructorDescriptor(constructor),
                descriptionCreator);
    }

    public void clearCache()
    {
        memberToLocation.clear();
    }


    public Location getMemberLocation(Member member, String methodName, String memberTypeDesc, ObjectCreator<String> textDescriptionCreator)
    {
        String className = member.getDeclaringClass().getName();

        String key = className + ":" + methodName + ":" + memberTypeDesc;

        Location location = memberToLocation.get(key);

        if (location == null)
        {
            location = constructMemberLocation(member, methodName, memberTypeDesc, textDescriptionCreator.createObject());

            memberToLocation.put(key, location);
        }

        return location;

    }

    private Location constructMemberLocation(Member member, String methodName, String memberTypeDesc, String textDescription)
    {

        ClassNode classNode = readClassNode(member.getDeclaringClass());

        if (classNode == null)
        {
            throw new RuntimeException(String.format("Unable to read class file for %s (to gather line number information).",
                    textDescription));
        }

        for (MethodNode mn : (List<MethodNode>) classNode.methods)
        {
            if (mn.name.equals(methodName) && mn.desc.equals(memberTypeDesc))
            {
                int lineNumber = findFirstLineNumber(mn.instructions);

                // If debugging info is not available, we may lose the line number data, in which case,
                // just generate the Location from the textDescription.

                if (lineNumber < 1)
                {
                    break;
                }

                String description = String.format("%s (at %s:%d)", textDescription, classNode.sourceFile, lineNumber);

                return new StringLocation(description, lineNumber);
            }
        }

        // Didn't find it. Odd.

        return new StringLocation(textDescription, 0);
    }

    private int findFirstLineNumber(InsnList instructions)
    {
        for (AbstractInsnNode node = instructions.getFirst(); node != null; node = node.getNext())
        {
            if (node instanceof LineNumberNode)
            {
                return ((LineNumberNode) node).line;
            }
        }

        return -1;
    }

    public void addPlasticClassListener(PlasticClassListener listener)
    {
        manager.addPlasticClassListener(listener);
    }

    public void removePlasticClassListener(PlasticClassListener listener)
    {
        manager.removePlasticClassListener(listener);
    }

}
