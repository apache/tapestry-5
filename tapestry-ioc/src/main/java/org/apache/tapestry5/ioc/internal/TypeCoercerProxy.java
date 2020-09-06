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

package org.apache.tapestry5.ioc.internal;

import org.apache.tapestry5.commons.services.TypeCoercer;

/**
 * A simplified version of {@link TypeCoercer} used to defer the instantiation of the actual TypeCoercer
 * service until necessary.
 * 
 * @since 5.3
 */
public interface TypeCoercerProxy
{
    /**
     * Returns input cast to targetType if input is an instance of target type, otherwise delegates
     * to {@link TypeCoercer#coerce(Object, Class)}.
     * 
     * @param <S>
     * @param <T>
     * @param input
     *            value to be coerced
     * @param targetType
     *            desired type of value
     * @return the value, coerced
     * @throws RuntimeException
     *             if the input can not be coerced
     */
    <S, T> T coerce(S input, Class<T> targetType);

}
