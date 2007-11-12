// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.internal.structure;

import org.apache.tapestry.Binding;
import org.apache.tapestry.MarkupWriter;
import org.apache.tapestry.ioc.services.TypeCoercer;
import org.apache.tapestry.runtime.RenderQueue;

/**
 *
 */
public class ExpansionPageElement implements PageElement
{
    private final Binding _binding;

    private final boolean _invariant;

    private final TypeCoercer _coercer;

    private boolean _cached;

    private String _cachedValue;

    public ExpansionPageElement(Binding binding, TypeCoercer coercer)
    {
        _binding = binding;
        _coercer = coercer;

        _invariant = _binding.isInvariant();
    }

    public void render(MarkupWriter writer, RenderQueue queue)
    {
        String value = _cached ? _cachedValue : _coercer.coerce(_binding.get(), String.class);

        if (_invariant && !_cached)
        {
            _cachedValue = value;
            _cached = true;
        }

        writer.write(value);
    }

    @Override
    public String toString()
    {
        return String.format("Expansion[%s]", _binding.toString());
    }
}
