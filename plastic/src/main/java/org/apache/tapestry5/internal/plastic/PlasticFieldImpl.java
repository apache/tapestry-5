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

import org.apache.tapestry5.internal.plastic.asm.Opcodes;
import org.apache.tapestry5.internal.plastic.asm.Type;
import org.apache.tapestry5.internal.plastic.asm.tree.FieldNode;
import org.apache.tapestry5.internal.plastic.asm.tree.MethodNode;
import org.apache.tapestry5.plastic.*;

import java.lang.reflect.Modifier;

class PlasticFieldImpl extends PlasticMember implements PlasticField, Comparable<PlasticFieldImpl>
{
    private final FieldNode node;

    private final String typeName;

    private Object tag;

    private FieldHandleImpl handle;

    // Names of methods to get or set the value of the field, invoked
    // from the generated FieldAccess object. With a FieldConduit,
    // these also represent the names of the methods that replace field access
    // in non-introduced methods

    private MethodNode getAccess, setAccess;

    private FieldState state = FieldState.INITIAL;

    private int fieldIndex = -1;

    public PlasticFieldImpl(PlasticClassImpl plasticClass, FieldNode node)
    {
        super(plasticClass, node.visibleAnnotations);

        this.node = node;
        this.typeName = Type.getType(node.desc).getClassName();
    }

    @Override
    public String toString()
    {
        return String.format("PlasticField[%s %s %s (in class %s)]", Modifier.toString(node.access), typeName,
                node.name, plasticClass.className);
    }

    @Override
    public String getGenericSignature()
    {
        return node.signature;
    }

    @Override
    public int getModifiers()
    {
        return node.access;
    }

    @Override
    public int compareTo(PlasticFieldImpl o)
    {
        return this.node.name.compareTo(o.node.name);
    }

    @Override
    public PlasticClass getPlasticClass()
    {
        plasticClass.check();

        return plasticClass;
    }

    @Override
    public FieldHandle getHandle()
    {
        plasticClass.check();

        if (handle == null)
        {
            fieldIndex = plasticClass.nextFieldIndex++;

            // The shim gets assigned later

            handle = new FieldHandleImpl(plasticClass.className, node.name, fieldIndex);

            plasticClass.shimFields.add(this);
        }

        return handle;
    }

    @Override
    public PlasticField claim(Object tag)
    {
        assert tag != null;

        plasticClass.check();

        if (this.tag != null)
        {
            throw new IllegalStateException(String.format(
                    "Field %s of class %s can not be claimed by %s as it is already claimed by %s.", node.name,
                    plasticClass.className, tag, this.tag));
        }

        this.tag = tag;

        // Force the list of unclaimed fields to be recomputed on next access

        plasticClass.unclaimedFields = null;

        return this;
    }

    @Override
    public boolean isClaimed()
    {
        plasticClass.check();

        return tag != null;
    }

    @Override
    public String getName()
    {
        plasticClass.check();

        return node.name;
    }

    @Override
    public String getTypeName()
    {
        plasticClass.check();

        return typeName;
    }

    private void verifyInitialState(String operation)
    {
        if (state != FieldState.INITIAL)
        {
            throw new IllegalStateException(String.format("Unable to %s field %s of class %s, as it already %s.",
                    operation, node.name, plasticClass.className, state.description));
        }
    }

    private void ensureNotPublic()
    {
        if (Modifier.isPublic(node.access))
        {
            throw new IllegalArgumentException(String.format("Field %s of class %s must be instrumented, and may not be public.", node.name, plasticClass.className));
        }
    }

    @Override
    public PlasticField inject(Object value)
    {
        plasticClass.check();

        verifyInitialState("inject a value into");

        assert value != null;

        plasticClass.initializeFieldFromStaticContext(node.name, typeName, value);

        makeReadOnly();

        state = FieldState.INJECTED;

        return this;
    }

    @Override
    public PlasticField injectComputed(ComputedValue<?> computedValue)
    {
        plasticClass.check();

        verifyInitialState("inject a computed value into");

        assert computedValue != null;

        initializeComputedField(computedValue);

        makeReadOnly();

        state = FieldState.INJECTED;

        return this;
    }

