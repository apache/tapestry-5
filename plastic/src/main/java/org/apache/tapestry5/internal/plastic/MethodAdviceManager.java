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

package org.apache.tapestry5.internal.plastic;

import org.apache.tapestry5.internal.plastic.asm.Opcodes;
import org.apache.tapestry5.internal.plastic.asm.tree.ClassNode;
import org.apache.tapestry5.internal.plastic.asm.tree.MethodNode;
import org.apache.tapestry5.plastic.*;

import java.util.List;

/**
 * Responsible for tracking the advice added to a method, as well as creating the MethodInvocation
 * class for the method and ultimately rewriting the original method to instantiate the MethodInvocation
 * and handle the success or failure result.
 */
class MethodAdviceManager
{
    private final static String RETURN_VALUE = "returnValue";

    private final MethodDescription description;

    /**
     * The method to which advice is added; it must be converted to instantiate the
     * MethodInvocation subclass, then unpack the return value and/or re-throw checked exceptions.
     */
    private final MethodNode advisedMethodNode;

    private final ClassNode invocationClassNode;

    private final List<MethodAdvice> advice = PlasticInternalUtils.newList();

    private final boolean isVoid;

    private final String invocationClassName;

    /**
     * The new method that uses the original instructions from the advisedMethodNode.
     */
    private final String newMethodName;

    private final String[] constructorTypes;
    private PlasticClassImpl plasticClass;

    MethodAdviceManager(PlasticClassImpl plasticClass, MethodDescription description, MethodNode methodNode)
    {
        this.plasticClass = plasticClass;
        this.description = description;
        this.advisedMethodNode = methodNode;

        isVoid = description.returnType.equals("void");

        invocationClassName = String.format("%s$Invocation_%s_%s", plasticClass.className, description.methodName,
                PlasticUtils.nextUID());

        invocationClassNode = new ClassNode();

        invocationClassNode.visit(PlasticConstants.DEFAULT_VERSION_OPCODE, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, plasticClass.nameCache.toInternalName(invocationClassName),
                null, PlasticClassImpl.ABSTRACT_METHOD_INVOCATION_INTERNAL_NAME, new String[]
                        {plasticClass.nameCache.toInternalName(MethodInvocation.class)});

        constructorTypes = createFieldsAndConstructor();

        createReturnValueAccessors();

        createSetParameter();

        createGetParameter();

        newMethodName = String.format("advised$%s_%s", description.methodName, PlasticUtils.nextUID());

        createProceedToAdvisedMethod();
    }

    private String[] createFieldsAndConstructor()
    {
        if (!isVoid)
            invocationClassNode.visitField(Opcodes.ACC_PUBLIC, RETURN_VALUE, plasticClass.nameCache.toDesc(description.returnType),
                    null, null);

        List<String> consTypes = PlasticInternalUtils.newList();
        consTypes.add(Object.class.getName());
        consTypes.add(InstanceContext.class.getName());
        consTypes.add(MethodInvocationBundle.class.getName());

        for (int i = 0; i < description.argumentTypes.length; i++)
        {
            String type = description.argumentTypes[i];

            invocationClassNode.visitField(Opcodes.ACC_PRIVATE, "p" + i, plasticClass.nameCache.toDesc(type), null, null);

            consTypes.add(type);
        }

        String[] constructorTypes = consTypes.toArray(new String[consTypes.size()]);

        MethodNode cons = new MethodNode(Opcodes.ACC_PUBLIC, PlasticClassImpl.CONSTRUCTOR_NAME, plasticClass.nameCache.toMethodDescriptor("void",
                constructorTypes), null, null);

        InstructionBuilder builder = plasticClass.newBuilder(cons);

        // First three arguments go to the super-class

        builder.loadThis();
        builder.loadArgument(0);
        builder.loadArgument(1);
        builder.loadArgument(2);
        builder.invokeConstructor(AbstractMethodInvocation.class, Object.class, InstanceContext.class,
                MethodInvocationBundle.class);

        for (int i = 0; i < description.argumentTypes.length; i++)
        {
            String name = "p" + i;
            String type = description.argumentTypes[i];

            builder.loadThis();
            builder.loadArgument(3 + i);
            builder.putField(invocationClassName, name, type);
        }

        builder.returnResult();

        invocationClassNode.methods.add(cons);

        return constructorTypes;
    }

    public void add(MethodAdvice advice)
    {
        this.advice.add(advice);
    }

    private void createReturnValueAccessors()
    {
        addReturnValueSetter();
        createReturnValueGetter();
    }

