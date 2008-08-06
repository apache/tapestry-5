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

package org.apache.tapestry5.internal.services;

import org.apache.tapestry5.ValueEncoder;
import org.apache.tapestry5.services.ValueEncoderFactory;

/**
 * An implementation of {@link ValueEncoderFactory} that returns a pre-wired instance of {@link ValueEncoder}. This is
 * odd for a factory, because it doesn't actually create the returned instance, just stores it until the encoder is
 * needed.
 *
 * @param <V>
 */
public class GenericValueEncoderFactory<V> implements ValueEncoderFactory<V>
{
    private final ValueEncoder<V> encoder;

    public GenericValueEncoderFactory(ValueEncoder<V> encoder)
    {
        this.encoder = encoder;
    }

    public ValueEncoder<V> create(Class<V> type)
    {
        return encoder;
    }

    public static <V> GenericValueEncoderFactory<V> create(ValueEncoder<V> encoder)
    {
        return new GenericValueEncoderFactory<V>(encoder);
    }
}