    private void initializeComputedField(ComputedValue<?> computedValue)
    {
        int index = plasticClass.staticContext.store(computedValue);

        plasticClass.constructorBuilder.loadThis(); // for the putField()

        // Get the ComputedValue out of the StaticContext and onto the stack

        plasticClass.constructorBuilder.loadArgument(0).loadConstant(index);
        plasticClass.constructorBuilder.invoke(PlasticClassImpl.STATIC_CONTEXT_GET_METHOD).checkcast(ComputedValue.class);

        // Add the InstanceContext to the stack

        plasticClass.constructorBuilder.loadArgument(1);
        plasticClass.constructorBuilder.invoke(PlasticClassImpl.COMPUTED_VALUE_GET_METHOD).castOrUnbox(typeName);

        plasticClass.constructorBuilder.putField(plasticClass.className, node.name, typeName);
    }

    @Override
    public PlasticField injectFromInstanceContext()
    {
        plasticClass.check();

        verifyInitialState("inject instance context value into");

        // Easiest to load this, for the putField(), early, in case the field is
        // wide (long or double primitive)

        plasticClass.constructorBuilder.loadThis();

        // Add the InstanceContext to the stack

        plasticClass.constructorBuilder.loadArgument(1);
        plasticClass.constructorBuilder.loadConstant(typeName);

        plasticClass.constructorBuilder.invokeStatic(PlasticInternalUtils.class, Object.class, "getFromInstanceContext",
                InstanceContext.class, String.class).castOrUnbox(typeName);

        plasticClass.constructorBuilder.putField(plasticClass.className, node.name, typeName);

        makeReadOnly();

        state = FieldState.INJECTED;

        return this;
    }

    @Override
    public <F> PlasticField setConduit(FieldConduit<F> conduit)
    {
        assert conduit != null;

        plasticClass.check();

        verifyInitialState("set the FieldConduit for");

        // First step: define a field to store the conduit and add constructor logic
        // to initialize it

        String conduitFieldName = plasticClass.createAndInitializeFieldFromStaticContext(node.name + "_FieldConduit",
                FieldConduit.class.getName(), conduit);

        replaceFieldReadAccess(conduitFieldName);
        replaceFieldWriteAccess(conduitFieldName);

        state = FieldState.CONDUIT;

        return this;
    }

    @Override
    public <F> PlasticField setComputedConduit(ComputedValue<FieldConduit<F>> computedConduit)
    {
        assert computedConduit != null;

        plasticClass.check();

        verifyInitialState("set the computed FieldConduit for");

        // First step: define a field to store the conduit and add constructor logic
        // to initialize it

        PlasticField conduitField = plasticClass.introduceField(FieldConduit.class, node.name + "_FieldConduit").injectComputed(
                computedConduit);

        replaceFieldReadAccess(conduitField.getName());
        replaceFieldWriteAccess(conduitField.getName());

        state = FieldState.CONDUIT;

        return this;
    }

    @Override
    public PlasticField createAccessors(PropertyAccessType accessType)
    {
        plasticClass.check();

        return createAccessors(accessType, PlasticInternalUtils.toPropertyName(node.name));
    }

    @Override
    public PlasticField createAccessors(PropertyAccessType accessType, String propertyName)
    {
        plasticClass.check();

        assert accessType != null;
        assert PlasticInternalUtils.isNonBlank(propertyName);

        String capitalized = PlasticInternalUtils.capitalize(propertyName);

        if (accessType != PropertyAccessType.WRITE_ONLY)
        {
            String signature = node.signature == null ? null : "()" + node.signature;

            introduceAccessorMethod(getTypeName(), "get" + capitalized, null, signature,
                    new InstructionBuilderCallback()
                    {
                        @Override
                        public void doBuild(InstructionBuilder builder)
                        {
                            builder.loadThis().getField(PlasticFieldImpl.this).returnResult();
                        }
                    });
        }

        if (accessType != PropertyAccessType.READ_ONLY)
        {
            String signature = node.signature == null ? null : "(" + node.signature + ")V";

            introduceAccessorMethod("void", "set" + capitalized, new String[]
                    {getTypeName()}, signature, new InstructionBuilderCallback()
            {
                @Override
                public void doBuild(InstructionBuilder builder)
                {
                    builder.loadThis().loadArgument(0);
                    builder.putField(plasticClass.className, node.name, getTypeName());
                    builder.returnResult();
                }
            });
        }

        return this;
    }

