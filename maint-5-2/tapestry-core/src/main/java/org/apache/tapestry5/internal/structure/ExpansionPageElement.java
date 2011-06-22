// Copyright 2006, 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.structure;

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.runtime.RenderCommand;
import org.apache.tapestry5.runtime.RenderQueue;

public class ExpansionPageElement implements RenderCommand
{
    private final Binding binding;

    private final boolean invariant;

    private final TypeCoercer coercer;

    private boolean cached;

    private String cachedValue;

    public ExpansionPageElement(Binding binding, TypeCoercer coercer)
    {
        this.binding = binding;
        this.coercer = coercer;

        invariant = this.binding.isInvariant();
    }

    public void render(MarkupWriter writer, RenderQueue queue)
    {
        String value = cached ? cachedValue : coercer.coerce(binding.get(), String.class);

        if (invariant && !cached)
        {
            cachedValue = value;
            cached = true;
        }

        writer.write(value);
    }

    @Override
    public String toString()
    {
        return String.format("Expansion[%s]", binding.toString());
    }
}
