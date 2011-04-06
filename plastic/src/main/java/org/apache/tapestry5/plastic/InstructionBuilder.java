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

package org.apache.tapestry5.plastic;

/**
 * Simplifies the generation of method instructions for a particular method (or constructor), allowing bytecode to be
 * created with a friendlier API that focuses on Java type names (names as they would appear in Java source) rather than
 * JVM descriptors or internal names. In some limited cases, types may be specified as Java Class instances as well.
 * In addition, there is good support for primitive type boxing and unboxing.
 * <p>
 * Most methods return the same instance of InstructionBuilder, allowing for a "fluid" API.
 * <p>
 * More complex functionality, such as {@linkplain #startTryCatch(InstructionBuilderCallback, TryCatchCallback)
 * try/catch blocks}, is more DSL (domain specific language) like, and is based on callbacks. This looks better in
 * Groovy and will be more reasonable once JDK 1.8 closures are available; in the meantime, it means some deeply nested
 * inner classes, but helps ensure that correct bytecode is generated.
 */
@SuppressWarnings("rawtypes")
public interface InstructionBuilder
{
    /** Returns the default value for the method, which may be null, or a specific primitive value. */
    @Opcodes("ACONST_NULL, LCONST_0, FCONST_0, DCONST_0, ICONST_0, RETURN, ARETURN, IRETURN, FRETURN, LRETURN, DRETURN")
    InstructionBuilder returnDefaultValue();

    /**
     * Loads this onto the stack.
     */
    @Opcodes("ALOAD")
    InstructionBuilder loadThis();

    /**
     * Loads the null constant onto the stack.
     */
    @Opcodes("ACONST_NULL")
    InstructionBuilder loadNull();

    /**
     * Loads an argument onto the stack, using the opcode appropriate to the argument's type. In addition
     * this automatically adjusts for arguments of primitive type long or double (which take up two
     * local variable indexes, rather than one as for all other types)
     * 
     * @param index
     *            to argument (0 is the first argument, not this)
     */
    @Opcodes("ALOAD, ILOAD, LLOAD, FLOAD, DLOAD")
    InstructionBuilder loadArgument(int index);

    /**
     * Loads all arguments for the current method onto the stack; this is used when invoking a method
     * that takes the exact same parameters (often, a super-class implementation). A call to {@link #loadThis()} (or
     * some other way of identifying the target method) should precede this call.
     */
    @Opcodes("ALOAD, ILOAD, LLOAD, FLOAD, DLOAD")
    InstructionBuilder loadArguments();

    /**
     * Invokes an instance method of a base class, or a private method of a class, using the target object
     * and parameters already on the stack. Leaves the result on the stack (unless its a void method).
     * 
     * @param containingClassName
     *            class name containing the method
     * @param description
     *            describes the method name, parameters and return type
     */
    @Opcodes("INVOKESPECIAL")
    InstructionBuilder invokeSpecial(String containingClassName, MethodDescription description);

    /**
     * Invokes a standard virtual method.
     */
    @Opcodes("INVOKEVIRTUAL")
    InstructionBuilder invokeVirtual(String className, String returnType, String methodName, String... argumentTypes);

    /**
     * Invokes a standard virtual method.
     */
    @Opcodes("INVOKEINTERFACE")
    InstructionBuilder invokeInterface(String interfaceName, String returnType, String methodName,
            String... argumentTypes);

    /**
     * Automatically invokes an interface or virtual method. Remember to use {@link #invokeConstructor(Class, Class...)}
     * for constructors and {@link #invokeSpecial(String, MethodDescription)} for private methods.
     */
    @Opcodes("INVOKEVIRTUAL, INVOKEINTERFACE")
    InstructionBuilder invoke(Class clazz, Class returnType, String methodName, Class... argumentTypes);

    /**
     * Returns the top value on the stack. For void methods, no value should
     * be on the stack and the method will simply return.
     */
    @Opcodes("ARETURN, IRETURN, LRETURN, FRETURN, DRETURN")
    InstructionBuilder returnResult();

    /**
     * If the type name is a primitive type, adds code to box the type into the equivalent wrapper type, using static
     * methods on the wrapper type. Does nothing if the type is not primitive, or type void.
     */
    @Opcodes("INVOKESTATIC")
    InstructionBuilder boxPrimitive(String typeName);