    private void introduceAccessorMethod(String returnType, String name, String[] parameterTypes, String signature,
                                         InstructionBuilderCallback callback)
    {
        MethodDescription description = new MethodDescription(Opcodes.ACC_PUBLIC, returnType, name, parameterTypes,
                signature, null);

        String desc = plasticClass.nameCache.toDesc(description);

        if (plasticClass.inheritanceData.isImplemented(name, desc))
        {
            throw new IllegalArgumentException(String.format(
                    "Unable to create new accessor method %s on class %s as the method is already implemented.",
                    description.toString(), plasticClass.className));
        }

        plasticClass.introduceMethod(description, callback);
    }

    private void replaceFieldWriteAccess(String conduitFieldName)
    {
        ensureNotPublic();

        String setAccessName = plasticClass.makeUnique(plasticClass.methodNames, "conduit_set_" + node.name);

        setAccess = new MethodNode(accessForMethod(), setAccessName, "(" + node.desc + ")V", null, null);

        InstructionBuilder builder = plasticClass.newBuilder(setAccess);

        pushFieldConduitOntoStack(conduitFieldName, builder);

        builder.loadThis();

        plasticClass.pushInstanceContextFieldOntoStack(builder);

        // Take the value passed to this method and push it onto the stack.

        builder.loadArgument(0);
        builder.boxPrimitive(typeName);

        builder.invoke(FieldConduit.class, void.class, "set", Object.class, InstanceContext.class, Object.class);

        if (isWriteBehindEnabled())
        {
            builder.loadThis().loadArgument(0).putField(plasticClass.className, node.name, typeName);
        }

        builder.returnResult();

        plasticClass.addMethod(setAccess);

        plasticClass.redirectFieldWrite(node.name, isPrivate(), setAccess);
    }

    /**
     * Determines the access for a method that takes the place of reading from or writing to the field. Escalates
     * private fields to package private visibility, but leaves protected and package private alone (public fields
     * can not be instrumented).
     */
    private int accessForMethod()
    {
        return (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_FINAL | node.access) & ~Opcodes.ACC_PRIVATE;
    }

    private boolean isPrivate()
    {
        return Modifier.isPrivate(node.access);
    }

    private void replaceFieldReadAccess(String conduitFieldName)
    {
        ensureNotPublic();

        boolean writeBehindEnabled = isWriteBehindEnabled();

        String getAccessName = plasticClass.makeUnique(plasticClass.methodNames, "conduit_get_" + node.name);

        getAccess = new MethodNode(accessForMethod(), getAccessName, "()" + node.desc, null, null);

        InstructionBuilder builder = plasticClass.newBuilder(getAccess);

        // Get the correct FieldConduit object on the stack

        pushFieldConduitOntoStack(conduitFieldName, builder);

        builder.loadThis();

        // Now push the instance context on the stack

        plasticClass.pushInstanceContextFieldOntoStack(builder);

        builder.invoke(FieldConduit.class, Object.class, "get", Object.class, InstanceContext.class).castOrUnbox(
                typeName);

        if (writeBehindEnabled)
        {
            // Dupe the value, then push this, then swap

            if (isWide())
            {
                // Dupe this under the wide value, then pop the wide value

                builder.dupeWide().loadThis().dupe(2).pop();
            } else
            {
                builder.dupe().loadThis().swap();
            }

            // At which point the stack is the result value, this, the result value

            builder.putField(plasticClass.className, node.name, typeName);

            // And now it is just the result value
        }

        builder.returnResult();

        plasticClass.addMethod(getAccess);

        plasticClass.redirectFieldRead(node.name, isPrivate(), getAccess);
    }

    private boolean isWriteBehindEnabled()
    {
        return plasticClass.pool.isEnabled(TransformationOption.FIELD_WRITEBEHIND);
    }

