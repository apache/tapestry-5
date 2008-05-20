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

package org.apache.tapestry5.services;

import org.apache.tapestry5.ValueEncoder;

/**
 * A source for {@link ValueEncoder} instances of a given type.
 */
public interface ValueEncoderFactory<V>
{
    /**
     * For a given type, create an encoder.
     *
     * @param type type of object for which an encoder is needed
     * @return the encoder for the object
     */
    ValueEncoder<V> create(Class<V> type);
}
