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
import org.apache.tapestry5.plastic.InstructionBuilder;
import org.apache.tapestry5.plastic.InstructionBuilderCallback;
import org.apache.tapestry5.plastic.TryCatchBlock;
import org.apache.tapestry5.plastic.TryCatchCallback;

public class TryCatchBlockImpl extends Lockable implements TryCatchBlock
{
    private final InstructionBuilder builder;

    private final InstructionBuilderState state;

    private final Label startLabel, endLabel;

    TryCatchBlockImpl(InstructionBuilder builder, InstructionBuilderState state)
    {
        this.builder = builder;
        this.state = state;

        this.startLabel = new Label();
        this.endLabel = new Label();
    }

    @Override
    public void addTry(InstructionBuilderCallback callback)
    {
        state.visitor.visitLabel(startLabel);

        callback.doBuild(builder);

        state.visitor.visitLabel(endLabel);
    }

    @Override
    public void addCatch(String exceptionClassName, InstructionBuilderCallback callback)
    {
        assert exceptionClassName != null;

        doCatch(state.nameCache.toInternalName(exceptionClassName), callback);
    }

    private void doCatch(String exceptionInternalName, InstructionBuilderCallback callback)
    {
        check();

        Label handler = state.newLabel();

        callback.doBuild(builder);

        state.visitor.visitTryCatchBlock(startLabel, endLabel, handler, exceptionInternalName);
    }

    @Override
    public void addFinally(InstructionBuilderCallback callback)
    {
        doCatch(null, callback);
    }

    void doCallback(TryCatchCallback callback)
    {
        assert callback != null;

        callback.doBlock(this);

        lock();
    }

}