    private boolean isWide()
    {
        PrimitiveType pt = PrimitiveType.getByName(typeName);

        return pt != null && pt.isWide();
    }

    private void pushFieldConduitOntoStack(String conduitFileName, InstructionBuilder builder)
    {
        builder.loadThis();
        builder.getField(plasticClass.className, conduitFileName, FieldConduit.class);
    }

    private void makeReadOnly()
    {
        ensureNotPublic();

        String setAccessName = plasticClass.makeUnique(plasticClass.methodNames, "reject_field_change_" + node.name);

        setAccess = new MethodNode(accessForMethod(), setAccessName, "(" + node.desc + ")V", null, null);

        String message = String.format("Field %s of class %s is read-only.", node.name, plasticClass.className);

        plasticClass.newBuilder(setAccess).throwException(IllegalStateException.class, message);

        plasticClass.addMethod(setAccess);

        plasticClass.redirectFieldWrite(node.name, isPrivate(), setAccess);

        node.access |= Opcodes.ACC_FINAL;
    }

    /**
     * Adds a static setter method, allowing an external FieldAccess implementation
     * to directly set the value of the field.
     */
    private MethodNode addShimSetAccessMethod()
    {
        String name = plasticClass.makeUnique(plasticClass.methodNames, "shim_set_" + node.name);

        // Takes two Object parameters (instance, and value) and returns void.

        MethodNode mn = new MethodNode(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_FINAL, name, "(" + node.desc + ")V", null, null);

        InstructionBuilder builder = plasticClass.newBuilder(mn);

        builder.loadThis().loadArgument(0).putField(plasticClass.className, node.name, typeName);
        builder.returnResult();

        plasticClass.addMethod(mn);

        plasticClass.fieldTransformMethods.add(mn);

        return mn;
    }

    private MethodNode addShimGetAccessMethod()
    {
        String name = plasticClass.makeUnique(plasticClass.methodNames, "shim_get_" + node.name);

        MethodNode mn = new MethodNode(Opcodes.ACC_SYNTHETIC | Opcodes.ACC_FINAL, name, "()" + node.desc, null, null);

        InstructionBuilder builder = plasticClass.newBuilder(mn);

        builder.loadThis().getField(plasticClass.className, node.name, typeName).returnResult();

        plasticClass.addMethod(mn);

        plasticClass.fieldTransformMethods.add(mn);

        return mn;
    }

    void installShim(PlasticClassHandleShim shim)
    {
        if (handle != null)
        {
            handle.shim = shim;
        }
    }

    /**
     * Invoked with the object instance on the stack and cast to the right type.
     */
    void extendShimGet(SwitchBlock switchBlock)
    {
        if (getAccess == null)
        {
            getAccess = addShimGetAccessMethod();
        }

        final String methodToInvoke = getAccess.name;

        plasticClass.shimInvokedMethods.add(getAccess);

        switchBlock.addCase(fieldIndex, false, new InstructionBuilderCallback()
        {
            @Override
            public void doBuild(InstructionBuilder builder)
            {
                builder.invokeVirtual(plasticClass.className, typeName, methodToInvoke).boxPrimitive(typeName).returnResult();
            }
        });
    }

    /**
     * Invoked with the object instance on the stack and cast to the right type, then the
     * new field value (as Object, needing to be cast or unboxed).
     */
    void extendShimSet(SwitchBlock switchBlock)
    {
        // If no conduit has yet been specified, then we need a set access method for the shim to invoke.

        if (setAccess == null)
        {
            setAccess = addShimSetAccessMethod();
        }

        plasticClass.shimInvokedMethods.add(setAccess);

        final String methodToInvoke = setAccess.name;

        switchBlock.addCase(fieldIndex, true, new InstructionBuilderCallback()
        {

            @Override
            public void doBuild(InstructionBuilder builder)
            {
                builder.castOrUnbox(typeName);
                builder.invokeVirtual(plasticClass.className, "void", methodToInvoke, typeName);
                // Should not be necessary, as its always a void method, and we can
                // drop to the bottom of the method.
                // builder.returnResult();
            }
        });
    }

}
