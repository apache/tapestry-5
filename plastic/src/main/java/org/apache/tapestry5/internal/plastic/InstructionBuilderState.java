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

import java.util.Map;

import org.apache.tapestry5.internal.plastic.asm.Label;
import org.apache.tapestry5.internal.plastic.asm.MethodVisitor;
import org.apache.tapestry5.internal.plastic.asm.Opcodes;
import org.apache.tapestry5.plastic.LocalVariable;
import org.apache.tapestry5.plastic.MethodDescription;

/**
 * Stores information about the method whose instructions are being constructed, to make it easier
 * to share data across multiple instances.
 */
public class InstructionBuilderState implements Opcodes
{
    final MethodDescription description;

    final MethodVisitor visitor;

    final NameCache nameCache;

    int localIndex;

    int varSuffix;

    static class LVInfo
    {
        final int width, offset, loadOpcode, storeOpcode;

        final Label end;

        public LVInfo(int width, int offset, int loadOpcode, int storeOpcode, Label end)
        {
            this.width = width;
            this.offset = offset;
            this.loadOpcode = loadOpcode;
            this.storeOpcode = storeOpcode;
            this.end = end;
        }
    }

    /** Map from LocalVariable to Integer offset. */
    final Map<LocalVariable, LVInfo> locals = PlasticInternalUtils.newMap();

    /** Index for argument (0 is first true argument); allows for double-width primitive types. */
    final int[] argumentIndex;

    /** Opcode used to load argument (0 is first true argument). */
    final int[] argumentLoadOpcode;

    protected InstructionBuilderState(MethodDescription description, MethodVisitor visitor, NameCache nameCache)
    {
        this.description = description;
        this.visitor = visitor;
        this.nameCache = nameCache;

        // TODO: Account for static methods?

        int argCount = description.argumentTypes.length;

        argumentIndex = new int[argCount];
        argumentLoadOpcode = new int[argCount];

        // first argument index is for "this"

        int offset = 1;

        for (int i = 0; i < argCount; i++)
        {
            PrimitiveType type = PrimitiveType.getByName(description.argumentTypes[i]);

            argumentIndex[i] = offset++;
            argumentLoadOpcode[i] = type == null ? ALOAD : type.loadOpcode;

            if (type != null && type.isWide())
                offset++;
        }

        localIndex = offset;
    }

    /** Creates a new Label and adds it to the method. */
    Label newLabel()
    {
        Label result = new Label();

        visitor.visitLabel(result);

        return result;
    }

    LocalVariable startVariable(String type)
    {
        Label start = newLabel();
        Label end = new Label();

        PrimitiveType ptype = PrimitiveType.getByName(type);

        int width = (ptype != null && ptype.isWide()) ? 2 : 1;

        int loadOpcode = ptype == null ? ALOAD : ptype.loadOpcode;
        int storeOpcode = ptype == null ? ASTORE : ptype.storeOpcode;

        LVInfo info = new LVInfo(width, localIndex, loadOpcode, storeOpcode, end);

        localIndex += width;

        LocalVariable var = new LocalVariableImpl(type);

        locals.put(var, info);

        visitor.visitLocalVariable(nextVarName(), nameCache.toDesc(type), null, start, end, info.offset);

        return var;
    }

    void load(LocalVariable var)
    {
        LVInfo info = locals.get(var);

        visitor.visitVarInsn(info.loadOpcode, info.offset);
    }

    void store(LocalVariable var)
    {
        LVInfo info = locals.get(var);

        visitor.visitVarInsn(info.storeOpcode, info.offset);
    }

    void stopVariable(LocalVariable variable)
    {
        LVInfo info = locals.get(variable);

        visitor.visitLabel(info.end);

        locals.remove(variable);

        localIndex -= info.width;
    }

    private String nextVarName()
    {
        return "var" + varSuffix++;
    }
}
