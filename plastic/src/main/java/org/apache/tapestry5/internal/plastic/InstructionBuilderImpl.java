// Copyright 2011 The Apache Software Foundation
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

import org.apache.tapestry5.internal.plastic.InstructionBuilderState.LVInfo;
import org.apache.tapestry5.internal.plastic.asm.Label;
import org.apache.tapestry5.internal.plastic.asm.MethodVisitor;
import org.apache.tapestry5.internal.plastic.asm.Opcodes;
import org.apache.tapestry5.internal.plastic.asm.Type;
import org.apache.tapestry5.plastic.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class InstructionBuilderImpl extends Lockable implements Opcodes, InstructionBuilder
{
    private static final int[] DUPE_OPCODES = new int[]
            {DUP, DUP_X1, DUP_X2};

    /**
     * Maps from condition to opcode to jump to the false code block.
     */
    private static final Map<Condition, Integer> conditionToOpcode = new HashMap<>();

    static
    {
        Map<Condition, Integer> m = conditionToOpcode;

        m.put(Condition.NULL, IFNONNULL);
        m.put(Condition.NON_NULL, IFNULL);
        m.put(Condition.ZERO, IFNE);
        m.put(Condition.NON_ZERO, IFEQ);
        m.put(Condition.EQUAL, IF_ICMPNE);
        m.put(Condition.NOT_EQUAL, IF_ICMPEQ);
        m.put(Condition.LESS_THAN, IF_ICMPGE);
        m.put(Condition.GREATER, IF_ICMPLE);
    }

    private static final Map<String, Integer> typeToSpecialComparisonOpcode = new HashMap<>();

    static
    {
        Map<String, Integer> m = typeToSpecialComparisonOpcode;

        m.put("long", LCMP);
        m.put("float", FCMPL);
        m.put("double", DCMPL);
    }

    private static final Map<Object, Integer> constantOpcodes = new HashMap<>();

    static
    {
        Map<Object, Integer> m = constantOpcodes;

        m.put(Integer.valueOf(-1), ICONST_M1);
        m.put(Integer.valueOf(0), ICONST_0);
        m.put(Integer.valueOf(1), ICONST_1);
        m.put(Integer.valueOf(2), ICONST_2);
        m.put(Integer.valueOf(3), ICONST_3);
        m.put(Integer.valueOf(4), ICONST_4);
        m.put(Integer.valueOf(5), ICONST_5);

        m.put(Long.valueOf(0), LCONST_0);
        m.put(Long.valueOf(1), LCONST_1);

        m.put(Float.valueOf(0), FCONST_0);
        m.put(Float.valueOf(1), FCONST_1);
        m.put(Float.valueOf(2), FCONST_2);

        m.put(Double.valueOf(0), DCONST_0);
        m.put(Double.valueOf(1), DCONST_1);

        m.put(null, ACONST_NULL);
    }

    protected final InstructionBuilderState state;

    protected final MethodVisitor v;

    protected final NameCache cache;

    InstructionBuilderImpl(MethodDescription description, MethodVisitor visitor, NameCache cache)
    {
        InstructionBuilderState state = new InstructionBuilderState(description, visitor, cache);
        this.state = state;

        // These are conveniences for values stored inside the state. In fact,
        // these fields predate the InstructionBuilderState type.

        this.v = state.visitor;
        this.cache = state.nameCache;
    }

    @Override
    public InstructionBuilder returnDefaultValue()
    {
        check();

        PrimitiveType type = PrimitiveType.getByName(state.description.returnType);

        if (type == null)
        {
            v.visitInsn(ACONST_NULL);
            v.visitInsn(ARETURN);
        } else
        {
            switch (type)
            {
                case VOID:
                    break;

                case LONG:
                    v.visitInsn(LCONST_0);
                    break;

                case FLOAT:
                    v.visitInsn(FCONST_0);
                    break;

                case DOUBLE:
                    v.visitInsn(DCONST_0);
                    break;

                default:
                    v.visitInsn(ICONST_0);
                    break;
            }

            v.visitInsn(type.returnOpcode);
        }

        return this;
    }

    @Override
    public InstructionBuilder loadThis()
    {
        check();

        v.visitVarInsn(ALOAD, 0);

        return this;
    }

    @Override
    public InstructionBuilder loadNull()
    {
        check();

        v.visitInsn(ACONST_NULL);

        return this;
    }

    @Override
    public InstructionBuilder loadArgument(int index)
    {
        check();

        PrimitiveType type = PrimitiveType.getByName(state.description.argumentTypes[index]);

        int opcode = type == null ? ALOAD : type.loadOpcode;

        v.visitVarInsn(state.argumentLoadOpcode[index], state.argumentIndex[index]);

        return this;
    }

    @Override
    public InstructionBuilder loadArguments()
    {
        check();

        for (int i = 0; i < state.description.argumentTypes.length; i++)
        {
            loadArgument(i);
        }

        return this;
    }

    @Override
    public InstructionBuilder invokeSpecial(String containingClassName, MethodDescription description)
    {
        check();

        doInvoke(INVOKESPECIAL, containingClassName, description, false);

        return this;
    }

    @Override
    public InstructionBuilder invokeVirtual(PlasticMethod method)
    {
        check();

        assert method != null;

        MethodDescription description = method.getDescription();

        return invokeVirtual(method.getPlasticClass().getClassName(), description.returnType, description.methodName,
                description.argumentTypes);
    }

    @Override
    public InstructionBuilder invokeVirtual(String className, String returnType, String methodName,
                                            String... argumentTypes)
    {
        check();

        doInvoke(INVOKEVIRTUAL, className, returnType, methodName, false, argumentTypes);

        return this;
    }

    @Override
    public InstructionBuilder invokeInterface(String interfaceName, String returnType, String methodName,
                                              String... argumentTypes)
    {
        check();

        doInvoke(INVOKEINTERFACE, interfaceName, returnType, methodName, true, argumentTypes);

        return this;
    }

    private void doInvoke(int opcode, String className, String returnType, String methodName, boolean isInterface,
                          String... argumentTypes)
    {
        v.visitMethodInsn(opcode, cache.toInternalName(className), methodName,
                cache.toMethodDescriptor(returnType, argumentTypes), isInterface);
    }

    @Override
    public InstructionBuilder invokeStatic(Class clazz, Class returnType, String methodName, Class... argumentTypes)
    {
        doInvoke(INVOKESTATIC, clazz, returnType, methodName, false, argumentTypes);

        return this;
    }

    private void doInvoke(int opcode, Class clazz, Class returnType, String methodName, boolean isInterface,
                          Class... argumentTypes)
    {
        doInvoke(opcode, clazz.getName(), cache.toTypeName(returnType), methodName, isInterface,
                PlasticUtils.toTypeNames(argumentTypes));
    }

    @Override
    public InstructionBuilder invoke(Method method)
    {
        check();

        return invoke(method.getDeclaringClass(), method.getReturnType(), method.getName(), method.getParameterTypes());
    }

    @Override
    public InstructionBuilder invoke(Class clazz, Class returnType, String methodName, Class... argumentTypes)
    {
        check();

        doInvoke(clazz.isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL, clazz, returnType, methodName,
                clazz.isInterface(), argumentTypes);

        return this;
    }

    private void doInvoke(int opcode, String containingClassName, MethodDescription description, boolean isInterface)
    {
        v.visitMethodInsn(opcode, cache.toInternalName(containingClassName), description.methodName,
                cache.toDesc(description), isInterface);
    }

    @Override
    public InstructionBuilder returnResult()
    {
        check();

        PrimitiveType type = PrimitiveType.getByName(state.description.returnType);

        int opcode = type == null ? ARETURN : type.returnOpcode;

        v.visitInsn(opcode);

        return this;
    }

    @Override
    public InstructionBuilder boxPrimitive(String typeName)
    {
        check();

        PrimitiveType type = PrimitiveType.getByName(typeName);

        if (type != null && type != PrimitiveType.VOID)
        {
            v.visitMethodInsn(INVOKESTATIC, type.wrapperInternalName, "valueOf", type.valueOfMethodDescriptor,
                    false);
        }

        return this;
    }

    @Override
    public InstructionBuilder unboxPrimitive(String typeName)
    {
        check();

        PrimitiveType type = PrimitiveType.getByName(typeName);

        if (type != null)
        {
            doUnbox(type);
        }

        return this;
    }

    private void doUnbox(PrimitiveType type)
    {
        v.visitMethodInsn(INVOKEVIRTUAL, type.wrapperInternalName, type.toValueMethodName, type.toValueMethodDescriptor,
                false);
    }

    @Override
    public InstructionBuilder getField(String className, String fieldName, String typeName)
    {
        check();

        v.visitFieldInsn(GETFIELD, cache.toInternalName(className), fieldName, cache.toDesc(typeName));

        return this;
    }

    @Override
    public InstructionBuilder getStaticField(String className, String fieldName, String typeName)
    {
        check();

        v.visitFieldInsn(GETSTATIC, cache.toInternalName(className), fieldName, cache.toDesc(typeName));

        return this;
    }

    @Override
    public InstructionBuilder getStaticField(String className, String fieldName, Class fieldType)
    {
        check();

        return getStaticField(className, fieldName, cache.toTypeName(fieldType));
    }

    @Override
    public InstructionBuilder putStaticField(String className, String fieldName, Class fieldType)
    {
        check();

        return putStaticField(className, fieldName, cache.toTypeName(fieldType));
    }

    @Override
    public InstructionBuilder putStaticField(String className, String fieldName, String typeName)
    {
        check();

        v.visitFieldInsn(PUTSTATIC, cache.toInternalName(className), fieldName, cache.toDesc(typeName));

        return this;
    }

    @Override
    public InstructionBuilder getField(PlasticField field)
    {
        check();

        return getField(field.getPlasticClass().getClassName(), field.getName(), field.getTypeName());
    }

    @Override
    public InstructionBuilder putField(String className, String fieldName, String typeName)
    {
        check();

        v.visitFieldInsn(PUTFIELD, cache.toInternalName(className), fieldName, cache.toDesc(typeName));

        return this;
    }

    @Override
    public InstructionBuilder putField(String className, String fieldName, Class fieldType)
    {
        check();

        return putField(className, fieldName, cache.toTypeName(fieldType));
    }

    @Override
    public InstructionBuilder getField(String className, String fieldName, Class fieldType)
    {
        check();

        return getField(className, fieldName, cache.toTypeName(fieldType));
    }

    @Override
    public InstructionBuilder loadArrayElement(int index, String elementType)
    {
        check();

        loadConstant(index);

        PrimitiveType type = PrimitiveType.getByName(elementType);

        if (type == null)
        {
            v.visitInsn(AALOAD);
        } else
        {
            throw new RuntimeException("Access to non-object arrays is not yet supported.");
        }

        return this;
    }

    @Override
    public InstructionBuilder loadArrayElement()
    {
        check();

        v.visitInsn(AALOAD);

        return this;
    }

    @Override
    public InstructionBuilder checkcast(String className)
    {
        check();

        // Found out the hard way that array names are handled differently; you cast to the descriptor, not the internal
        // name.

        String internalName = className.contains("[") ? cache.toDesc(className) : cache.toInternalName(className);

        v.visitTypeInsn(CHECKCAST, internalName);

        return this;
    }

    @Override
    public InstructionBuilder checkcast(Class clazz)
    {
        check();

        return checkcast(cache.toTypeName(clazz));
    }

    @Override
    public InstructionBuilder startTryCatch(TryCatchCallback callback)
    {
        check();

        new TryCatchBlockImpl(this, state).doCallback(callback);

        return this;
    }

    @Override
    public InstructionBuilder newInstance(String className)
    {
        check();

        v.visitTypeInsn(NEW, cache.toInternalName(className));

        return this;
    }

    @Override
    public InstructionBuilder newInstance(Class clazz)
    {
        check();

        return newInstance(clazz.getName());
    }

    @Override
    public InstructionBuilder invokeConstructor(String className, String... argumentTypes)
    {
        check();

        doInvoke(INVOKESPECIAL, className, "void", "<init>", false, argumentTypes);

        return this;
    }

    @Override
    public InstructionBuilder invokeConstructor(Class clazz, Class... argumentTypes)
    {
        check();

        return invokeConstructor(clazz.getName(), PlasticUtils.toTypeNames(argumentTypes));
    }

    @Override
    public InstructionBuilder dupe(int depth)
    {
        check();

        if (depth < 0 || depth >= DUPE_OPCODES.length)
            throw new IllegalArgumentException(String.format(
                    "Dupe depth %d is invalid; values from 0 to %d are allowed.", depth, DUPE_OPCODES.length - 1));

        v.visitInsn(DUPE_OPCODES[depth]);

        return this;
    }

    @Override
    public InstructionBuilder dupe()
    {
        check();

        v.visitInsn(DUP);

        return this;
    }

    @Override
    public InstructionBuilder pop()
    {
        check();

        v.visitInsn(POP);

        return this;
    }

    @Override
    public InstructionBuilder swap()
    {
        check();

        v.visitInsn(SWAP);

        return this;
    }

    @Override
    public InstructionBuilder loadConstant(Object constant)
    {
        check();

        Integer opcode = constantOpcodes.get(constant);

        if (opcode != null)
            v.visitInsn(opcode);
        else
            v.visitLdcInsn(constant);

        return this;
    }

    @Override
    public InstructionBuilder loadTypeConstant(String typeName)
    {
        check();

        Type type = Type.getType(cache.toDesc(typeName));

        v.visitLdcInsn(type);

        return this;
    }

    @Override
    public InstructionBuilder loadTypeConstant(Class clazz)
    {
        check();

        Type type = Type.getType(clazz);

        v.visitLdcInsn(type);

        return this;
    }

    @Override
    public InstructionBuilder castOrUnbox(String typeName)
    {
        check();

        PrimitiveType type = PrimitiveType.getByName(typeName);

        if (type == null)
            return checkcast(typeName);

        v.visitTypeInsn(CHECKCAST, type.wrapperInternalName);
        doUnbox(type);

        return this;
    }

    @Override
    public InstructionBuilder throwException(String className, String message)
    {
        check();

        newInstance(className).dupe().loadConstant(message);

        invokeConstructor(className, "java.lang.String");

        v.visitInsn(ATHROW);

        return this;
    }

    @Override
    public InstructionBuilder throwException(Class<? extends Throwable> exceptionType, String message)
    {
        check();

        return throwException(cache.toTypeName(exceptionType), message);
    }

    @Override
    public InstructionBuilder throwException()
    {
        check();

        v.visitInsn(ATHROW);

        return this;
    }

    @Override
    public InstructionBuilder startSwitch(int min, int max, SwitchCallback callback)
    {
        check();

        assert callback != null;

        new SwitchBlockImpl(this, state, min, max).doCallback(callback);

        return this;
    }

    @Override
    public InstructionBuilder startVariable(String type, final LocalVariableCallback callback)
    {
        check();

        final LocalVariable var = state.startVariable(type);

        callback.doBuild(var, this);

        state.stopVariable(var);

        return this;
    }

    @Override
    public InstructionBuilder storeVariable(LocalVariable var)
    {
        check();

        state.store(var);

        return this;
    }

    @Override
    public InstructionBuilder loadVariable(LocalVariable var)
    {
        check();

        state.load(var);

        return this;
    }

    @Override
    public InstructionBuilder when(Condition condition, final InstructionBuilderCallback ifTrue)
    {
        check();

        assert ifTrue != null;

        // This is nice for code coverage but could be more efficient, possibly generate
        // more efficient bytecode, if it talked to the v directly.

        return when(condition, new WhenCallback()
        {
            @Override
            public void ifTrue(InstructionBuilder builder)
            {
                ifTrue.doBuild(builder);
            }

            @Override
            public void ifFalse(InstructionBuilder builder)
            {
            }
        });
    }

    @Override
    public InstructionBuilder when(Condition condition, final WhenCallback callback)
    {
        check();

        assert condition != null;
        assert callback != null;

        Label ifFalseLabel = new Label();
        Label endIfLabel = new Label();

        v.visitJumpInsn(conditionToOpcode.get(condition), ifFalseLabel);

        callback.ifTrue(this);

        v.visitJumpInsn(GOTO, endIfLabel);

        v.visitLabel(ifFalseLabel);

        callback.ifFalse(this);

        v.visitLabel(endIfLabel);

        return this;
    }

    @Override
    public InstructionBuilder doWhile(Condition condition, final WhileCallback callback)
    {
        check();

        assert condition != null;
        assert callback != null;

        Label doCheck = state.newLabel();

        Label exitLoop = new Label();

        callback.buildTest(this);

        v.visitJumpInsn(conditionToOpcode.get(condition), exitLoop);

        callback.buildBody(this);

        v.visitJumpInsn(GOTO, doCheck);

        v.visitLabel(exitLoop);

        return this;
    }

    @Override
    public InstructionBuilder increment(LocalVariable variable)
    {
        check();

        LVInfo info = state.locals.get(variable);

        v.visitIincInsn(info.offset, 1);

        return this;
    }

    @Override
    public InstructionBuilder arrayLength()
    {
        check();

        v.visitInsn(ARRAYLENGTH);

        return this;
    }

    @Override
    public InstructionBuilder iterateArray(final InstructionBuilderCallback callback)
    {
        startVariable("int", new LocalVariableCallback()
        {
            @Override
            public void doBuild(final LocalVariable indexVariable, InstructionBuilder builder)
            {
                builder.loadConstant(0).storeVariable(indexVariable);

                builder.doWhile(Condition.LESS_THAN, new WhileCallback()
                {
                    @Override
                    public void buildTest(InstructionBuilder builder)
                    {
                        builder.dupe().arrayLength();
                        builder.loadVariable(indexVariable).swap();
                    }

                    @Override
                    public void buildBody(InstructionBuilder builder)
                    {
                        builder.dupe().loadVariable(indexVariable).loadArrayElement();

                        callback.doBuild(builder);

                        builder.increment(indexVariable);
                    }
                });
            }
        });

        return this;
    }

    @Override
    public InstructionBuilder dupeWide()
    {
        check();

        v.visitInsn(DUP2);

        return this;
    }

    @Override
    public InstructionBuilder popWide()
    {
        check();

        v.visitInsn(POP2);

        return this;
    }

    @Override
    public InstructionBuilder compareSpecial(String typeName)
    {
        check();

        Integer opcode = typeToSpecialComparisonOpcode.get(typeName);

        if (opcode == null)
            throw new IllegalArgumentException(String.format("Not a special primitive type: '%s'.", typeName));

        v.visitInsn(opcode);

        return this;
    }

    void doCallback(InstructionBuilderCallback callback)
    {
        check();

        if (callback != null)
            callback.doBuild(this);

        lock();
    }
}
