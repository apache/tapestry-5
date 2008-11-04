//  Copyright 2008 The Apache Software Foundation
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

package org.apache.tapestry5.internal.bindings;

import org.apache.tapestry5.ioc.Location;

/**
 * Extends {@link org.apache.tapestry5.internal.bindings.AbstractBinding} with a description and a binding type, with
 * invariant forced to true.
 */
public abstract class VariantBinding extends AbstractBinding
{
    private final Class bindingType;

    private final String description;

    public VariantBinding(Class bindingType, String description, Location location)
    {
        super(location);

        this.bindingType = bindingType;
        this.description = description;
    }

    @Override
    public boolean isInvariant()
    {
        return true;
    }

    @Override
    public Class getBindingType()
    {
        return bindingType;
    }

    @Override
    public String toString()
    {
        return String.format("VariantBinding[%s]", description);
    }
}
