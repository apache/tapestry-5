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

    final Map<String, LocalVariable> locals = PlasticInternalUtils.newMap();

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

    Label newLabel()
    {
        Label result = new Label();

        visitor.visitLabel(result);

        return result;
    }
}
