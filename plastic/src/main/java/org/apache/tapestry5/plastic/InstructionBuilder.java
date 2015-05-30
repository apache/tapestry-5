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

import java.lang.reflect.Method;

/**
 * Simplifies the generation of method instructions for a particular method (or constructor), allowing bytecode to be
 * created with a friendlier API that focuses on Java type names (names as they would appear in Java source) rather than
 * JVM descriptors or internal names. In some limited cases, types may be specified as Java Class instances as well.
 * In addition, there is good support for primitive type boxing and unboxing.
 *
 * Most methods return the same instance of InstructionBuilder, allowing for a "fluid" API.
 *
 * More complex functionality, such as {@linkplain #startTryCatch(TryCatchCallback)
 * try/catch blocks}, is more like a DSL (domain specific language), and is based on callbacks. This looks better in
 * Groovy and will be more reasonable once JDK 1.8 closures are available; in the meantime, it means some deeply nested
 * inner classes, but helps ensure that correct bytecode is generated and helps to limit the amount of bookkeeping is
 * necessary on the part of code using InstructionBuilder.
 */
@SuppressWarnings("rawtypes")
public interface InstructionBuilder
{
    /**
     * Returns the default value for the method, which may be null, or a specific primitive value.
     */
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
     * @param index to argument (0 is the first argument, not this)
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
     * @param containingClassName class name containing the method
     * @param description         describes the method name, parameters and return type
     */
    @Opcodes("INVOKESPECIAL")
    InstructionBuilder invokeSpecial(String containingClassName, MethodDescription description);

    /**
     * Invokes a standard virtual method.
     */
    @Opcodes("INVOKEVIRTUAL")
    InstructionBuilder invokeVirtual(String className, String returnType, String methodName, String... argumentTypes);

