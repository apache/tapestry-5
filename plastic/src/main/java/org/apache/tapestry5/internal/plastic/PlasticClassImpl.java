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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry5.internal.plastic.asm.Opcodes;
import org.apache.tapestry5.internal.plastic.asm.Type;
import org.apache.tapestry5.internal.plastic.asm.tree.AbstractInsnNode;
import org.apache.tapestry5.internal.plastic.asm.tree.AnnotationNode;
import org.apache.tapestry5.internal.plastic.asm.tree.ClassNode;
import org.apache.tapestry5.internal.plastic.asm.tree.FieldInsnNode;
import org.apache.tapestry5.internal.plastic.asm.tree.FieldNode;
import org.apache.tapestry5.internal.plastic.asm.tree.InsnList;
import org.apache.tapestry5.internal.plastic.asm.tree.MethodInsnNode;
import org.apache.tapestry5.internal.plastic.asm.tree.MethodNode;
import org.apache.tapestry5.internal.plastic.asm.tree.VarInsnNode;
import org.apache.tapestry5.plastic.AnnotationAccess;
import org.apache.tapestry5.plastic.ClassInstantiator;
import org.apache.tapestry5.plastic.ComputedValue;
import org.apache.tapestry5.plastic.Condition;
import org.apache.tapestry5.plastic.WhenCallback;
import org.apache.tapestry5.plastic.FieldConduit;
import org.apache.tapestry5.plastic.FieldHandle;
import org.apache.tapestry5.plastic.InstanceContext;
import org.apache.tapestry5.plastic.InstructionBuilder;
import org.apache.tapestry5.plastic.InstructionBuilderCallback;
import org.apache.tapestry5.plastic.MethodAdvice;
import org.apache.tapestry5.plastic.MethodDescription;
import org.apache.tapestry5.plastic.MethodHandle;
import org.apache.tapestry5.plastic.MethodInvocation;
import org.apache.tapestry5.plastic.MethodInvocationResult;
import org.apache.tapestry5.plastic.MethodParameter;
import org.apache.tapestry5.plastic.PlasticClass;
import org.apache.tapestry5.plastic.PlasticField;
import org.apache.tapestry5.plastic.PlasticMethod;
import org.apache.tapestry5.plastic.PlasticUtils;
import org.apache.tapestry5.plastic.PropertyAccessType;
import org.apache.tapestry5.plastic.SwitchBlock;
import org.apache.tapestry5.plastic.SwitchCallback;
import org.apache.tapestry5.plastic.TryCatchBlock;
import org.apache.tapestry5.plastic.TryCatchCallback;

@SuppressWarnings("all")
public class PlasticClassImpl extends Lockable implements PlasticClass, InternalPlasticClassTransformation, Opcodes
{
    private static final String NOTHING_TO_VOID = "()V";

    private static final String CONSTRUCTOR_NAME = "<init>";

    private static final String OBJECT_INT_TO_OBJECT = "(Ljava/lang/Object;I)Ljava/lang/Object;";

    private static final String OBJECT_INT_OBJECT_TO_VOID = "(Ljava/lang/Object;ILjava/lang/Object;)V";

    private static final String OBJECT_INT_OBJECT_ARRAY_TO_METHOD_INVOCATION_RESULT = String.format(
            "(Ljava/lang/Object;I[Ljava/lang/Object;)%s", toDesc(Type.getInternalName(MethodInvocationResult.class)));

    private static final String ABSTRACT_METHOD_INVOCATION_INTERNAL_NAME = PlasticInternalUtils
            .toInternalName(AbstractMethodInvocation.class.getName());

    private static final String OBJECT_INTERNAL_NAME = Type.getInternalName(Object.class);

    private static final String HANDLE_SHIM_BASE_CLASS_INTERNAL_NAME = Type
            .getInternalName(PlasticClassHandleShim.class);

    private static final String STATIC_CONTEXT_INTERNAL_NAME = Type.getInternalName(StaticContext.class);

    private static final String INSTANCE_CONTEXT_INTERNAL_NAME = Type.getInternalName(InstanceContext.class);

    private static final String INSTANCE_CONTEXT_DESC = toDesc(INSTANCE_CONTEXT_INTERNAL_NAME);

    private static final String CONSTRUCTOR_DESC = String.format("(L%s;L%s;)V", STATIC_CONTEXT_INTERNAL_NAME,
            INSTANCE_CONTEXT_INTERNAL_NAME);

    private static final Method STATIC_CONTEXT_GET_METHOD = toMethod(StaticContext.class, "get", int.class);

    private static final Method COMPUTED_VALUE_GET_METHOD = toMethod(ComputedValue.class, "get", InstanceContext.class);

    private static final MethodDescription TO_STRING_METHOD_DESCRIPTION = new MethodDescription(String.class.getName(),
            "toString");

    private static String toDesc(String internalName)
    {
        return "L" + internalName + ";";
    }

