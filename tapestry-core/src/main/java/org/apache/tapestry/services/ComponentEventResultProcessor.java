// Copyright 2007, 2008 The Apache Software Foundation
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

package org.apache.tapestry.services;

import org.apache.tapestry.runtime.Component;

import java.io.IOException;

/**
 * Responsible for handling the return value provided by a component event handler method.
 *
 * @param <T>
 */
public interface ComponentEventResultProcessor<T>
{
    /**
     * For a given, non-null return value from a component event method, construct and send a response.
     *
     * @param value            the value returned from a method
     * @param component        the component on which a method was invoked
     * @param methodDescripion a description of method which provided the value
     * @throws RuntimeException if the value can not handled
     */
    void processResultValue(T value, Component component, String methodDescripion) throws IOException;
}