    @Opcodes("INVOKEVIRTUAL")
    InstructionBuilder invokeVirtual(PlasticMethod method);

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
     * Automatically invokes an interface or virtual method. Remember to use {@link #invokeConstructor(Class, Class...)}
     * for constructors and {@link #invokeSpecial(String, MethodDescription)} for private methods.
     */
    InstructionBuilder invoke(Method method);

    /**
     * Invokes a static method of a class.
     */
    @Opcodes("INVOKESTATIC")
    InstructionBuilder invokeStatic(Class clazz, Class returnType, String methodName, Class... argumentTypes);

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
     * @param typeName possibly primitive type name
     */
    @Opcodes("INVOKEVIRTUAL")
    InstructionBuilder unboxPrimitive(String typeName);

    /**
     * Loads an instance field onto the stack. The object containing the field should already be loaded onto the stack
     * (usually, via {@link #loadThis()}).
     *
     * @param className name of class containing the field
     * @param fieldName name of the field
     * @param typeName  type of field
     */
    @Opcodes("GETFIELD")
    InstructionBuilder getField(String className, String fieldName, String typeName);

    /**
     * Loads an instance or static field onto the stack. The plastic class instance containing the field should already be loaded
     * onto the stack (usually, via {@link #loadThis()}).
     *
     * @param field identifies name, type and container of field to load
     */
    @Opcodes("GETFIELD")
    InstructionBuilder getField(PlasticField field);

    /**
     * Loads a field onto the stack. This version is used when the
     * field type is known at build time, rather than discovered at runtime.
     *
     * @param className name of class containing the field
     * @param fieldName name of the field
     * @param fieldType type of field
     */
    @Opcodes("GETFIELD")
    InstructionBuilder getField(String className, String fieldName, Class fieldType);

    /**
     * Gets a static field; does not consume a value from the stack, but pushes the fields' value onto the stack.
     *
     * @param className name of class containing the field
     * @param fieldName name of the field
     * @param fieldType type of field
     */
    @Opcodes("GETSTATIC")
    InstructionBuilder getStaticField(String className, String fieldName, Class fieldType);

    /**
     * Gets a static field; does not consume a value from the stack, but pushes the fields' value onto the stack.
     *
     * @param className name of class containing the field
     * @param fieldName name of the field
     * @param typeName  type of field
     */
    @Opcodes("GETSTATIC")
    InstructionBuilder getStaticField(String className, String fieldName,
                                      String typeName);


    /**
     * Sets a static field; the new field value should be on top of the stack.
     *
     * @param className name of class containing the field
     * @param fieldName name of the field
     * @param fieldType type of field
     */
    @Opcodes("PUTSTATIC")
    InstructionBuilder putStaticField(String className, String fieldName, Class fieldType);

    /**
     * Sets a static field; the new field value should be on top of the stack.
     *
     * @param className name of class containing the field
     * @param fieldName name of the field
     * @param typeName  type of field
     */
    @Opcodes("PUTSTATIC")
    InstructionBuilder putStaticField(String className, String fieldName,
                                      String typeName);

    /**
     * Expects the stack to contain the instance to update, and the value to store into the field.
     */
    @Opcodes("PUTFIELD")
    InstructionBuilder putField(String className, String fieldName, String typeName);

    @Opcodes("PUTFIELD")
    InstructionBuilder putField(String className, String fieldName, Class fieldType);

    /**
     * Loads a value from an array object, which must be the top element of the stack.
     *
     * @param index       constant index into the array
     * @param elementType the type name of the elements of the array
     *                    <strong>Note: currently only reference types (objects and arrays) are supported, not
     *                    primitives</strong>
     */
    @Opcodes("LDC, AALOAD")
    InstructionBuilder loadArrayElement(int index, String elementType);

    /**
     * Loads a value from an array object. The stack should have the array at depth 1, and an array index
     * on top. Only object arrays (not arrays of primitives) are supported.
     */
    @Opcodes("AALOAD")
    InstructionBuilder loadArrayElement();

    /**
     * Adds a check that the object on top of the stack is assignable to the indicated class.
     *
     * @param className class to cast to
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
     * @param tryCatchCallback allows generation of try, catch, and finally clauses
     */
    InstructionBuilder startTryCatch(TryCatchCallback tryCatchCallback);

    /**
     * Creates a new, uninitialized instance of the indicated class. This should be followed
     * by code to call the new instance's constructor.
     *
     * @param className of class to instantiate
     */
    @Opcodes("NEW")
    InstructionBuilder newInstance(String className);

    /**
     * A convenience version of {@link #newInstance(String)} used when the class is known
     * at build time.
     *
     * @param clazz to instantiate
     */
    @Opcodes("NEW")
    InstructionBuilder newInstance(Class clazz);

    /**
     * Invokes a constructor on a class. The instance should already be on the stack, followed
     * by the right number and type of parameters. Note that a constructor acts like a void method,
     * so you will often follow the sequence: newInstance(), dupe(0), invokeConstructor() so that a reference
     * to the instance is left on the stack.F
     *
     * @param className     the class containing the constructor
     * @param argumentTypes java type names for each argument of the constructor
     */
    @Opcodes("INVOKESPECIAL")
    InstructionBuilder invokeConstructor(String className, String... argumentTypes);

    @Opcodes("INVOKESPECIAL")
    InstructionBuilder invokeConstructor(Class clazz, Class... argumentTypes);

    /**
     * Duplicates the top object on the stack, placing the result at some depth.
     *
     * @param depth 0 (DUP), 1 (DUP_X1) or 2 (DUP_X2)
     */
    @Opcodes("DUP, DUP_X1, DUP_X2")
    InstructionBuilder dupe(int depth);

    /**
     * Duplicates a wide value (a primitive long or double).
     */
    @Opcodes("DUP2")
    InstructionBuilder dupeWide();

    /**
     * Pops a wide value (a primitive long or double).
     */
    @Opcodes("POP2")
    InstructionBuilder popWide();

    /**
     * Duplicates the top object on the stack. Commonly used with {@link #when(Condition, WhenCallback)}.
     *
     * @see #dupe(int)
     */
    @Opcodes("DUP")
    InstructionBuilder dupe();

    /**
     * Discards the top value on the stack. Assumes the value is a single word value: an object reference, or a small
     * primitive) and not a double or long.
     */
    InstructionBuilder pop();

    /**
     * Swaps the top element of the stack with the next element down. Note that this can cause problems if the top
     * element on the stack
     * is a long or double.
     */
    @Opcodes("SWAP")
    InstructionBuilder swap();

    /**
     * Loads a constant value
     *
     * @param constant Integer, Float, Double, Long, String or null
     */
    @Opcodes("LDC, ICONST_*, LCONST_*, FCONST_*, DCONST_*, ACONST_NULL")
    InstructionBuilder loadConstant(Object constant);

    /**
     * Loads a Java type (a Class instance) as a constant. This assumes the type name is the name of class (or array)
     * but not a primitive type.
     *
     * @param typeName Java class name
     */
    @Opcodes("LDC")
    InstructionBuilder loadTypeConstant(String typeName);

    /**
     * Loads a Java type (a Class instance) as a constant. This assumes the type name is the name of class (or array)
     * but not a primitive type.
     *
     * @param type Java type to load as a constant
     */
    @Opcodes("LDC")
    InstructionBuilder loadTypeConstant(Class type);

    /**
     * Casts the object on top of the stack to the indicated type. For primitive types, casts to the wrapper type
     * and invokes the appropriate unboxing static method call, leaving a primitive type value on the stack.
     *
     * @param typeName to cast or unbox to
     */
    @Opcodes("CHECKCAST, INVOKEVIRTUAL")
    InstructionBuilder castOrUnbox(String typeName);

    /**
     * Throws an exception with a fixed message. Assumes the exception class includes a constructor that takes a single
     * string.
     *
     * @param className name of exception class to instantiate
     * @param message   message (passed as first and only parameter to constructor)
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
     * @param min the minimum value to match against
     * @param max the maximum value to match against
     */
    @Opcodes("TABLESWITCH")
    InstructionBuilder startSwitch(int min, int max, SwitchCallback callback);

    /**
     * Starts a block where the given name is active.
     *
     * @param type     type of local variable
     * @param callback generates code used when variable is in effect
     */
    InstructionBuilder startVariable(String type, LocalVariableCallback callback);

    /**
     * Stores the value on top of the stack to a local variable (previously defined by
     * {@link #startVariable(String, LocalVariableCallback)}.
     */
    @Opcodes("ASTORE, ISTORE, LSTORE, FSTORE, DSTORE")
    InstructionBuilder storeVariable(LocalVariable variable);

    /**
     * Loads a value from a local variable and pushes it onto the stack. The variable is defined by
     * {@link #startVariable(String, LocalVariableCallback)}.
     */
    @Opcodes("ALOAD, ILOAD, LLOAD, FLOAD, DLOAD")
    InstructionBuilder loadVariable(LocalVariable variable);

    /**
     * Executes conditional code based on a {@link Condition}. The testing opcodes all pop
     * the value off the stack, so this is usually preceded by {@link #dupe(int) dupe(0)}.
     *
     * @param condition defines true and false cases
     * @param callback  provides code for true and false blocks
     * @return this builder
     */
    @Opcodes("IFEQ, etc., GOTO")
    InstructionBuilder when(Condition condition, WhenCallback callback);

    /**
     * Simplified version of {@link #when(Condition, WhenCallback)} that
     * simply executes the callback code when the condition is true and does nothing
     * if the condition is false (the more general case).
     *
     * The testing opcodes all pop the value off the stack, so this is usually preceded by {@link #dupe(int) dupe(0)}.
     *
     * @param condition to evaluate
     * @param ifTrue    generates code for when condition is true
     * @return this builder
     */
    @Opcodes("IFEQ, etc., GOTO")
    InstructionBuilder when(Condition condition, InstructionBuilderCallback ifTrue);

    /**
     * Implements a simple loop based on a condition. First the {@linkplain WhileCallback#buildTest(InstructionBuilder)}
     * code is executed, then the condition is evaluated (which will consume at least the top value on the stack).
     * When the condition is false, the loop is exited. When the condition is true, the code defined by
     * {@link WhileCallback#buildBody(InstructionBuilder)} is executed, and then a GOTO back to the test code.
     *
     * @param condition
     * @param callback
     * @return this builder
     */
    @Opcodes("IFEQ, etc., GOTO")
    InstructionBuilder doWhile(Condition condition, WhileCallback callback);

    /**
     * Expects an array to be the top value on the stack. Iterates the array.
     * The callback generates code that will have each successive value from the array
     * as the top value on the stack. Creates a variable to store the loop index.
     *
     * @param callback to invoke. The element will be the top value on the stack. The callback is responsible
     *                 for removing it from the stack.
     * @return this builder
     */
    @Opcodes("IINC, ARRAYLENGTH, IFEQ, etc., GOTO")
    InstructionBuilder iterateArray(InstructionBuilderCallback callback);

    /**
     * Increments a local integer variable.
     *
     * @return this builder
     */
    @Opcodes("IINC")
    InstructionBuilder increment(LocalVariable variable);

    /**
     * Expects the top object on the stack to be an array. Replaces it with the length of that array.
     */
    @Opcodes("ARRAYLENGTH")
    InstructionBuilder arrayLength();

    /**
     * Special comparison logic for primitive float, double and long. Expect two matching wide values
     * on the stack. Reduces the two wide values to a single int value: -1, 0, or 1 depending on whether the deeper
     * value is less than, equal to, or greater than the top value on the stack.
     *
     * @param typeName
     */
    @Opcodes("LCMP, FCMPL, DCMPL")
    InstructionBuilder compareSpecial(String typeName);
}