    private InstructionBuilder newMethod(String name, Class returnType, Class... argumentTypes)
    {
        MethodNode mn = new MethodNode(Opcodes.ACC_PUBLIC, name, plasticClass.nameCache.toMethodDescriptor(returnType, argumentTypes),
                null, null);

        invocationClassNode.methods.add(mn);

        return plasticClass.newBuilder(mn);
    }

    private void createReturnValueGetter()
    {
        InstructionBuilder builder = newMethod("getReturnValue", Object.class);

        if (isVoid)
        {
            builder.loadNull().returnResult();
        } else
        {
            builder.loadThis().getField(invocationClassName, RETURN_VALUE, description.returnType)
                    .boxPrimitive(description.returnType).returnResult();
        }
    }

    private void addReturnValueSetter()
    {
        InstructionBuilder builder = newMethod("setReturnValue", MethodInvocation.class, Object.class);

        if (isVoid)
        {
            builder.throwException(IllegalArgumentException.class, String
                    .format("Method %s of class %s is void, setting a return value is not allowed.", description,
                            plasticClass.className));
        } else
        {
            builder.loadThis().loadArgument(0);
            builder.castOrUnbox(description.returnType);
            builder.putField(invocationClassName, RETURN_VALUE, description.returnType);

            builder.loadThis().invoke(AbstractMethodInvocation.class, void.class, "clearCheckedException");

            builder.loadThis().returnResult();
        }
    }

    private void createGetParameter()
    {
        InstructionBuilder builder = newMethod("getParameter", Object.class, int.class);

        if (description.argumentTypes.length == 0)
        {
            indexOutOfRange(builder);
        } else
        {
            builder.loadArgument(0);
            builder.startSwitch(0, description.argumentTypes.length - 1, new SwitchCallback()
            {

                @Override
                public void doSwitch(SwitchBlock block)
                {
                    for (int i = 0; i < description.argumentTypes.length; i++)
                    {
                        final int index = i;

                        block.addCase(i, false, new InstructionBuilderCallback()
                        {

                            @Override
                            public void doBuild(InstructionBuilder builder)
                            {
                                String type = description.argumentTypes[index];

                                builder.loadThis();
                                builder.getField(invocationClassName, "p" + index, type).boxPrimitive(type)
                                        .returnResult();
                            }
                        });
                    }
                }
            });
        }
    }

    private void indexOutOfRange(InstructionBuilder builder)
    {
        builder.throwException(IllegalArgumentException.class, "Parameter index out of range.");
    }

    private void createSetParameter()
    {
        InstructionBuilder builder = newMethod("setParameter", MethodInvocation.class, int.class, Object.class);

        if (description.argumentTypes.length == 0)
        {
            indexOutOfRange(builder);
        } else
        {
            builder.loadArgument(0).startSwitch(0, description.argumentTypes.length - 1, new SwitchCallback()
            {

                @Override
                public void doSwitch(SwitchBlock block)
                {
                    for (int i = 0; i < description.argumentTypes.length; i++)
                    {
                        final int index = i;

                        block.addCase(i, true, new InstructionBuilderCallback()
                        {

                            @Override
                            public void doBuild(InstructionBuilder builder)
                            {
                                String type = description.argumentTypes[index];

                                builder.loadThis();
                                builder.loadArgument(1).castOrUnbox(type);
                                builder.putField(invocationClassName, "p" + index, type);
                            }
                        });
                    }
                }
            });

            builder.loadThis().returnResult();
        }
    }

    private void createNewMethod()
    {
        String[] exceptions = advisedMethodNode.exceptions == null ? null
                : advisedMethodNode.exceptions.toArray(new String[0]);

        // Remove the private flag, so that the MethodInvocation implementation (in the same package)
        // can directly access the method without an additional access method.

        MethodNode mn = new MethodNode(advisedMethodNode.access & ~Opcodes.ACC_PRIVATE, newMethodName,
                advisedMethodNode.desc, advisedMethodNode.signature, exceptions);

        // Copy everything else about the advisedMethodNode over to the new node

        advisedMethodNode.accept(mn);

        // Add this new method, with the same implementation as the original method, to the
        // PlasticClass

        plasticClass.classNode.methods.add(mn);
    }

