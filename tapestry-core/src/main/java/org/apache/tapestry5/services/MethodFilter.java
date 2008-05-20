// Copyright 2006, 2007 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.services;

/**
 * Used by {@link ClassTransformation#findMethods(MethodFilter)} to accept or reject each method.
 */
public interface MethodFilter
{
    /**
     * Passed each signature in turn, only signatures for which this method returns true will be included in the final
     * result.
     */
    boolean accept(TransformMethodSignature signature);
}
