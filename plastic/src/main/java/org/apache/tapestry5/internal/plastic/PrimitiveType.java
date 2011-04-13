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

import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.internal.plastic.asm.Opcodes;

/**
 * Collects together information needed to write code that involves primitive types, including
 * moving between wrapper types and primitive values, or extracting a primitive value from
 * the {@link StaticContext}.
 */
@SuppressWarnings("rawtypes")
public enum PrimitiveType implements Opcodes
{
    VOID("void", "V", void.class, Void.class, null, null, ILOAD, ISTORE, RETURN),

    BOOLEAN("boolean", "Z", boolean.class, Boolean.class, "booleanValue", "getBoolean", ILOAD, ISTORE, IRETURN),

    CHAR("char", "C", char.class, Character.class, "charValue", "getChar", ILOAD, ISTORE, IRETURN),

    BYTE("byte", "B", byte.class, Byte.class, "byteValue", "getByte", ILOAD, ISTORE, IRETURN),

    SHORT("short", "S", short.class, Short.class, "shortValue", "getShort", ILOAD, ISTORE, IRETURN),

    INT("int", "I", int.class, Integer.class, "intValue", "getInt", ILOAD, ISTORE, IRETURN),

    FLOAT("float", "F", float.class, Float.class, "floatValue", "getFloat", FLOAD, FSTORE, FRETURN),

    LONG("long", "J", long.class, Long.class, "longValue", "getLong", LLOAD, LSTORE, LRETURN),

    DOUBLE("double", "D", double.class, Double.class, "doubleValue", "getDouble", DLOAD, DSTORE, DRETURN);

    /**
     * @param name
     *            the Java source name for the type
     * @param descriptor
     *            Java descriptor for the type ('Z', 'I', etc.)
     * @param primitiveType
     *            TODO
     * @param wrapperType
     *            wrapper type, e.g., java.lang.Integer
     * @param toValueMethodName
     *            name of method of wrapper class to extract primitive value
     * @param getFromStaticContextMethodName
     *            name of method of {@link StaticContext} used to extract primitive context value
     * @param loadOpcode
     *            Correct opcode for loading an argument or local variable onto the stack (ILOAD, LLOAD, FLOAD or
     *            DLOAD)
     * @param storeOpcode
     *            matching opcode for storing a value to a local variable (ISTORE, LSTORE, FSTORE or DSTORE)
     * @param returnOpcode
     *            Correct opcode for returning the top value on the stack (IRETURN, LRETURN, FRETURN
     *            or DRETURN)
     */
    private PrimitiveType(String name, String descriptor, Class primitiveType, Class wrapperType,
            String toValueMethodName, String getFromStaticContextMethodName, int loadOpcode, int storeOpcode,
            int returnOpcode)
    {
        this.name = name;
        this.descriptor = descriptor;
        this.primitiveType = primitiveType;
        this.wrapperType = wrapperType;
        this.wrapperInternalName = wrapperType == null ? null : PlasticInternalUtils.toInternalName(wrapperType
                .getName());
        this.toValueMethodName = toValueMethodName;
        this.getFromStaticContextMethodName = getFromStaticContextMethodName;
        this.loadOpcode = loadOpcode;
        this.storeOpcode = storeOpcode;
        this.returnOpcode = returnOpcode;

        this.valueOfMethodDescriptor = String.format("(%s)L%s;", descriptor, wrapperInternalName);
        this.toValueMethodDescriptor = "()" + descriptor;
        this.getFromStaticContextMethodDescriptor = "(I)" + descriptor;
    }

    public final String name, descriptor, wrapperInternalName, valueOfMethodDescriptor, toValueMethodName,
            getFromStaticContextMethodName, toValueMethodDescriptor, getFromStaticContextMethodDescriptor;

    public final Class primitiveType, wrapperType;

    public final int loadOpcode, storeOpcode, returnOpcode;

    private static final Map<String, PrimitiveType> BY_NAME = new HashMap<String, PrimitiveType>();
    private static final Map<String, PrimitiveType> BY_DESC = new HashMap<String, PrimitiveType>();
    private static final Map<Class, PrimitiveType> BY_PRIMITIVE_TYPE = new HashMap<Class, PrimitiveType>();

    static
    {
        for (PrimitiveType type : values())
        {
            BY_NAME.put(type.name, type);
            BY_DESC.put(type.descriptor, type);
            BY_PRIMITIVE_TYPE.put(type.primitiveType, type);
        }
    }

    public boolean isWide()
    {
        return this == LONG || this == DOUBLE;
    }

    /**
     * Returns the primitive type matching the given type name or null for a non-primitive type (an array type,
     * or an class name).
     * 
     * @param name
     *            possible primitive name
     * @return the type or null
     */
    public static PrimitiveType getByName(String name)
    {
        return BY_NAME.get(name);
    }

    public static PrimitiveType getByPrimitiveType(Class primitiveType)
    {
        return BY_PRIMITIVE_TYPE.get(primitiveType);
    }
}
