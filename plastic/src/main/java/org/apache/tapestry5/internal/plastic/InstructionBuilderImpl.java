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

import java.lang.reflect.Method;

import org.apache.tapestry5.internal.plastic.asm.Label;
import org.apache.tapestry5.internal.plastic.asm.MethodVisitor;
import org.apache.tapestry5.internal.plastic.asm.Opcodes;
import org.apache.tapestry5.internal.plastic.asm.Type;
import org.apache.tapestry5.plastic.InstructionBuilder;
import org.apache.tapestry5.plastic.InstructionBuilderCallback;
import org.apache.tapestry5.plastic.MethodDescription;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.plastic.PlasticUtils;
import org.apache.tapestry5.plastic.SwitchCallback;
import org.apache.tapestry5.plastic.TryCatchCallback;

@SuppressWarnings("rawtypes")
public class InstructionBuilderImpl extends Lockable implements Opcodes, InstructionBuilder
{
    private static final int[] DUPE_OPCODES = new int[]
    { DUP, DUP_X1, DUP_X2 };

    protected final InstructionBuilderState state;

    protected final MethodVisitor v;

    protected final NameCache cache;

    InstructionBuilderImpl(MethodDescription description, MethodVisitor visitor, NameCache cache)
    {
        this(new InstructionBuilderState(description, visitor, cache));
    }

    InstructionBuilderImpl(InstructionBuilderState state)
    {
        this.state = state;

        // These are conveniences for values stored inside the state. In fact,
        // these fields predate the InstructionBuilderState type.
        this.v = state.visitor;
        this.cache = state.nameCache;
    }

    public InstructionBuilder returnDefaultValue()
    {
        check();

        PrimitiveType type = PrimitiveType.getByName(state.description.returnType);

        if (type == null)
        {
            v.visitInsn(ACONST_NULL);
            v.visitInsn(ARETURN);
        }
        else
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

    public InstructionBuilder loadThis()
    {
        check();

        v.visitVarInsn(ALOAD, 0);

        return this;
    }

    public InstructionBuilder loadNull()
    {
        check();

        v.visitInsn(ACONST_NULL);

        return this;
    }

    public InstructionBuilder loadArgument(int index)
    {
        check();

        PrimitiveType type = PrimitiveType.getByName(state.description.argumentTypes[index]);

        int opcode = type == null ? ALOAD : type.loadOpcode;

        v.visitVarInsn(state.argumentLoadOpcode[index], state.argumentIndex[index]);

        return this;
    }

    public InstructionBuilder loadArguments()
    {
        check();

        for (int i = 0; i < state.description.argumentTypes.length; i++)
        {
            loadArgument(i);
        }

        return this;
    }

    public InstructionBuilder invokeSpecial(String containingClassName, MethodDescription description)
    {
        check();

        doInvoke(INVOKESPECIAL, containingClassName, description);

        return this;
    }

    public InstructionBuilder invokeVirtual(PlasticMethod method)
    {
        check();

        assert method != null;

        MethodDescription description = method.getDescription();

        return invokeVirtual(method.getPlasticClass().getClassName(), description.returnType, description.methodName,
                description.argumentTypes);
    }

    public InstructionBuilder invokeVirtual(String className, String returnType, String methodName,
            String... argumentTypes)
    {
        check();

        doInvoke(INVOKEVIRTUAL, className, returnType, methodName, argumentTypes);

        return this;
    }

    public InstructionBuilder invokeInterface(String interfaceName, String returnType, String methodName,
            String... argumentTypes)
    {
        check();

        doInvoke(INVOKEINTERFACE, interfaceName, returnType, methodName, argumentTypes);

        return this;
    }

    private void doInvoke(int opcode, String className, String returnType, String methodName, String... argumentTypes)
    {
        v.visitMethodInsn(opcode, cache.toInternalName(className), methodName,
                cache.toMethodDescriptor(returnType, argumentTypes));
    }

    public InstructionBuilder invokeStatic(Class clazz, Class returnType, String methodName, Class... argumentTypes)
    {
        doInvoke(INVOKESTATIC, clazz, returnType, methodName, argumentTypes);

        return this;
    }

    private void doInvoke(int opcode, Class clazz, Class returnType, String methodName, Class... argumentTypes)
    {
        doInvoke(opcode, clazz.getName(), cache.toTypeName(returnType), methodName,
                PlasticUtils.toTypeNames(argumentTypes));
    }

    public InstructionBuilder invoke(Method method)
    {
        check();

        return invoke(method.getDeclaringClass(), method.getReturnType(), method.getName(), method.getParameterTypes());
    }

    public InstructionBuilder invoke(Class clazz, Class returnType, String methodName, Class... argumentTypes)
    {
        check();

        doInvoke(clazz.isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL, clazz, returnType, methodName, argumentTypes);

        return this;
    }

    private void doInvoke(int opcode, String containingClassName, MethodDescription description)
    {
        v.visitMethodInsn(opcode, cache.toInternalName(containingClassName), description.methodName,
                cache.toDesc(description));
    }

    public InstructionBuilder returnResult()
    {
        check();

        PrimitiveType type = PrimitiveType.getByName(state.description.returnType);

        int opcode = type == null ? ARETURN : type.returnOpcode;

        v.visitInsn(opcode);

        return this;
    }

    public InstructionBuilder boxPrimitive(String typeName)
    {
        check();

        PrimitiveType type = PrimitiveType.getByName(typeName);

        if (type != null && type != PrimitiveType.VOID)
        {
            v.visitMethodInsn(INVOKESTATIC, type.wrapperInternalName, "valueOf", type.valueOfMethodDescriptor);
        }

        return this;
    }

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
        v.visitMethodInsn(INVOKEVIRTUAL, type.wrapperInternalName, type.toValueMethodName, type.toValueMethodDescriptor);
    }

