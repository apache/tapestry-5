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
 * Allows a portion of a method to be marked so that exception and finally handlers can be provided.
 * 
 * @see InstructionBuilder#startTryCatch(TryCatchCallback)
 */
public interface TryCatchBlock
{
    /**
     * Invoked first, to generate the code in which exceptions may be caught.
     */
    void addTry(InstructionBuilderCallback callback);

    /**
     * Ends the block (if not already ended) and inserts a catch block for the named exception.
     * The InstructionBuilder is returned so that the code for handling the exception can be added. The exception object
     * will be on top of the stack. This should be called after {@link #addTry(InstructionBuilderCallback)}.
     *
     * Note: no attempt is made currently to sort the handlers; for example adding a catch for java.lang.Exception first
     * will mean that more specific exception handlers added later will never be invoked.
     * 
     * @param exceptionClassName
     *            caught exception class
     * @param callback
     *            that implements the logic of the catch block
     */
    @Opcodes("TRYCATCHBLOCK")
    void addCatch(String exceptionClassName, InstructionBuilderCallback callback);

    /**
     * As with {@link #addCatch(String, InstructionBuilderCallback)}, but the exception caught is
     * null, which acts as a finally block in the Java language. This must be called last (after
     * {@link #addTry(InstructionBuilderCallback)} and any calls to
     * {@link #addCatch(String, InstructionBuilderCallback)}.
     * 
     * @param callback
     *            implements the logic of the finally block
     */
    @Opcodes("TRYCATCHBLOCK")
    void addFinally(InstructionBuilderCallback callback);
}
