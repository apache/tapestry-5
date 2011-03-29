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
 * Support for building the equivalent of a Java switch statement.
 */
public interface SwitchBlock
{
    /**
     * Adds a handler for a particular case value. This method should only be invoked at most once for each case
     * value.
     * 
     * @param caseValue
     *            value to match
     * @param jumpToEnd
     *            true if a jump to the end should be provided, or false
     *            if either the callback generated a return opcode, or
     *            it is desired to "drop down" into the next case handler.
     *            The last case handled drop down out of the SwitchBlock.
     * @param callback
     *            provides the logic for the specified case
     */
    void addCase(int caseValue, boolean jumpToEnd, InstructionBuilderCallback callback);

    /**
     * Adds the default handler. This is optional, and is only allowed after all cases have been added.
     * The default handler automatically throws an {@link IllegalArgumentException}.
     * 
     * @param callback
     *            provides the logic for the default handler case.
     */
    void addDefault(InstructionBuilderCallback callback);

}
