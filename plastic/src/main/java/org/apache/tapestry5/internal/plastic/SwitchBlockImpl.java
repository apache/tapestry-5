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

import org.apache.tapestry5.internal.plastic.asm.Label;
import org.apache.tapestry5.internal.plastic.asm.Opcodes;
import org.apache.tapestry5.plastic.InstructionBuilder;
import org.apache.tapestry5.plastic.InstructionBuilderCallback;
import org.apache.tapestry5.plastic.SwitchBlock;
import org.apache.tapestry5.plastic.SwitchCallback;

public class SwitchBlockImpl extends Lockable implements SwitchBlock, Opcodes
{
    private final InstructionBuilder builder;

    private final InstructionBuilderState state;

    private final int min, max;

    private final Label defaultLabel, endSwitchLabel;

    private final Label[] caseLabels;

    private boolean defaultAdded = false;

    SwitchBlockImpl(InstructionBuilder builder, InstructionBuilderState state, int min, int max)
    {
        assert min <= max;

        this.builder = builder;
        this.state = state;
        this.min = min;
        this.max = max;

        defaultLabel = new Label();
        endSwitchLabel = new Label();

        caseLabels = new Label[max - min + 1];

        for (int i = min; i <= max; i++)
        {
            caseLabels[i - min] = new Label();
        }

        state.visitor.visitTableSwitchInsn(min, max, defaultLabel, caseLabels);
    }

    void doCallback(SwitchCallback callback)
    {
        check();

        callback.doSwitch(this);

        if (!defaultAdded)
        {
            addDefault(new InstructionBuilderCallback()
            {
                @Override
                public void doBuild(InstructionBuilder builder)
                {
                    builder.throwException(IllegalArgumentException.class,
                            "Switch value not matched in case statement.");
                }
            });
        }

        state.visitor.visitLabel(endSwitchLabel);

        lock();
    }

    @Override
    public void addCase(int caseValue, boolean jumpToEnd, InstructionBuilderCallback callback)
    {
        assert caseValue >= min;
        assert caseValue <= max;

        if (defaultAdded)
            throw new IllegalStateException("The default block must come last.");

        // TODO: Check that no case value is repeated

        state.visitor.visitLabel(caseLabels[caseValue - min]);

        callback.doBuild(builder);

        if (jumpToEnd)
            state.visitor.visitJumpInsn(GOTO, endSwitchLabel);
    }

    @Override
    public void addDefault(InstructionBuilderCallback callback)
    {
        if (defaultAdded)
            throw new IllegalStateException("A SwitchBlock may only have one default block.");

        state.visitor.visitLabel(defaultLabel);

        callback.doBuild(builder);

        defaultAdded = true;
    }
}
