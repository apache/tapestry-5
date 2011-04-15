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
 * Condition used with {@link InstructionBuilder#when(Condition, WhenCallback)}. Most conditions
 * pop the top element off the stack; some pop two elements.
 */
public enum Condition
{
    /** Is the top element of the stack null? */
    NULL,

    /** Is the top element of the stack non-null? */
    NON_NULL,

    /** Is the top element of the stack the integer zero? */
    ZERO,

    /** Is the top element of the stack not the integer zero? */
    NON_ZERO,

    /**
     * Compare two integer elements on the stack; branch if the deeper
     * element is less than the top element.
     */
    LESS_THAN,

    /**
     * Compare two integer elements on the stack; branch if the deeper
     * element equal to the top element.
     */
    EQUAL,

    /**
     * Compare two integer elements on the stack; branch if the deeper
     * element is not equal to the top element.
     */
    NOT_EQUAL,

    /**
     * Compare two integer elements on the stack; branch if the deeper
     * element is greater than the top element.
     */
    GREATER;
}
