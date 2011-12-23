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
 * Identifies the type of class referenced in a {@link PlasticClassEvent}.
 * 
 * @see PlasticClassListener
 */
public enum ClassType
{
    /**
     * A primary class, either created new or by transforming an existing class.
     */
    PRIMARY,

    /** A supporting class, needed to implement some of the logic of the primary class. */
    SUPPORT,

    /**
     * An implementation of {@link MethodInvocation}, needed to handle advice added to
     * a method of a primary class.
     */
    METHOD_INVOCATION,

    /**
     * An inner class within a controlled package, which may have field accesses (to non-private
     * fields visible to it) instrumented.
     *
     * @since 5.4
     */
    INNER;
}