    /**
     * Unboxes a wrapper type to a primitive type if typeName is a primitive type name (the value on the stack
     * should be the corresponding wrapper type instance). Does nothing for non-primitive types.
     * 
     * @param typeName
     *            possibly primitive type name
     */
    @Opcodes("INVOKEVIRTUAL")
    InstructionBuilder unboxPrimitive(String typeName);

    /**
     * Loads an instance field onto the stack. The object containing the field should already be loaded onto the stack.
     * 
     * @param className
     *            name of class containing the field
     * @param fieldName
     *            name of the field
     * @param typeName
     *            type of field
     */
    @Opcodes("GETFIELD")
    InstructionBuilder getField(String className, String fieldName, String typeName);

    /**
     * Loads a field onto the stack. This version is used when the
     * field type is known at build time, rather than discovered at runtime.
     * 
     * @param className
     *            name of class containing the field
     * @param fieldName
     *            name of the field
     * @param fieldType
     *            type of field
     */
    @Opcodes("GETFIELD")
    InstructionBuilder getField(String className, String fieldName, Class fieldType);

    /** Expects the stack to contain the instance to update, and the value to store into the field. */
    @Opcodes("PUTFIELD")
    InstructionBuilder putField(String className, String fieldName, String typeName);

    @Opcodes("PUTFIELD")
    InstructionBuilder putField(String className, String fieldName, Class fieldType);

    /**
     * Loads a value from an array object, which must be the top element of the stack.
     * 
     * @param index
     *            into the array
     * @param elementType
     *            the type name of the elements of the array
     *            <strong>Note: currently only reference types (objects and arrays) are supported, not
     *            primitives</strong>
     */
    @Opcodes("LDC, AALOAD")
    InstructionBuilder loadArrayElement(int index, String elementType);

    /**
     * Adds a check that the object on top of the stack is assignable to the indicated class.
     * 
     * @param className
     *            class to cast to
     */
    @Opcodes("CHECKCAST")
    InstructionBuilder checkcast(String className);

    @Opcodes("CHECKCAST")
    InstructionBuilder checkcast(Class clazz);

    /**
     * Defines the start of a block that can have exception handlers and finally blocks applied.
     * Continue using this InstructionBuilder to define code inside the block, then call
     * methods on the InstructionBlock to define the end of the block and set up handlers.
     * 
     * @param tryCallback
     *            generates the code that is "inside" the <code>try</code>
     * @param catchCallback
     *            generates <code>catch</code> and <code>finally</code> blocks
     */
    InstructionBuilder startTryCatch(TryCatchCallback catchCallback);

    /**
     * Creates a new, uninitialized instance of the indicated class. This should be followed
     * by code to call the new instance's constructor.
     * 
     * @param className
     *            of class to instantiate
     */
    @Opcodes("NEW")
    InstructionBuilder newInstance(String className);

    /**
     * A convenience version of {@link #newInstance(String)} used when the class is known
     * at build time.
     * 
     * @param clazz
     *            to instantiate
     */
    @Opcodes("NEW")
    InstructionBuilder newInstance(Class clazz);

    /**
     * Invokes a constructor on a class. The instance should already be on the stack, followed
     * by the right number and type of parameters. Note that a constructor acts like a void method,
     * so you will often follow the sequence: newInstance(), dupe(0), invokeConstructor() so that a reference
     * to the instance is left on the stack.F
     * 
     * @param className
     *            the class containing the constructor
     * @param argumentTypes
     *            java type names for each argument of the constructor
     * @return
     */
    @Opcodes("INVOKESPECIAL")
    InstructionBuilder invokeConstructor(String className, String... argumentTypes);

    @Opcodes("INVOKESPECIAL")
    InstructionBuilder invokeConstructor(Class clazz, Class... argumentTypes);

    /**
     * Duplicates the top` object on the stack, placing the result at some depth.
     * 
     * @param depth
     *            0 (DUP), 1 (DUP_X1) or 2 (DUP_X2)
     * @return
     */
    @Opcodes("DUP, DUP_X1, DUP_X2")
    InstructionBuilder dupe(int depth);

    /**
     * Discards the top value on the stack. Assumes the value is a single word value: an object reference, or a small
     * primitive) and not a double or long.
     */
    InstructionBuilder pop();

