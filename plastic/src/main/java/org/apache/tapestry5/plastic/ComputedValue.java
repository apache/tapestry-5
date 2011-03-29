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
 * Provides an indirect, or computed, value. This is used for certain
 * kinds of injection, where the exact value to be injected must be computed
 * for each <em>instance</em> being instantiated, rather than for the
 * class as a whole.
 * 
 * @see PlasticField#injectComputed(ComputedValue)
 */
public interface ComputedValue<T>
{
    /**
     * Computes or otherwise provides the value, given the instance's context.
     */
    T get(InstanceContext context);
}
