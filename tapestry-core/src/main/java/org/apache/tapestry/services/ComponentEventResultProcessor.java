// Copyright 2007 The Apache Software Foundation
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

/**
 * Responsible for handling the return value provided by a component event handler.
 *
 * @param <T>
 */
public interface ComponentEventResultProcessor<T>
{
    /**
     * For a given, non-null return value, provide a corresponding Link object (which will
     * ultimately be transformed into a URL and sent to the client as a redirect).
     *
     * @param value            the value returned from a method
     * @param component        the component on which a method was invoked
     * @param methodDescripion a description of method which provided the value
     * @return an object that can send the request to the client
     * @throws RuntimeException if the value can not be converted into a link
     */
    ActionResponseGenerator processComponentEvent(T value, Component component,
                                                  String methodDescripion);
}