    public InstructionBuilder getField(String className, String fieldName, String typeName)
    {
        check();

        v.visitFieldInsn(GETFIELD, cache.toInternalName(className), fieldName, cache.toDesc(typeName));

        return this;
    }

    public InstructionBuilder getField(PlasticField field)
    {
        check();

        return getField(field.getPlasticClass().getClassName(), field.getName(), field.getTypeName());
    }

    public InstructionBuilder putField(String className, String fieldName, String typeName)
    {
        check();

        v.visitFieldInsn(PUTFIELD, cache.toInternalName(className), fieldName, cache.toDesc(typeName));

        return this;
    }

    public InstructionBuilder putField(String className, String fieldName, Class fieldType)
    {
        check();

        return putField(className, fieldName, cache.toTypeName(fieldType));
    }

    public InstructionBuilder getField(String className, String fieldName, Class fieldType)
    {
        check();

        return getField(className, fieldName, fieldType.getName());
    }

    public InstructionBuilder loadArrayElement(int index, String elementType)
    {
        check();

        v.visitLdcInsn(index);

        PrimitiveType type = PrimitiveType.getByName(elementType);

        if (type == null)
        {
            v.visitInsn(AALOAD);
        }
        else
        {
            throw new RuntimeException("Access to non-object arrays is not yet supported.");
        }

        return this;
    }

    public InstructionBuilder checkcast(String className)
    {
        check();

        // Found out the hard way that array names are handled differently; you cast to the descriptor, not the internal
        // name.

        String internalName = className.contains("[") ? cache.toDesc(className) : cache.toInternalName(className);

        v.visitTypeInsn(CHECKCAST, internalName);

        return this;
    }

    public InstructionBuilder checkcast(Class clazz)
    {
        check();

        return checkcast(cache.toTypeName(clazz));
    }

    public InstructionBuilder startTryCatch(TryCatchCallback callback)
    {
        check();

        new TryCatchBlockImpl(state).doCallback(callback);

        return this;
    }

    public InstructionBuilder newInstance(String className)
    {
        check();

        v.visitTypeInsn(NEW, cache.toInternalName(className));

        return this;
    }

    public InstructionBuilder newInstance(Class clazz)
    {
        check();

        return newInstance(clazz.getName());
    }

    public InstructionBuilder invokeConstructor(String className, String... argumentTypes)
    {
        check();

        doInvoke(INVOKESPECIAL, className, "void", "<init>", argumentTypes);

        return this;
    }

    public InstructionBuilder invokeConstructor(Class clazz, Class... argumentTypes)
    {
        check();

        return invokeConstructor(clazz.getName(), PlasticUtils.toTypeNames(argumentTypes));
    }

