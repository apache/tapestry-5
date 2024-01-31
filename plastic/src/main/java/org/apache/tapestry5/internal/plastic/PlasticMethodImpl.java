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
import org.apache.tapestry5.internal.plastic.asm.tree.MethodNode;
import org.apache.tapestry5.plastic.*;

import java.lang.reflect.Modifier;
import java.util.List;

class PlasticMethodImpl extends PlasticMember implements PlasticMethod, Comparable<PlasticMethodImpl>
{
    private final MethodNode node;

    private MethodDescription description;

    private MethodHandleImpl handle;

    private MethodAdviceManager adviceManager;

    private List<MethodParameter> parameters;

    private int methodIndex = -1;

    // Lazily initialized
    private String methodIdentifier;

    public PlasticMethodImpl(PlasticClassImpl plasticClass, MethodNode node)
    {
        super(plasticClass, node.visibleAnnotations);

        this.node = node;
        this.description = PlasticInternalUtils.toMethodDescription(node);
    }

    @Override
    public String toString()
    {
        return String.format("PlasticMethod[%s in class %s]", description, plasticClass.className);
    }

    @Override
    public PlasticClass getPlasticClass()
    {
        plasticClass.check();

        return plasticClass;
    }

    @Override
    public MethodDescription getDescription()
    {
        plasticClass.check();

        return description;
    }

    @Override
    public int compareTo(PlasticMethodImpl o)
    {
        plasticClass.check();

        return description.compareTo(o.description);
    }

    @Override
    public boolean isOverride()
    {
        plasticClass.check();

        return plasticClass.inheritanceData.isOverride(node.name, node.desc);
    }

    @Override
    public boolean isAbstract()
    {
        return Modifier.isAbstract(node.access);
    }

    @Override
    public String getMethodIdentifier()
    {
        plasticClass.check();

        if (methodIdentifier == null)
        {
            methodIdentifier = String.format("%s.%s",
                    plasticClass.className,
                    description.toShortString());
        }

        return methodIdentifier;
    }

    @Override
    public boolean isVoid()
    {
        plasticClass.check();

        return description.returnType.equals("void");
    }

    @Override
    public MethodHandle getHandle()
    {
        plasticClass.check();

        if (handle == null)
        {
            methodIndex = plasticClass.nextMethodIndex++;
            handle = new MethodHandleImpl(plasticClass.className, description.toString(), methodIndex);
            plasticClass.shimMethods.add(this);
        }

        return handle;
    }

    @Override
    public PlasticMethod changeImplementation(InstructionBuilderCallback callback)
    {
        plasticClass.check();

        // If the method is currently abstract, clear that flag.
        if (Modifier.isAbstract(node.access))
        {
            node.access = node.access & ~org.apache.tapestry5.internal.plastic.asm.Opcodes.ACC_ABSTRACT;
            description = description.withModifiers(node.access);
        }

        node.instructions.clear();

        plasticClass.newBuilder(description, node).doCallback(callback);

        // With the implementation changed, it is necessary to intercept field reads/writes.
        // The node may not already have been in the fieldTransformMethods Set if it was
        // an introduced method.

        plasticClass.fieldTransformMethods.add(node);

        return this;
    }

    @Override
    public PlasticMethod addAdvice(MethodAdvice advice)
    {
        plasticClass.check();

        assert advice != null;

        if (adviceManager == null)
        {
            adviceManager = new MethodAdviceManager(plasticClass, description, node);
            plasticClass.advisedMethods.add(this);
        }

        adviceManager.add(advice);

        return this;
    }

    @Override
    public PlasticMethod delegateTo(final PlasticField field)
    {
        plasticClass.check();

        assert field != null;
        assert field.getPlasticClass() == plasticClass;

        // TODO: Better handling error case where delegating to a primitive or object array.

        // TODO: Is there a easy way to ensure that the type has the necessary method? I don't
        // like that errors along those lines may be deferred until execution time.

        changeImplementation(new InstructionBuilderCallback()
        {
            @Override
            public void doBuild(InstructionBuilder builder)
            {
                // Load the field

                builder.loadThis().getField(field);
                builder.loadArguments();

                invokeDelegateAndReturnResult(builder, field.getTypeName());
            }
        });

        return this;
    }