    /**
     * Invoke the "new" method, and deal with the return value and/or thrown exceptions.
     */
    private void createProceedToAdvisedMethod()
    {
        InstructionBuilder builder = newMethod("proceedToAdvisedMethod", void.class);

        if (!isVoid)
            builder.loadThis();

        builder.loadThis().invoke(AbstractMethodInvocation.class, Object.class, "getInstance").checkcast(plasticClass.className);

        // Load up each parameter
        for (int i = 0; i < description.argumentTypes.length; i++)
        {
            String type = description.argumentTypes[i];

            builder.loadThis().getField(invocationClassName, "p" + i, type);
        }

        builder.startTryCatch(new TryCatchCallback()
        {
            @Override
            public void doBlock(TryCatchBlock block)
            {
                block.addTry(new InstructionBuilderCallback()
                {

                    @Override
                    public void doBuild(InstructionBuilder builder)
                    {
                        builder.invokeVirtual(plasticClass.className, description.returnType, newMethodName,
                                description.argumentTypes);

                        if (!isVoid)
                            builder.putField(invocationClassName, RETURN_VALUE, description.returnType);

                        builder.returnResult();
                    }
                });

                for (String exceptionName : description.checkedExceptionTypes)
                {
                    if (plasticClass.pool.isCheckedException(exceptionName))
                    {
                        block.addCatch(exceptionName, new InstructionBuilderCallback()
                        {
                            @Override
                            public void doBuild(InstructionBuilder builder)
                            {
                                builder.loadThis().swap();
                                builder.invoke(AbstractMethodInvocation.class, MethodInvocation.class,
                                        "setCheckedException", Exception.class);

                                builder.returnResult();
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Creates a new method containing the advised method's original implementation, then rewrites the
     * advised method to create the MethodInvocation subclass, invoke proceed() on it, and handle
     * the return value and/or checked exceptions.
     */
    void rewriteOriginalMethod()
    {
        createNewMethod();

        plasticClass.pool.realize(plasticClass.className, ClassType.METHOD_INVOCATION, invocationClassNode);

        String fieldName = String.format("methodinvocationbundle_%s_%s", description.methodName,
                PlasticUtils.nextUID());

        MethodAdvice[] adviceArray = advice.toArray(new MethodAdvice[advice.size()]);
        MethodInvocationBundle bundle = new MethodInvocationBundle(plasticClass.className, description, adviceArray);

        plasticClass.classNode.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, fieldName, plasticClass.nameCache.toDesc(constructorTypes[2]), null, null);
        plasticClass.initializeFieldFromStaticContext(fieldName, constructorTypes[2], bundle);

        // Ok, here's the easy part: replace the method invocation with instantiating the invocation class

        advisedMethodNode.instructions.clear();
        advisedMethodNode.tryCatchBlocks.clear();

        if (advisedMethodNode.localVariables != null)
        {
            advisedMethodNode.localVariables.clear();
        }

        InstructionBuilder builder = plasticClass.newBuilder(description, advisedMethodNode);

        builder.newInstance(invocationClassName).dupe();

        // Now load up the parameters to the constructor

        builder.loadThis();
        builder.loadThis().getField(plasticClass.className, plasticClass.getInstanceContextFieldName(), constructorTypes[1]);
        builder.loadThis().getField(plasticClass.className, fieldName, constructorTypes[2]);

        // Load up the actual method parameters

        builder.loadArguments();
        builder.invokeConstructor(invocationClassName, constructorTypes);

        // That leaves an instance of the invocation class on the stack. If the method is void
        // and throws no checked exceptions, then the variable actually isn't used. This code
        // should be refactored a bit once there are tests for those cases.

        builder.startVariable(invocationClassName, new LocalVariableCallback()
        {
            @Override
            public void doBuild(final LocalVariable invocation, InstructionBuilder builder)
            {
                builder.dupe().storeVariable(invocation);

                builder.invoke(AbstractMethodInvocation.class, MethodInvocation.class, "proceed");

                if (description.checkedExceptionTypes.length > 0)
                {
                    builder.invoke(MethodInvocation.class, boolean.class, "didThrowCheckedException");

                    builder.when(Condition.NON_ZERO, new InstructionBuilderCallback()
                    {
                        @Override
                        public void doBuild(InstructionBuilder builder)
                        {
                            builder.loadVariable(invocation).loadTypeConstant(Exception.class);
                            builder.invokeVirtual(invocationClassName, Throwable.class.getName(),
                                    "getCheckedException", Class.class.getName());
                            builder.throwException();
                        }
                    });
                }

                if (!isVoid)
                {
                    builder.loadVariable(invocation).getField(invocationClassName, RETURN_VALUE,
                            description.returnType);
                }

                builder.returnResult();
            }
        });
    }
}