    /**
     * Swaps the top element of the stack with the next element down. Note that this can cause problems if the top
     * element on the stack
     * is a long or double.
     * 
     * @return
     */
    @Opcodes("SWAP")
    InstructionBuilder swap();

    /**
     * Loads a constant value
     * 
     * @param constant
     *            a non-null Integer, Float, Double, Long, String.
     */
    @Opcodes("LDC")
    InstructionBuilder loadConstant(Object constant);

    /**
     * Loads a Java type (a Class instance) as a constant. This assumes the type name is the name of class (or array)
     * but not a primitive type.
     * 
     * @param typeName
     *            Java class name
     */
    @Opcodes("LDC")
    InstructionBuilder loadTypeConstant(String typeName);

    /**
     * Loads a Java type (a Class instance) as a constant. This assumes the type name is the name of class (or array)
     * but not a primitive type.
     * 
     * @param type
     *            Java type to load as a constant
     */
    @Opcodes("LDC")
    InstructionBuilder loadTypeConstant(Class type);

    /**
     * Casts the object on top of the stack to the indicated type. For primitive types, casts to the wrapper type
     * and invokes the appropriate unboxing static method call, leaving a primitive type value on the stack.
     * 
     * @param typeName
     *            to cast or unbox to
     */
    @Opcodes("CHECKCAST, INVOKEVIRTUAL")
    InstructionBuilder castOrUnbox(String typeName);

    /**
     * Throws an exception with a fixed message. Assumes the exception class includes a constructor that takes a single
     * string.
     * 
     * @param className
     *            name of exception class to instantiate
     * @param message
     *            message (passed as first and only parameter to constructor)
     */
    @Opcodes("NEW, DUP, LDC, INVOKESPECIAL, ATHROW")
    InstructionBuilder throwException(String className, String message);

    @Opcodes("NEW, DUP, LDC, INVOKESPECIAL, ATHROW")
    InstructionBuilder throwException(Class<? extends Throwable> exceptionType, String message);

    /**
     * Throws the exception on the top of the stack.
     */
    @Opcodes("ATHROW")
    InstructionBuilder throwException();

    /**
     * Starts a switch statement.
     * 
     * @param min
     *            the minimum value to match against
     * @param max
     *            the maximum value to match against
     */
    @Opcodes("TABLESWITCH")
    InstructionBuilder startSwitch(int min, int max, SwitchCallback callback);

    /**
     * Starts a block where the given name is active.
     * 
     * @param name
     *            name of local variable
     * @param type
     *            type of local variable
     * @param callback
     *            generates code used when variable is in effect
     */
    InstructionBuilder startVariable(String name, String type, InstructionBuilderCallback callback);

    /**
     * Stores the value on top of the stack to a local variable (previously defined by
     * {@link #startVariable(String, String, InstructionBuilderCallback)}.
     */
    @Opcodes("ASTORE, ISTORE, LSTORE, FSTORE, DSTORE")
    InstructionBuilder storeVariable(String name);

    /**
     * Loads a value from a local variable and pushes it onto the stack. The variable must have been previously defined
     * by {@link #startVariable(String, String, InstructionBuilderCallback)}.
     */
    @Opcodes("ALOAD, ILOAD, LLOAD, FLOAD, DLOAD")
    InstructionBuilder loadVariable(String name);

    /**
     * Checks if the top value on the stack is zero and invokes the code from one of the two callbacks.
     * 
     * @param ifTrue
     *            callback to generate code for the case where the top value on the stack is zero (may be null)
     * @param ifFalse
     *            callback to generate code for the case where the top value on the stack is non-zero
     *            (may be null)
     */
    @Opcodes("IFEQ, GOTO")
    InstructionBuilder ifZero(InstructionBuilderCallback ifTrue, InstructionBuilderCallback ifFalse);

    /**
     * Checks if the top value on the stack is null and invokes the code from one of the two callbacks.
     * 
     * @param ifTrue
     *            callback to generate code for the case where the top value on the stack is null (may be null)
     * @param ifFalse
     *            callback to generate code for the case where the top value on the stack is not null
     *            (may be null)
     */
    @Opcodes("IFNULL, GOTO")
    InstructionBuilder ifNull(InstructionBuilderCallback ifTrue, InstructionBuilderCallback ifFalse);
}