    @Override
    public PlasticMethod delegateTo(PlasticMethod delegateProvidingMethod)
    {
        plasticClass.check();

        assert delegateProvidingMethod != null;
        assert delegateProvidingMethod.getPlasticClass() == plasticClass;

        // TODO: ensure not primitive/array type
        final MethodDescription providerDescriptor = delegateProvidingMethod.getDescription();
        final String delegateType = providerDescriptor.returnType;

        if (delegateType.equals("void") || providerDescriptor.argumentTypes.length > 0)
            throw new IllegalArgumentException(
                    String.format(
                            "Method %s is not usable as a delegate provider; it must be a void method that takes no arguments.",
                            delegateProvidingMethod));

        changeImplementation(new InstructionBuilderCallback()
        {
            @Override
            public void doBuild(InstructionBuilder builder)
            {
                // Load the field

                builder.loadThis();

                if (Modifier.isPrivate(providerDescriptor.modifiers))
                {
                    builder.invokeSpecial(plasticClass.className, providerDescriptor);
                } else
                {
                    builder.invokeVirtual(plasticClass.className, delegateType, providerDescriptor.methodName);
                }

                builder.loadArguments();

                invokeDelegateAndReturnResult(builder, delegateType);
            }
        });

        return this;
    }

    @Override
    public List<MethodParameter> getParameters()
    {
        if (parameters == null)
        {
            parameters = PlasticInternalUtils.newList();

            for (int i = 0; i < description.argumentTypes.length; i++)
            {

                parameters.add(new MethodParameterImpl(plasticClass,
                        PlasticClassImpl.safeArrayDeref(node.visibleParameterAnnotations, i),
                        PlasticClassImpl.safeArrayDeref(description.argumentTypes, i), i));
            }
        }

        return parameters;
    }

    void rewriteMethodForAdvice()
    {
        adviceManager.rewriteOriginalMethod();
    }

    private boolean isPrivate()
    {
        return Modifier.isPrivate(node.access);
    }

    /**
     * If a MethodHandle has been requested and the method itself is private, then create a non-private
     * access method. Returns the access method name (which is different from the method name itself only
     * for private methods, where an access method is created).
     */
    private String setupMethodHandleAccess()
    {
        if (isPrivate())
            return createAccessMethod();
        else
            return node.name;
    }

    private String createAccessMethod()
    {
        String name = String.format("%s$access%s", node.name, PlasticUtils.nextUID());

        // Kind of awkward that exceptions are specified as String[] when what we have handy is List<String>
        MethodNode mn = new MethodNode(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_FINAL, name, node.desc, node.signature, null);
        // But it is safe enough for the two nodes to share
        mn.exceptions = node.exceptions;

        InstructionBuilder builder = plasticClass.newBuilder(mn);

        builder.loadThis();
        builder.loadArguments();
        builder.invokeSpecial(plasticClass.className, description);
        builder.returnResult();

        plasticClass.addMethod(mn);

        return name;
    }

    void installShim(PlasticClassHandleShim shim)
    {
        handle.shim = shim;
    }

    /**
     * The properly cast target instance will be on the stack.
     */
    void extendShimInvoke(SwitchBlock block)
    {
        final String accessMethodName = setupMethodHandleAccess();

        block.addCase(methodIndex, false, new InstructionBuilderCallback()
        {
            @Override
            public void doBuild(InstructionBuilder builder)
            {
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
                                // The third argument is an Object array; get each
                                for (int i = 0; i < description.argumentTypes.length; i++)
                                {
                                    String argumentType = description.argumentTypes[i];

                                    builder.loadArgument(2);
                                    builder.loadArrayElement(i, Object.class.getName());
                                    builder.castOrUnbox(argumentType);
                                }

                                builder.invokeVirtual(plasticClass.className, description.returnType, accessMethodName,
                                        description.argumentTypes);

                                // TODO: hate see "void" just there.

                                if (description.returnType.equals("void"))
                                    builder.loadNull();
                                else
                                    builder.boxPrimitive(description.returnType);

                                builder.newInstance(SuccessMethodInvocationResult.class).dupe(1).swap();
                                builder.invokeConstructor(SuccessMethodInvocationResult.class, Object.class);
                                builder.returnResult();
                            }
                        });

                        for (String exceptionType : description.checkedExceptionTypes)
                        {
                            block.addCatch(exceptionType, new InstructionBuilderCallback()
                            {
                                @Override
                                public void doBuild(InstructionBuilder builder)
                                {
                                    builder.newInstance(FailureMethodInvocationResult.class).dupe(1).swap();
                                    builder.invokeConstructor(FailureMethodInvocationResult.class, Throwable.class);
                                    builder.returnResult();
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private void invokeDelegateAndReturnResult(InstructionBuilder builder, String delegateType)
    {
        // The trick is that you have to be careful to use the right opcode based on the field type
        // (interface vs. ordinary object).

        final TypeCategory typeCategory = plasticClass.pool.getTypeCategory(delegateType);

        if (typeCategory == TypeCategory.INTERFACE)
            builder.invokeInterface(delegateType, description.returnType, description.methodName,
                    description.argumentTypes);
        else
            builder.invokeVirtual(delegateType, description.returnType, description.methodName,
                    description.argumentTypes);

        builder.returnResult();
    }

}
