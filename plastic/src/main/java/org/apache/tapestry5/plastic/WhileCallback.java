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
 * Callback used with {@link InstructionBuilder#doWhile(Condition, WhileCallback)}.
 */
public interface WhileCallback
{
    /**
     * Creates the code to be evaluated by the {@link Condition}; often this involves
     * loading a value from a variable or argument, or {@link InstructionBuilder#dupe()}'ing the
     * top value on the stack.
     */
    void buildTest(InstructionBuilder builder);

    /**
     * Provides the main code executed inside the loop.
     */
    void buildBody(InstructionBuilder builder);
}
