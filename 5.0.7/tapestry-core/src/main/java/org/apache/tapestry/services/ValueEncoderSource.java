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

import org.apache.tapestry.ComponentResources;
import org.apache.tapestry.ValueEncoder;

/**
 * A source for value encoders based on a property type.
 */
public interface ValueEncoderSource
{
    /**
     * Creates a value encoder based on the <em>type</em> of the named parameter.
     *
     * @param parameterName the name of the parameter whose type is used to locate a PKE factory
     * @param resources     the resources of the component, from which parameter and its type are extracted
     * @return the value encoder
     */
    ValueEncoder createEncoder(String parameterName, ComponentResources resources);
}
