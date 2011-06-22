// Copyright 2007, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.internal.bindings.AbstractBinding;
import org.apache.tapestry5.ioc.Location;

/**
 * Wraps a {@link StringProvider} as a read-only {@link Binding}.
 */
public class AttributeExpansionBinding extends AbstractBinding
{
    private final StringProvider provider;

    public AttributeExpansionBinding(Location location, StringProvider provider)
    {
        super(location);

        this.provider = provider;
    }

    public Object get()
    {
        return provider.provideString();
    }

    /**
     * Returns false. Expansions reference properties that may change arbitrarily.
     */
    @Override
    public boolean isInvariant()
    {
        return false;
    }
}