    public InstructionBuilder dupe(int depth)
    {
        check();

        if (depth < 0 || depth >= DUPE_OPCODES.length)
            throw new IllegalArgumentException(String.format(
                    "Dupe depth %d is invalid; values from 0 to %d are allowed.", depth, DUPE_OPCODES.length - 1));

        v.visitInsn(DUPE_OPCODES[depth]);

        return this;
    }

    public InstructionBuilder pop()
    {
        check();

        v.visitInsn(POP);

        return this;
    }

    public InstructionBuilder swap()
    {
        check();

        v.visitInsn(SWAP);

        return this;
    }

    public InstructionBuilder loadConstant(Object constant)
    {
        check();

        v.visitLdcInsn(constant);

        return this;
    }

    public InstructionBuilder loadTypeConstant(String typeName)
    {
        check();

        Type type = Type.getType(cache.toDesc(typeName));

        v.visitLdcInsn(type);

        return this;
    }

    public InstructionBuilder loadTypeConstant(Class clazz)
    {
        check();

        Type type = Type.getType(clazz);

        v.visitLdcInsn(type);

        return this;
    }

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

    public InstructionBuilder throwException(String className, String message)
    {
        check();

        newInstance(className);
        dupe(0);
        loadConstant(message);
        invokeConstructor(className, "java.lang.String");
        v.visitInsn(ATHROW);

        return this;
    }

    public InstructionBuilder throwException(Class<? extends Throwable> exceptionType, String message)
    {
        check();

        return throwException(cache.toTypeName(exceptionType), message);
    }

    public InstructionBuilder throwException()
    {
        check();

        v.visitInsn(ATHROW);

        return this;
    }

    public InstructionBuilder startSwitch(int min, int max, SwitchCallback callback)
    {
        check();

        assert callback != null;

        new SwitchBlockImpl(state, min, max).doCallback(callback);

        return this;
    }

    public InstructionBuilder startVariable(String name, String type, InstructionBuilderCallback callback)
    {
        check();

        Label start = state.newLabel();
        Label end = new Label();

        LocalVariable var = new LocalVariable(name, type, state.localIndex++);

        v.visitLocalVariable(name, cache.toDesc(type), null, start, end, var.index);

        LocalVariable prior = state.locals.put(name, var);

        new InstructionBuilderImpl(state).doCallback(callback);

        v.visitLabel(end);

        state.localIndex--;

        // Restore the original variable with this name, probably just null though.

        state.locals.put(name, prior);

        return this;
    }

    public InstructionBuilder storeVariable(String name)
    {
        check();

        LocalVariable var = state.locals.get(name);

        PrimitiveType type = PrimitiveType.getByName(var.type);

        int opcode = type == null ? ASTORE : type.storeOpcode;

        v.visitVarInsn(opcode, var.index);

        return this;
    }

    public InstructionBuilder loadVariable(String name)
    {
        check();

        LocalVariable var = state.locals.get(name);

        PrimitiveType type = PrimitiveType.getByName(var.type);

        int opcode = type == null ? ALOAD : type.loadOpcode;

        v.visitVarInsn(opcode, var.index);

        return this;
    }

    public InstructionBuilder ifZero(InstructionBuilderCallback ifTrue, InstructionBuilderCallback ifFalse)
    {
        doConditional(IFEQ, ifTrue, ifFalse);

        return this;
    }

    public InstructionBuilder ifNull(InstructionBuilderCallback ifTrue, InstructionBuilderCallback ifFalse)
    {
        doConditional(IFNULL, ifTrue, ifFalse);

        return this;
    }

    private void doConditional(int opcode, InstructionBuilderCallback ifTrueCallback,
            InstructionBuilderCallback ifFalseCallback)
    {
        check();

        Label ifTrueLabel = new Label();
        Label endIfLabel = new Label();

        // Kind of clumsy code, but it will work.

        v.visitJumpInsn(opcode, ifTrueLabel);

        new InstructionBuilderImpl(state).doCallback(ifFalseCallback);

        v.visitJumpInsn(GOTO, endIfLabel);
        v.visitLabel(ifTrueLabel);

        new InstructionBuilderImpl(state).doCallback(ifTrueCallback);

        v.visitLabel(endIfLabel);
    }

    void doCallback(InstructionBuilderCallback callback)
    {
        check();

        if (callback != null)
            callback.doBuild(this);

        lock();
    }
}