    private static Method toMethod(Class declaringClass, String methodName, Class... parameterTypes)
    {
        try
        {
            return declaringClass.getMethod(methodName, parameterTypes);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private class PlasticMember implements AnnotationAccess
    {
        private final AnnotationAccess annotationAccess;

        PlasticMember(List<AnnotationNode> visibleAnnotations)
        {
            annotationAccess = pool.createAnnotationAccess(visibleAnnotations);
        }

        public <T extends Annotation> boolean hasAnnotation(Class<T> annotationType)
        {
            check();

            return annotationAccess.hasAnnotation(annotationType);
        }

        public <T extends Annotation> T getAnnotation(Class<T> annotationType)
        {
            check();

            return annotationAccess.getAnnotation(annotationType);
        }

    }

    private class MethodParameterImpl extends PlasticMember implements MethodParameter
    {
        private final String type;

        private final int index;

        MethodParameterImpl(List<AnnotationNode> visibleAnnotations, String type, int index)
        {
            super(visibleAnnotations);

            this.type = type;
            this.index = index;
        }

        public String getType()
        {
            check();

            return type;
        }

        public int getIndex()
        {
            check();

            return index;
        }
    }

    private class PlasticMethodImpl extends PlasticMember implements PlasticMethod, Comparable<PlasticMethodImpl>
    {
        private final MethodNode node;

        private MethodDescription description;

        private MethodHandleImpl handle;

        private MethodAdviceManager adviceManager;

        private List<MethodParameter> parameters;

        private int methodIndex = -1;

        public PlasticMethodImpl(MethodNode node)
        {
            super(node.visibleAnnotations);

            this.node = node;
            this.description = PlasticInternalUtils.toMethodDescription(node);
        }

        public String toString()
        {
            return String.format("PlasticMethod[%s in class %s]", description, className);
        }

        public PlasticClass getPlasticClass()
        {
            check();

            return PlasticClassImpl.this;
        }

        public MethodDescription getDescription()
        {
            check();

            return description;
        }

        public int compareTo(PlasticMethodImpl o)
        {
            return description.compareTo(o.description);
        }

        public MethodHandle getHandle()
        {
            check();

            if (handle == null)
            {
                methodIndex = nextMethodIndex++;
                handle = new MethodHandleImpl(className, description.toString(), methodIndex);
                shimMethods.add(this);
            }

            return handle;
        }

        public PlasticMethod changeImplementation(InstructionBuilderCallback callback)
        {
            check();

            // If the method is currently abstract, clear that flag.
            if (Modifier.isAbstract(node.access))
            {
                node.access = node.access & ~ACC_ABSTRACT;
                description = description.withModifiers(node.access);
            }

            node.instructions.clear();

            newBuilder(description, node).doCallback(callback);

            // With the implementation changed, it is necessary to intercept field reads/writes.
            // The node may not already have been in the fieldTransformMethods Set if it was
            // an introduced method.

            fieldTransformMethods.add(node);

            return this;
        }

        public PlasticMethod addAdvice(MethodAdvice advice)
        {
            check();

            assert advice != null;

            if (adviceManager == null)
            {
                adviceManager = new MethodAdviceManager(description, node);
                advisedMethods.add(this);
            }

            adviceManager.add(advice);

            return this;
        }

        public PlasticMethod delegateTo(final PlasticField field)
        {
            check();

            assert field != null;

            // TODO: Ensure that the field is a field of this class.

            // TODO: Better handling error case where delegating to a primitive or object array.

            // TODO: Is there a easy way to ensure that the type has the necessary method? I don't
            // like that errors along those lines may be deferred until execution time.

            changeImplementation(new InstructionBuilderCallback()
            {
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

        public PlasticMethod delegateTo(PlasticMethod delegateProvidingMethod)
        {
            check();

            assert delegateProvidingMethod != null;

            // TODO: ensure same class, ensure not primitive/array type
            final MethodDescription providerDescriptor = delegateProvidingMethod.getDescription();
            final String delegateType = providerDescriptor.returnType;

            if (delegateType.equals("void") || providerDescriptor.argumentTypes.length > 0)
                throw new IllegalArgumentException(
                        String.format(
                                "Method %s is not usable as a delegate provider; it must be a void method that takes no arguments.",
                                delegateProvidingMethod));

            changeImplementation(new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    // Load the field

                    builder.loadThis();

                    if (Modifier.isPrivate(providerDescriptor.modifiers))
                    {
                        builder.invokeSpecial(className, providerDescriptor);
                    }
                    else
                    {
                        builder.invokeVirtual(className, delegateType, providerDescriptor.methodName);
                    }

                    builder.loadArguments();

                    invokeDelegateAndReturnResult(builder, delegateType);
                }
            });

            return this;
        }

        public List<MethodParameter> getParameters()
        {
            if (parameters == null)
            {
                parameters = PlasticInternalUtils.newList();

                for (int i = 0; i < description.argumentTypes.length; i++)
                {
                    parameters.add(new MethodParameterImpl(node.visibleParameterAnnotations[i],
                            description.argumentTypes[i], i));
                }
            }

            return parameters;
        }

        private void rewriteMethodForAdvice()
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
            MethodNode mn = new MethodNode(ACC_SYNTHETIC | ACC_FINAL, name, node.desc, node.signature, null);
            // But it is safe enough for the two nodes to share
            mn.exceptions = node.exceptions;

            InstructionBuilder builder = newBuilder(mn);

            builder.loadThis();
            builder.loadArguments();
            builder.invokeSpecial(className, description);
            builder.returnResult();

            classNode.methods.add(mn);

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
                public void doBuild(InstructionBuilder builder)
                {
                    builder.startTryCatch(new TryCatchCallback()
                    {
                        public void doBlock(TryCatchBlock block)
                        {
                            block.addTry(new InstructionBuilderCallback()
                            {
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

                                    builder.invokeVirtual(className, description.returnType, accessMethodName,
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

            final TypeCategory typeCategory = pool.getTypeCategory(delegateType);

            if (typeCategory == TypeCategory.INTERFACE)
                builder.invokeInterface(delegateType, description.returnType, description.methodName,
                        description.argumentTypes);
            else
                builder.invokeVirtual(delegateType, description.returnType, description.methodName,
                        description.argumentTypes);

            builder.returnResult();
        }

    }

    private class PlasticFieldImpl extends PlasticMember implements PlasticField, Comparable<PlasticFieldImpl>
    {
        private final FieldNode node;

        private final String typeName;

        private Object tag;

        private FieldHandleImpl handle;

        // Names of methods to get or set the value of the field, invoked
        // from the generated FieldAccess object. With a FieldConduit,
        // these also represent the names of the methods that replace field access
        // in non-introduced methods

        private String getAccessName, setAccessName;

        private FieldState state = FieldState.INITIAL;

        private int fieldIndex = -1;

        public PlasticFieldImpl(FieldNode node)
        {
            super(node.visibleAnnotations);

            this.node = node;
            this.typeName = Type.getType(node.desc).getClassName();
        }

        public String toString()
        {
            return String.format("PlasticField[%s %s %s (in class %s)]", Modifier.toString(node.access), typeName,
                    node.name, className);
        }

        public int compareTo(PlasticFieldImpl o)
        {
            return this.node.name.compareTo(o.node.name);
        }

        public PlasticClass getPlasticClass()
        {
            check();

            return PlasticClassImpl.this;
        }

        public FieldHandle getHandle()
        {
            check();

            if (handle == null)
            {
                fieldIndex = nextFieldIndex++;

                // The shim gets assigned later

                handle = new FieldHandleImpl(className, node.name, fieldIndex);

                shimFields.add(this);
            }

            return handle;
        }

        public PlasticField claim(Object tag)
        {
            assert tag != null;

            check();

            if (this.tag != null)
                throw new IllegalStateException(String.format(
                        "Field %s of class %s can not be claimed by %s as it is already claimed by %s.", node.name,
                        className, tag, this.tag));

            this.tag = tag;

            // Force the list of unclaimed fields to be recomputed on next access

            unclaimedFields = null;

            return this;
        }

        public boolean isClaimed()
        {
            check();

            return tag != null;
        }

        public String getName()
        {
            check();

            return node.name;
        }

        public String getTypeName()
        {
            check();

            return typeName;
        }

        private void verifyInitialState(String operation)
        {
            if (state != FieldState.INITIAL)
                throw new IllegalStateException(String.format("Unable to %s field %s of class %s, as it already %s.",
                        operation, node.name, className, state.description));
        }

        public PlasticField inject(Object value)
        {
            check();

            verifyInitialState("inject a value into");

            assert value != null;

            initializeFieldFromStaticContext(node.name, typeName, value);

            makeReadOnly();

            state = FieldState.INJECTED;

            return this;
        }

        public PlasticField injectComputed(ComputedValue<?> computedValue)
        {
            check();

            verifyInitialState("inject a computed value into");

            assert computedValue != null;

            initializeComputedField(computedValue);

            makeReadOnly();

            state = FieldState.INJECTED;

            return this;
        }

        private void initializeComputedField(ComputedValue<?> computedValue)
        {
            int index = staticContext.store(computedValue);

            constructorBuilder.loadThis(); // for the putField()

            // Get the ComputedValue out of the StaticContext and onto the stack

            constructorBuilder.loadArgument(0).loadConstant(index);
            constructorBuilder.invoke(STATIC_CONTEXT_GET_METHOD).checkcast(ComputedValue.class);

            // Add the InstanceContext to the stack

            constructorBuilder.loadArgument(1);
            constructorBuilder.invoke(COMPUTED_VALUE_GET_METHOD).castOrUnbox(typeName);

            constructorBuilder.putField(className, node.name, typeName);
        }

        public PlasticField injectFromInstanceContext()
        {
            check();

            verifyInitialState("inject instance context value into");

            // Easiest to load this, for the putField(), early, in case the field is
            // wide (long or double primitive)

            constructorBuilder.loadThis();

            // Add the InstanceContext to the stack

            constructorBuilder.loadArgument(1);
            constructorBuilder.loadConstant(typeName);

            constructorBuilder.invokeStatic(PlasticInternalUtils.class, Object.class, "getFromInstanceContext",
                    InstanceContext.class, String.class).castOrUnbox(typeName);

            constructorBuilder.putField(className, node.name, typeName);

            makeReadOnly();

            state = FieldState.INJECTED;

            return this;
        }

        public PlasticField setConduit(FieldConduit<?> conduit)
        {
            assert conduit != null;

            check();

            verifyInitialState("set the FieldConduit for");

            // First step: define a field to store the conduit and add constructor logic
            // to initialize it

            String conduitFieldName = createAndInitializeFieldFromStaticContext(node.name + "_FieldConduit",
                    FieldConduit.class.getName(), conduit);

            replaceFieldReadAccess(conduitFieldName);
            replaceFieldWriteAccess(conduitFieldName);

            // TODO: Do we keep the field or not? It will now always be null/0/false.

            state = FieldState.CONDUIT;

            return null;
        }

        public PlasticField createAccessors(PropertyAccessType accessType)
        {
            check();

            return createAccessors(accessType, PlasticInternalUtils.toPropertyName(node.name));
        }

        public PlasticField createAccessors(PropertyAccessType accessType, String propertyName)
        {
            check();

            assert accessType != null;
            assert PlasticInternalUtils.isNonBlank(propertyName);

            String capitalized = PlasticInternalUtils.capitalize(propertyName);

            if (accessType != PropertyAccessType.WRITE_ONLY)
            {
                introduceMethod(new MethodDescription(getTypeName(), "get" + capitalized, null)).changeImplementation(
                        new InstructionBuilderCallback()
                        {
                            public void doBuild(InstructionBuilder builder)
                            {
                                builder.loadThis().getField(PlasticFieldImpl.this).returnResult();
                            }
                        });
            }

            if (accessType != PropertyAccessType.READ_ONLY)
            {
                introduceMethod(new MethodDescription("void", "set" + capitalized, getTypeName()))
                        .changeImplementation(new InstructionBuilderCallback()
                        {
                            public void doBuild(InstructionBuilder builder)
                            {
                                builder.loadThis().loadArgument(0);
                                builder.putField(className, node.name, getTypeName());
                                builder.returnResult();
                            }
                        });
            }

            return this;
        }

        private void replaceFieldWriteAccess(String conduitFieldName)
        {
            setAccessName = makeUnique(methodNames, "set_" + node.name);

            MethodNode mn = new MethodNode(ACC_SYNTHETIC | ACC_FINAL, setAccessName, "(" + node.desc + ")V", null, null);

            InstructionBuilder builder = newBuilder(mn);

            pushFieldConduitOntoStack(conduitFieldName, builder);
            pushInstanceContextFieldOntoStack(builder);

            // Take the value passed to this method and push it onto the stack.

            builder.loadArgument(0);
            builder.boxPrimitive(typeName);

            builder.invoke(FieldConduit.class, void.class, "set", InstanceContext.class, Object.class);

            builder.returnResult();

            addMethod(mn);

            fieldToWriteMethod.put(node.name, setAccessName);
        }

        private void replaceFieldReadAccess(String conduitFieldName)
        {
            getAccessName = makeUnique(methodNames, "getfieldvalue_" + node.name);

            MethodNode mn = new MethodNode(ACC_SYNTHETIC | ACC_FINAL, getAccessName, "()" + node.desc, null, null);

            InstructionBuilder builder = newBuilder(mn);

            // Get the correct FieldConduit object on the stack

            pushFieldConduitOntoStack(conduitFieldName, builder);

            // Now push the instance context on the stack

            pushInstanceContextFieldOntoStack(builder);

            builder.invoke(FieldConduit.class, Object.class, "get", InstanceContext.class).castOrUnbox(typeName);

            builder.returnResult();

            addMethod(mn);

            fieldToReadMethod.put(node.name, getAccessName);
        }

        private void pushFieldConduitOntoStack(String conduitFileName, InstructionBuilder builder)
        {
            builder.loadThis();
            builder.getField(className, conduitFileName, FieldConduit.class);
        }

        private void makeReadOnly()
        {
            setAccessName = makeUnique(methodNames, "setfieldvalue_" + node.name);

            MethodNode mn = new MethodNode(ACC_SYNTHETIC | ACC_FINAL, setAccessName, "(" + node.desc + ")V", null, null);

            String message = String.format("Field %s of class %s is read-only.", node.name, className);

            newBuilder(mn).throwException(IllegalStateException.class, message);

            addMethod(mn);

            fieldToWriteMethod.put(node.name, setAccessName);

            node.access |= ACC_FINAL;
        }

        /**
         * Adds a static setter method, allowing an external FieldAccess implementation
         * to directly set the value of the field.
         */
        private MethodNode addSetAccessMethod()
        {
            String name = makeUnique(methodNames, "directset_" + node.name);

            // Takes two Object parameters (instance, and value) and returns void.

            MethodNode mn = new MethodNode(ACC_SYNTHETIC | ACC_FINAL, name, "(" + node.desc + ")V", null, null);

            InstructionBuilder builder = newBuilder(mn);

            builder.loadThis().loadArgument(0).putField(className, node.name, typeName);
            builder.returnResult();

            addMethod(mn);

            return mn;
        }

        private MethodNode addGetAccessMethod()
        {
            String name = makeUnique(methodNames, "directget_" + node.name);

            MethodNode mn = new MethodNode(ACC_SYNTHETIC | ACC_FINAL, name, "()" + node.desc, null, null);

            InstructionBuilder builder = newBuilder(mn);

            builder.loadThis().getField(className, node.name, typeName).returnResult();

            addMethod(mn);

            return mn;
        }

        void installShim(PlasticClassHandleShim shim)
        {
            if (handle != null)
            {
                handle.shim = shim;
            }
        }

        /** Invoked with the object instance on the stack and cast to the right type. */
        void extendShimGet(SwitchBlock switchBlock)
        {
            String accessMethodName = getAccessName;

            if (accessMethodName == null)
            {
                MethodNode method = addGetAccessMethod();
                fieldTransformMethods.add(method);
                accessMethodName = method.name;
            }

            final String methodToInvoke = accessMethodName;

            switchBlock.addCase(fieldIndex, false, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    builder.invokeVirtual(className, typeName, methodToInvoke).boxPrimitive(typeName).returnResult();
                }
            });
        }

        /**
         * Invoked with the object instance on the stack and cast to the right type, then the
         * new field value (as Object, needing to be cast or unboxed).
         */
        void extendShimSet(SwitchBlock switchBlock)
        {
            String accessMethodName = setAccessName;

            // If no conduit has yet been specified, then we need a set access method for the shim to invoke.

            if (accessMethodName == null)
            {
                MethodNode method = addSetAccessMethod();
                fieldTransformMethods.add(method);
                accessMethodName = method.name;
            }

            final String methodToInvoke = accessMethodName;

            switchBlock.addCase(fieldIndex, true, new InstructionBuilderCallback()
            {

                public void doBuild(InstructionBuilder builder)
                {
                    builder.castOrUnbox(typeName);
                    builder.invokeVirtual(className, "void", methodToInvoke, typeName);
                    // Should not be necessary, as its always a void method, and we can
                    // drop to the bottom of the method.
                    // builder.returnResult();
                }
            });
        }
    }

    /**
     * Responsible for tracking the advice added to a method, as well as creating the MethodInvocation
     * class for the method and ultimately rewriting the original method to instnatiate the MethodInvocation
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

        private final List<MethodAdvice> advice = new ArrayList<MethodAdvice>();

        private final boolean isVoid;

        private final String invocationClassName;

        /** The new method that uses the original instructions from the advisedMethodNode. */
        private final String newMethodName;

        private final String[] constructorTypes;

        protected MethodAdviceManager(MethodDescription description, MethodNode methodNode)
        {
            this.description = description;
            this.advisedMethodNode = methodNode;

            isVoid = description.returnType.equals("void");

            invocationClassName = String.format("%s$Invocation_%s_%s", className, description.methodName,
                    PlasticUtils.nextUID());

            invocationClassNode = new ClassNode();

            invocationClassNode.visit(V1_5, ACC_PUBLIC | ACC_FINAL, nameCache.toInternalName(invocationClassName),
                    null, ABSTRACT_METHOD_INVOCATION_INTERNAL_NAME, new String[]
                    { nameCache.toInternalName(MethodInvocation.class) });

            constructorTypes = createFieldsAndConstructor();

            createReturnValueAccessors();

            createSetParameter();

            createGetParameter();

            newMethodName = String.format("advised$%s_%s", description.methodName, PlasticUtils.nextUID());

            createNewMethod();

            createProceedToAdvisedMethod();
        }

        private String[] createFieldsAndConstructor()
        {
            if (!isVoid)
                invocationClassNode.visitField(ACC_PUBLIC, RETURN_VALUE, nameCache.toDesc(description.returnType),
                        null, null);

            List<String> consTypes = new ArrayList<String>();
            consTypes.add(Object.class.getName());
            consTypes.add(InstanceContext.class.getName());
            consTypes.add(MethodInvocationBundle.class.getName());

            for (int i = 0; i < description.argumentTypes.length; i++)
            {
                String type = description.argumentTypes[i];

                invocationClassNode.visitField(ACC_PRIVATE, "p" + i, nameCache.toDesc(type), null, null);

                consTypes.add(type);
            }

            String[] constructorTypes = consTypes.toArray(new String[consTypes.size()]);

            MethodNode cons = new MethodNode(ACC_PUBLIC, CONSTRUCTOR_NAME, nameCache.toMethodDescriptor("void",
                    constructorTypes), null, null);

            InstructionBuilder builder = newBuilder(cons);

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
            MethodNode mn = new MethodNode(ACC_PUBLIC, name, nameCache.toMethodDescriptor(returnType, argumentTypes),
                    null, null);

            invocationClassNode.methods.add(mn);

            return newBuilder(mn);
        }

        private void createReturnValueGetter()
        {
            InstructionBuilder builder = newMethod("getReturnValue", Object.class);

            if (isVoid)
            {
                builder.loadNull().returnResult();
            }
            else
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
                                className));
            }
            else
            {
                builder.loadThis().loadArgument(0);
                builder.castOrUnbox(description.returnType);
                builder.putField(invocationClassName, RETURN_VALUE, description.returnType);

                builder.loadThis().returnResult();
            }
        }

        private void createGetParameter()
        {
            InstructionBuilder builder = newMethod("getParameter", Object.class, int.class);

            if (description.argumentTypes.length == 0)
            {
                indexOutOfRange(builder);
            }
            else
            {
                builder.loadArgument(0);
                builder.startSwitch(0, description.argumentTypes.length - 1, new SwitchCallback()
                {

                    public void doSwitch(SwitchBlock block)
                    {
                        for (int i = 0; i < description.argumentTypes.length; i++)
                        {
                            final int index = i;

                            block.addCase(i, false, new InstructionBuilderCallback()
                            {

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
            }
            else
            {
                builder.loadArgument(0).startSwitch(0, description.argumentTypes.length - 1, new SwitchCallback()
                {

                    public void doSwitch(SwitchBlock block)
                    {
                        for (int i = 0; i < description.argumentTypes.length; i++)
                        {
                            final int index = i;

                            block.addCase(i, true, new InstructionBuilderCallback()
                            {

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
            String[] exceptions = (String[]) (advisedMethodNode.exceptions == null ? null
                    : advisedMethodNode.exceptions.toArray(new String[0]));

            // Remove the private flag, so that the MethodInvocation implementation (in the same package)
            // can directly access the method without an additional access method.

            MethodNode mn = new MethodNode(advisedMethodNode.access & ~ACC_PRIVATE, newMethodName,
                    advisedMethodNode.desc, advisedMethodNode.signature, exceptions);

            // Copy everything else about the advisedMethodNode over to the new node

            advisedMethodNode.accept(mn);

            // Add this new method, with the same implementation as the original method, to the
            // PlasticClass

            classNode.methods.add(mn);
        }

        /** Invoke the "new" method, and deal with the return value and/or thrown exceptions. */
        private void createProceedToAdvisedMethod()
        {
            InstructionBuilder builder = newMethod("proceedToAdvisedMethod", void.class);

            if (!isVoid)
                builder.loadThis();

            builder.loadThis().invoke(AbstractMethodInvocation.class, Object.class, "getInstance").checkcast(className);

            // Load up each parameter
            for (int i = 0; i < description.argumentTypes.length; i++)
            {
                String type = description.argumentTypes[i];

                builder.loadThis().getField(invocationClassName, "p" + i, type);
            }

            builder.startTryCatch(new TryCatchCallback()
            {
                public void doBlock(TryCatchBlock block)
                {
                    block.addTry(new InstructionBuilderCallback()
                    {

                        public void doBuild(InstructionBuilder builder)
                        {
                            builder.invokeVirtual(className, description.returnType, newMethodName,
                                    description.argumentTypes);

                            if (!isVoid)
                                builder.putField(invocationClassName, RETURN_VALUE, description.returnType);

                            builder.returnResult();
                        }
                    });

                    for (String exceptionName : description.checkedExceptionTypes)
                    {
                        block.addCatch(exceptionName, new InstructionBuilderCallback()
                        {
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
            });
        }

        private void rewriteOriginalMethod()
        {
            pool.realize(invocationClassNode);

            String fieldName = String.format("methodinvocationbundle_%s_%s", description.methodName,
                    PlasticUtils.nextUID());

            MethodAdvice[] adviceArray = advice.toArray(new MethodAdvice[advice.size()]);
            MethodInvocationBundle bundle = new MethodInvocationBundle(description, adviceArray);

            classNode.visitField(ACC_PRIVATE | ACC_FINAL, fieldName, nameCache.toDesc(constructorTypes[2]), null, null);
            initializeFieldFromStaticContext(fieldName, constructorTypes[2], bundle);

            // Ok, here's the easy part: replace the method invocation with instantiating the invocation class

            advisedMethodNode.instructions.clear();
            advisedMethodNode.tryCatchBlocks.clear();

            if (advisedMethodNode.localVariables != null)
                advisedMethodNode.localVariables.clear();

            InstructionBuilder builder = newBuilder(description, advisedMethodNode);

            builder.newInstance(invocationClassName).dupe();

            // Now load up the parameters to the constructor

            builder.loadThis();
            builder.loadThis().getField(className, getInstanceContextFieldName(), constructorTypes[1]);
            builder.loadThis().getField(className, fieldName, constructorTypes[2]);

            // Load up the actual method parameters

            builder.loadArguments();
            builder.invokeConstructor(invocationClassName, constructorTypes);

            // That leaves an instance of the invocation class on the stack. If the method is void
            // and throws no checked exceptions, then the variable actually isn't used. This code
            // should be refactored a bit once there are tests for those cases.

            builder.startVariable("invocation", invocationClassName, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    builder.dupe().storeVariable("invocation");

                    builder.invoke(AbstractMethodInvocation.class, MethodInvocation.class, "proceed");

                    if (description.checkedExceptionTypes.length > 0)
                    {
                        builder.invoke(MethodInvocation.class, boolean.class, "didThrowCheckedException");

                        builder.when(Condition.NON_ZERO, new InstructionBuilderCallback()
                        {
                            public void doBuild(InstructionBuilder builder)
                            {
                                builder.loadVariable("invocation").loadTypeConstant(Exception.class);
                                builder.invokeVirtual(invocationClassName, Throwable.class.getName(),
                                        "getCheckedException", Class.class.getName());
                                builder.throwException();
                            }
                        });
                    }

                    if (!isVoid)
                        builder.loadVariable("invocation").getField(invocationClassName, RETURN_VALUE,
                                description.returnType);

                    builder.returnResult();
                }
            });
        }
    }

    // Now past the inner classes; these are the instance variables of PlasticClassImpl proper:

    private final ClassNode classNode;

    private final PlasticClassPool pool;

    private final String className;

    private final String superClassName;

    private final AnnotationAccess annotationAccess;

    // All the non-introduced (and non-constructor) methods, in sorted order

    private final List<PlasticMethodImpl> methods;

    private final Map<MethodDescription, PlasticMethod> description2method = new HashMap<MethodDescription, PlasticMethod>();

    private final Set<String> methodNames = new HashSet<String>();

    // All non-introduced instance fields

    private final List<PlasticFieldImpl> fields;

    /**
     * Methods that require special attention inside {@link #createInstantiator()} because they
     * have method advice.
     */
    private final Set<PlasticMethodImpl> advisedMethods = PlasticInternalUtils.newSet();;

    private final NameCache nameCache = new NameCache();

    // This is generated from fields, as necessary
    private List<PlasticField> unclaimedFields;

    private final Set<String> fieldNames = PlasticInternalUtils.newSet();

    private final StaticContext staticContext;

    private final MethodBundle methodBundle;

    // MethodNodes in which field transformations should occur; this is most existing and
    // introduced methods, outside of special access methods.

    private final Set<MethodNode> fieldTransformMethods = PlasticInternalUtils.newSet();

    /**
     * Maps a field name to a replacement method that should be invoked instead of reading the
     * field.
     */
    private final Map<String, String> fieldToReadMethod = new HashMap<String, String>();

    /**
     * Maps a field name to a replacement method that should be invoked instead of writing the
     * field.
     */
    private final Map<String, String> fieldToWriteMethod = new HashMap<String, String>();

    /**
     * This normal no-arguments constructor, or null. By the end of the transformation
     * this will be converted into an ordinary method.
     */
    private MethodNode originalConstructor;

    private final MethodNode newConstructor;

    private final InstructionBuilder constructorBuilder;

    private String instanceContextFieldName;

    private Class<?> transformedClass;

    // Indexes used to identify fields or methods in the shim
    private int nextFieldIndex = 0;

    private int nextMethodIndex = 0;

    // Set of fields that need to contribute to the shim and gain access to it

    private final Set<PlasticFieldImpl> shimFields = PlasticInternalUtils.newSet();

    // Set of methods that need to contribute to the shim and gain access to it

    private final Set<PlasticMethodImpl> shimMethods = PlasticInternalUtils.newSet();

    /**
     * @param classNode
     * @param pool
     * @param parentMethodBundle
     * @param parentStaticContext
     */
    public PlasticClassImpl(ClassNode classNode, PlasticClassPool pool, MethodBundle parentMethodBundle,
            StaticContext parentStaticContext)
    {
        this.classNode = classNode;
        this.pool = pool;

        staticContext = parentStaticContext.dupe();

        annotationAccess = pool.createAnnotationAccess(classNode.visibleAnnotations);

        className = PlasticInternalUtils.toClassName(classNode.name);
        superClassName = PlasticInternalUtils.toClassName(classNode.superName);

        methodBundle = parentMethodBundle.createChild(className);

        methods = new ArrayList(classNode.methods.size());

        String invalidConstructorMessage = String.format(
                "Class %s has been transformed and may not be directly instantiated.", className);

        for (MethodNode node : (List<MethodNode>) classNode.methods)
        {
            if (node.name.equals(CONSTRUCTOR_NAME))
            {
                if (node.desc.equals(NOTHING_TO_VOID))
                {
                    originalConstructor = node;
                    fieldTransformMethods.add(node);
                }
                else
                {
                    node.instructions.clear();

                    newBuilder(node).throwException(IllegalStateException.class, invalidConstructorMessage);
                }

                continue;
            }

            if (Modifier.isStatic(node.access))
                continue;

            if (!Modifier.isAbstract(node.access))
                fieldTransformMethods.add(node);

            PlasticMethodImpl pmi = new PlasticMethodImpl(node);

            methods.add(pmi);
            description2method.put(pmi.getDescription(), pmi);

            if (isInheritableMethod(node))
                methodBundle.addMethod(node.name, node.desc);

            methodNames.add(node.name);
        }

        Collections.sort(methods);

        fields = new ArrayList(classNode.fields.size());

        for (FieldNode node : (List<FieldNode>) classNode.fields)
        {
            fieldNames.add(node.name);

            // Ignore static fields.

            if (Modifier.isStatic(node.access))
                continue;

            // TODO: Perhaps we should defer this private check until it is needed,
            // i.e., when we do some work on the field that requires it to be private?
            // However, given class loading issues, public fields are likely to cause
            // their own problems when passing the ClassLoader boundrary.

            if (!Modifier.isPrivate(node.access))
                throw new IllegalArgumentException(
                        String.format(
                                "Field %s of class %s is not private. Class transformation requires that all instance fields be private.",
                                node.name, className));

            fields.add(new PlasticFieldImpl(node));
        }

        Collections.sort(fields);

        // TODO: Make the output class's constructor protected, and create a shim class to instantiate it
        // efficiently (without reflection).
        newConstructor = new MethodNode(ACC_PUBLIC, CONSTRUCTOR_NAME, CONSTRUCTOR_DESC, null, null);
        constructorBuilder = newBuilder(newConstructor);

        // Start by calling the super-class no args constructor

        if (parentMethodBundle.isTransformed())
        {
            // If the parent is transformed, our first step is always to invoke its constructor.

            constructorBuilder.loadThis().loadArgument(0).loadArgument(1);
            constructorBuilder.invokeConstructor(superClassName, StaticContext.class.getName(),
                    InstanceContext.class.getName());
        }
        else
        {
            // Assumes the base class includes a visible constructor that takes no arguments.
            // TODO: Do a proper check for this case and throw a meaningful exception
            // if not present.

            constructorBuilder.loadThis().invokeConstructor(superClassName);
        }

        // During the transformation, we'll be adding code to the constructor to pull values
        // out of the static or instance context and assign them to fields.

        // Later on, we'll add the RETURN opcode
    }

    public <T extends Annotation> boolean hasAnnotation(Class<T> annotationType)
    {
        check();

        return annotationAccess.hasAnnotation(annotationType);
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType)
    {
        check();

        return annotationAccess.getAnnotation(annotationType);
    }

    public PlasticClass proxyInterface(Class interfaceType, PlasticField field)
    {
        check();

        assert field != null;

        introduceInterface(interfaceType);

        for (Method m : interfaceType.getMethods())
        {
            introduceMethod(m).delegateTo(field);
        }

        return this;
    }

    public ClassInstantiator createInstantiator()
    {
        lock();

        interceptFieldAccess();

        createShimIfNeeded();

        rewriteAdvisedMethods();

        completeConstructor();

        transformedClass = pool.realizeTransformedClass(classNode, methodBundle, staticContext);

        return createInstantiatorFromClass(transformedClass);
    }

    private ClassInstantiator createInstantiatorFromClass(Class clazz)
    {
        try
        {
            Constructor ctor = clazz.getConstructor(StaticContext.class, InstanceContext.class);

            return new ClassInstantiatorImpl(clazz, ctor, staticContext);
        }
        catch (Exception ex)
        {
            throw new RuntimeException(String.format("Unable to create ClassInstantiator for class %s: %s",
                    clazz.getName(), PlasticInternalUtils.toMessage(ex)), ex);
        }
    }

    private void completeConstructor()
    {
        if (originalConstructor != null)
        {
            // Convert the original constructor into a private method invoked from the
            // generated constructor.

            String initializerName = makeUnique(methodNames, "initializeInstance");

            originalConstructor.access = ACC_PRIVATE;
            originalConstructor.name = initializerName;

            stripOutSuperConstructorCall(originalConstructor);

            constructorBuilder.loadThis().invokeVirtual(className, "void", initializerName);
        }

        constructorBuilder.returnResult();

        classNode.methods.add(newConstructor);
    }

    private void stripOutSuperConstructorCall(MethodNode cons)
    {
        InsnList ins = cons.instructions;

        ListIterator li = ins.iterator();

        // Look for the ALOAD 0 (i.e., push this on the stack)
        while (li.hasNext())
        {
            AbstractInsnNode node = (AbstractInsnNode) li.next();

            if (node.getOpcode() == ALOAD)
            {
                VarInsnNode varNode = (VarInsnNode) node;

                assert varNode.var == 0;

                // Remove the ALOAD
                li.remove();
                break;
            }
        }

        // Look for the call to the super-class, an INVOKESPECIAL
        while (li.hasNext())
        {
            AbstractInsnNode node = (AbstractInsnNode) li.next();

            if (node.getOpcode() == INVOKESPECIAL)
            {
                MethodInsnNode mnode = (MethodInsnNode) node;

                assert mnode.owner.equals(classNode.superName);
                assert mnode.name.equals(CONSTRUCTOR_NAME);
                assert mnode.desc.equals(cons.desc);

                li.remove();
                return;
            }
        }

        throw new AssertionError("Could not convert constructor to simple method.");
    }

    public <T extends Annotation> List<PlasticField> getFieldsWithAnnotation(Class<T> annotationType)
    {
        check();

        List<PlasticField> result = getUnclaimedFields();

        Iterator<PlasticField> iterator = result.iterator();

        while (iterator.hasNext())
        {
            PlasticField plasticField = iterator.next();

            if (!plasticField.hasAnnotation(annotationType))
                iterator.remove();
        }

        return result;
    }

    public List<PlasticField> getAllFields()
    {
        check();

        return new ArrayList<PlasticField>(fields);
    }

    public List<PlasticField> getUnclaimedFields()
    {
        check();

        // Initially null, and set back to null by PlasticField.claim().

        if (unclaimedFields == null)
        {
            unclaimedFields = new ArrayList<PlasticField>(fields.size());

            for (PlasticField f : fields)
            {
                if (!f.isClaimed())
                    unclaimedFields.add(f);
            }
        }

        return unclaimedFields;
    }

    public PlasticMethod introducePrivateMethod(String typeName, String suggestedName, String[] argumentTypes,
            String[] exceptionTypes)
    {
        check();

        assert PlasticInternalUtils.isNonBlank(typeName);
        assert PlasticInternalUtils.isNonBlank(suggestedName);

        String name = makeUnique(methodNames, suggestedName);

        MethodDescription description = new MethodDescription(Modifier.PRIVATE, typeName, name, argumentTypes,
                exceptionTypes);

        return introduceMethod(description);
    }

    public PlasticField introduceField(String className, String suggestedName)
    {
        check();

        assert PlasticInternalUtils.isNonBlank(className);
        assert PlasticInternalUtils.isNonBlank(suggestedName);

        String name = makeUnique(fieldNames, suggestedName);

        // No signature and no initial value

        FieldNode fieldNode = new FieldNode(ACC_PRIVATE, name, PlasticInternalUtils.toDescriptor(className), null, null);

        classNode.fields.add(fieldNode);

        fieldNames.add(name);

        PlasticFieldImpl newField = new PlasticFieldImpl(fieldNode);

        return newField;
    }

    public PlasticField introduceField(Class fieldType, String suggestedName)
    {
        assert fieldType != null;

        return introduceField(fieldType.getName(), suggestedName);
    }

    private String makeUnique(Set<String> values, String input)
    {
        return values.contains(input) ? input + "$" + PlasticUtils.nextUID() : input;
    }

    public <T extends Annotation> List<PlasticMethod> getMethodsWithAnnotation(Class<T> annotationType)
    {
        check();

        List<PlasticMethod> result = getMethods();
        Iterator<PlasticMethod> iterator = result.iterator();

        while (iterator.hasNext())
        {
            PlasticMethod method = iterator.next();

            if (!method.hasAnnotation(annotationType))
                iterator.remove();
        }

        return result;
    }

    public List<PlasticMethod> getMethods()
    {
        check();

        return new ArrayList<PlasticMethod>(methods);
    }

    public PlasticMethod introduceMethod(MethodDescription description)
    {
        check();

        if (Modifier.isAbstract(description.modifiers))
        {
            description = description.withModifiers(description.modifiers & ~ACC_ABSTRACT);
        }

        PlasticMethod result = description2method.get(description);

        if (result == null)
        {
            result = createNewMethod(description);

            description2method.put(description, result);
        }

        methodNames.add(description.methodName);

        // Note that is it not necessary to add the new MethodNode to
        // fieldTransformMethods (the default implementations provided by introduceMethod() do not
        // ever access instance fields) ... unless the caller invokes changeImplementation().

        return result;
    }

    public PlasticMethod introduceMethod(MethodDescription description, InstructionBuilderCallback callback)
    {
        check();

        // TODO: optimize this so that a default implementation is not created.

        return introduceMethod(description).changeImplementation(callback);
    }

    public PlasticMethod introduceMethod(Method method)
    {
        check();

        return introduceMethod(new MethodDescription(method));
    }

    private void addMethod(MethodNode methodNode)
    {
        classNode.methods.add(methodNode);

        methodNames.add(methodNode.name);
    }

    private PlasticMethod createNewMethod(MethodDescription description)
    {
        if (Modifier.isStatic(description.modifiers))
            throw new IllegalArgumentException(String.format(
                    "Unable to introduce method '%s' into class %s: introduced methods may not be static.",
                    description, className));

        String desc = nameCache.toDesc(description);

        String[] exceptions = new String[description.checkedExceptionTypes.length];
        for (int i = 0; i < exceptions.length; i++)
        {
            exceptions[i] = PlasticInternalUtils.toInternalName(description.checkedExceptionTypes[i]);
        }

        MethodNode methodNode = new MethodNode(description.modifiers, description.methodName, desc, null, exceptions);
        boolean isOverride = methodBundle.isImplemented(methodNode.name, desc);

        if (isOverride)
            createOverrideOfBaseClassImpl(description, methodNode);
        else
            createNewMethodImpl(description, methodNode);

        classNode.methods.add(methodNode);

        if (!Modifier.isPrivate(description.modifiers))
            methodBundle.addMethod(description.methodName, desc);

        return new PlasticMethodImpl(methodNode);
    }

    private void createNewMethodImpl(MethodDescription methodDescription, MethodNode methodNode)
    {
        newBuilder(methodDescription, methodNode).returnDefaultValue();
    }

    private void createOverrideOfBaseClassImpl(MethodDescription methodDescription, MethodNode methodNode)
    {
        InstructionBuilder builder = newBuilder(methodDescription, methodNode);

        builder.loadThis();
        builder.loadArguments();
        builder.invokeSpecial(superClassName, methodDescription);
        builder.returnResult();
    }

    /**
     * Iterates over all non-introduced methods, including the original constructor. For each
     * method, the bytecode is
     * scanned for field reads and writes. When a match is found against an intercepted field, the
     * operation is
     * replaced with a method invocation.
     */
    private void interceptFieldAccess()
    {
        for (MethodNode node : fieldTransformMethods)
        {
            interceptFieldAccess(node);
        }
    }

    /**
     * Determines if any fields or methods have provided FieldHandles or MethodHandles; if so
     * a shim class must be created to facilitate read/write access to fields, or invocation of methods.
     */
    private void createShimIfNeeded()
    {
        if (shimFields.isEmpty() && shimMethods.isEmpty())
            return;

        PlasticClassHandleShim shim = createShimInstance();

        installShim(shim);
    }

    public void installShim(PlasticClassHandleShim shim)
    {
        for (PlasticFieldImpl f : shimFields)
        {
            f.installShim(shim);
        }

        for (PlasticMethodImpl m : shimMethods)
        {
            m.installShim(shim);
        }
    }

    public PlasticClassHandleShim createShimInstance()
    {
        String shimClassName = String.format("%s$Shim_%s", classNode.name, PlasticUtils.nextUID());

        ClassNode shimClassNode = new ClassNode();

        shimClassNode.visit(V1_5, ACC_PUBLIC | ACC_FINAL, shimClassName, null, HANDLE_SHIM_BASE_CLASS_INTERNAL_NAME,
                null);

        implementConstructor(shimClassNode);

        if (!shimFields.isEmpty())
        {
            implementShimGet(shimClassNode);
            implementShimSet(shimClassNode);
        }

        if (!shimMethods.isEmpty())
        {
            implementShimInvoke(shimClassNode);
        }

        return instantiateShim(shimClassNode);
    }

    private void implementConstructor(ClassNode shimClassNode)
    {
        MethodNode mn = new MethodNode(ACC_PUBLIC, CONSTRUCTOR_NAME, NOTHING_TO_VOID, null, null);

        InstructionBuilder builder = newBuilder(mn);

        builder.loadThis().invokeConstructor(PlasticClassHandleShim.class).returnResult();

        shimClassNode.methods.add(mn);

    }

    private PlasticClassHandleShim instantiateShim(ClassNode shimClassNode)
    {
        Class shimClass = pool.realize(shimClassNode);

        try
        {
            return (PlasticClassHandleShim) shimClass.newInstance();
        }
        catch (Exception ex)
        {
            throw new RuntimeException(
                    String.format("Unable to instantiate shim class %s for plastic class %s: %s",
                            PlasticInternalUtils.toClassName(shimClassNode.name), className,
                            PlasticInternalUtils.toMessage(ex)), ex);
        }
    }

    private void implementShimGet(ClassNode shimClassNode)
    {
        MethodNode mn = new MethodNode(ACC_PUBLIC, "get", OBJECT_INT_TO_OBJECT, null, null);

        InstructionBuilder builder = newBuilder(mn);

        // Arg 0 is the target instance
        // Arg 1 is the index

        builder.loadArgument(0).checkcast(className);
        builder.loadArgument(1);

        builder.startSwitch(0, nextFieldIndex - 1, new SwitchCallback()
        {
            public void doSwitch(SwitchBlock block)
            {
                for (PlasticFieldImpl f : shimFields)
                {
                    f.extendShimGet(block);
                }
            }
        });

        shimClassNode.methods.add(mn);
    }

    private void implementShimSet(ClassNode shimClassNode)
    {
        MethodNode mn = new MethodNode(ACC_PUBLIC, "set", OBJECT_INT_OBJECT_TO_VOID, null, null);

        InstructionBuilder builder = newBuilder(mn);

        // Arg 0 is the target instance
        // Arg 1 is the index
        // Arg 2 is the new value

        builder.loadArgument(0).checkcast(className);
        builder.loadArgument(2);

        builder.loadArgument(1);

        builder.startSwitch(0, nextFieldIndex - 1, new SwitchCallback()
        {
            public void doSwitch(SwitchBlock block)
            {
                for (PlasticFieldImpl f : shimFields)
                {
                    f.extendShimSet(block);
                }
            }
        });

        builder.returnResult();

        shimClassNode.methods.add(mn);
    }

    private void implementShimInvoke(ClassNode shimClassNode)
    {
        MethodNode mn = new MethodNode(ACC_PUBLIC, "invoke", OBJECT_INT_OBJECT_ARRAY_TO_METHOD_INVOCATION_RESULT, null,
                null);

        InstructionBuilder builder = newBuilder(mn);

        // Arg 0 is the target instance
        // Arg 1 is the index
        // Arg 2 is the object array of parameters

        builder.loadArgument(0).checkcast(className);

        builder.loadArgument(1);

        builder.startSwitch(0, nextMethodIndex - 1, new SwitchCallback()
        {
            public void doSwitch(SwitchBlock block)
            {
                for (PlasticMethodImpl m : shimMethods)
                {
                    m.extendShimInvoke(block);
                }
            }
        });

        shimClassNode.methods.add(mn);
    }

    private void rewriteAdvisedMethods()
    {
        for (PlasticMethodImpl method : advisedMethods)
        {
            method.rewriteMethodForAdvice();
        }
    }

    private void interceptFieldAccess(MethodNode methodNode)
    {
        InsnList insns = methodNode.instructions;

        ListIterator it = insns.iterator();

        while (it.hasNext())
        {
            AbstractInsnNode node = (AbstractInsnNode) it.next();

            int opcode = node.getOpcode();

            if (opcode != GETFIELD && opcode != PUTFIELD)
                continue;

            // Make sure we're talking about access to a field of this class, not some other
            // visible field of another class.

            FieldInsnNode fnode = (FieldInsnNode) node;

            if (!fnode.owner.equals(classNode.name))
                continue;

            Map<String, String> fieldToMethod = opcode == GETFIELD ? fieldToReadMethod : fieldToWriteMethod;

            String methodName = fieldToMethod.get(fnode.name);

            if (methodName == null)
                continue;

            String methodDescription = opcode == GETFIELD ? "()" + fnode.desc : "(" + fnode.desc + ")V";

            // Replace the field access node with the appropriate method invocation.

            insns.insertBefore(fnode, new MethodInsnNode(INVOKEVIRTUAL, fnode.owner, methodName, methodDescription));

            it.remove();
        }
    }

    private String getInstanceContextFieldName()
    {
        if (instanceContextFieldName == null)
        {
            instanceContextFieldName = makeUnique(fieldNames, "instanceContext");

            // TODO: Once we support inheritance, we could use a protected field and only initialize
            // it once, in the first base class where it is needed.

            FieldNode node = new FieldNode(ACC_PRIVATE | ACC_FINAL, instanceContextFieldName, INSTANCE_CONTEXT_DESC,
                    null, null);

            classNode.fields.add(node);

            // Extend the constructor to store the context in a field.

            constructorBuilder.loadThis().loadArgument(1)
                    .putField(className, instanceContextFieldName, InstanceContext.class);
        }

        return instanceContextFieldName;
    }

    /** Creates a new private final field and initializes its value (using the StaticContext). */
    private String createAndInitializeFieldFromStaticContext(String suggestedFieldName, String fieldType,
            Object injectedFieldValue)
    {
        String name = makeUnique(fieldNames, suggestedFieldName);

        FieldNode field = new FieldNode(ACC_PRIVATE | ACC_FINAL, name, nameCache.toDesc(fieldType), null, null);

        classNode.fields.add(field);

        initializeFieldFromStaticContext(name, fieldType, injectedFieldValue);

        return name;
    }

    /**
     * Initializes a field from the static context. The injected value is added to the static
     * context and the class constructor updated to assign the value from the context (which includes casting and
     * possibly unboxing).
     */
    private void initializeFieldFromStaticContext(String fieldName, String fieldType, Object injectedFieldValue)
    {
        int index = staticContext.store(injectedFieldValue);

        // Although it feels nicer to do the loadThis() later and then swap() that breaks
        // on primitive longs and doubles, so its just easier to do the loadThis() first
        // so its at the right place on the stack for the putField().

        constructorBuilder.loadThis();

        constructorBuilder.loadArgument(0).loadConstant(index);
        constructorBuilder.invoke(STATIC_CONTEXT_GET_METHOD);
        constructorBuilder.castOrUnbox(fieldType);

        constructorBuilder.putField(className, fieldName, fieldType);
    }

    private void pushInstanceContextFieldOntoStack(InstructionBuilder builder)
    {
        builder.loadThis().getField(className, getInstanceContextFieldName(), InstanceContext.class);
    }

    public PlasticClass getPlasticClass()
    {
        return this;
    }

    public Class<?> getTransformedClass()
    {
        if (transformedClass == null)
            throw new IllegalStateException(String.format(
                    "Transformed class %s is not yet available because the transformation is not yet complete.",
                    className));

        return transformedClass;
    }

    private boolean isInheritableMethod(MethodNode node)
    {
        return (node.access & (ACC_ABSTRACT | ACC_PRIVATE)) == 0;
    }

    public String getClassName()
    {
        return className;
    }

    private InstructionBuilderImpl newBuilder(MethodNode mn)
    {
        return newBuilder(PlasticInternalUtils.toMethodDescription(mn), mn);
    }

    private InstructionBuilderImpl newBuilder(MethodDescription description, MethodNode mn)
    {
        return new InstructionBuilderImpl(description, mn, nameCache);
    }

    public Set<PlasticMethod> introduceInterface(Class interfaceType)
    {
        check();

        assert interfaceType != null;

        if (!interfaceType.isInterface())
            throw new IllegalArgumentException(String.format(
                    "Class %s is not an interface; ony interfaces may be introduced.", interfaceType.getName()));

        String interfaceName = nameCache.toInternalName(interfaceType);

        // I suppose this means that a subclass may restate that it implements an interface from a base class.

        if (!classNode.interfaces.contains(interfaceName))
            classNode.interfaces.add(interfaceName);

        Set<PlasticMethod> introducedMethods = new HashSet<PlasticMethod>();

        for (Method m : interfaceType.getMethods())
        {
            MethodDescription description = new MethodDescription(m);

            if (!isMethodImplemented(description))
            {
                introducedMethods.add(introduceMethod(m));
            }
        }

        return introducedMethods;
    }

    public PlasticClass addToString(final String toStringValue)
    {
        check();

        if (!isMethodImplemented(TO_STRING_METHOD_DESCRIPTION))
        {
            introduceMethod(TO_STRING_METHOD_DESCRIPTION, new InstructionBuilderCallback()
            {
                public void doBuild(InstructionBuilder builder)
                {
                    builder.loadConstant(toStringValue).returnResult();
                }
            });
        }

        return this;
    }

    /**
     * Returns true if this class has an implementation of the indicated method, or a super-class provides
     * a non-abstract implementation.
     */
    private boolean isMethodImplemented(MethodDescription description)
    {
        return methodBundle.isImplemented(description.methodName, nameCache.toDesc(description));
    }

    public PlasticClass copyAnnotations(String sourceClassName)
    {
        assert PlasticInternalUtils.isNonBlank(sourceClassName);

        ClassNode sourceClass = pool.constructClassNode(sourceClassName);

        classNode.visibleAnnotations = sourceClass.visibleAnnotations;

        Map<String, MethodNode> sourceMethods = buildMethodNodeMap(sourceClass, true);

        if (sourceMethods.isEmpty())
            return this;

        Map<String, MethodNode> targetMethods = buildMethodNodeMap(classNode, false);

        for (Map.Entry<String, MethodNode> entry : sourceMethods.entrySet())
        {
            MethodNode target = targetMethods.get(entry.getKey());

            // Not all source methods (especially private ones) will be in the target class,
            // which is typically a proxy, implementing just public methods defined in an interface.

            if (target == null)
                continue;

            MethodNode source = entry.getValue();

            target.visibleAnnotations = source.visibleAnnotations;
            target.visibleParameterAnnotations = source.visibleParameterAnnotations;
        }

        return this;
    }

    private static Map<String, MethodNode> buildMethodNodeMap(ClassNode source, boolean withAnnotationsOnly)
    {
        boolean all = !withAnnotationsOnly;

        Map<String, MethodNode> result = new HashMap<String, MethodNode>();

        for (Object m : source.methods)
        {
            MethodNode mn = (MethodNode) m;

            if (mn.name.equals(CONSTRUCTOR_NAME))
                continue;

            if (all || hasAnnotations(mn))
                result.put(mn.name + ":" + mn.desc, mn);
        }

        return result;
    }

    /**
     * True if the node has any visible annotations, or it has visible annotations on any
     * parameter.
     * 
     * @param mn
     * @return true if any annotations present
     */
    private static boolean hasAnnotations(MethodNode mn)
    {
        if (nonEmpty(mn.visibleAnnotations))
            return true;

        if (mn.visibleParameterAnnotations != null)
        {
            for (List pa : mn.visibleParameterAnnotations)
            {
                if (nonEmpty(pa))
                    return true;
            }
        }

        return false;
    }

    private static boolean nonEmpty(List l)
    {
        return l != null && !l.isEmpty();
    }

    public PlasticClass copyAnnotations(Class sourceClass)
    {
        assert sourceClass != null;

        return copyAnnotations(sourceClass.getName());
    }

}
