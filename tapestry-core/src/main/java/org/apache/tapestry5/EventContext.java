// Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5;

/**
 * A collection of parameters that may eventually be passed to an event handler method.  Includes the ability to coerce
 * or encode parameters as needed.
 *
 * @see org.apache.tapestry5.ioc.services.TypeCoercer
 * @see org.apache.tapestry5.ValueEncoder
 */
public interface EventContext
{
    /**
     * Returns the number of parameter values that can be extracted.
     */
    int getCount();

    /**
     * Extracts a parameter value and coerces or decodes it to the desired type.
     *
     * @param desiredType the type of value required
     * @param index       identifies which parameter value to extract
     * @return the value extracted and converted or coerced
     * @throws RuntimeException if the value can't be converted or the index is out of range
     */
    <T> T get(Class<T> desiredType, int index);
}
