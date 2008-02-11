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

package org.apache.tapestry.internal.bindings;

import org.apache.tapestry.ioc.Location;

/**
 * Binding type for literal, immutable values. Literal bindings are {@linkplain org.apache.tapestry.Binding#isInvariant()
 * invariant}; any value provided by a LiteralBinding, even if {@linkplain org.apache.tapestry.ioc.services.TypeCoercer#coerce(Object,
 * Class) coerced}, will be cached aggresively by Tapestry cmponent.
 * <p/>
 * <p>LiteralBindings are often used for literal string values supplied in-line in the component template, but is used
 * for many other things as well, any kind of fixed, read-only value.
 */
public class LiteralBinding extends AbstractBinding
{
    private final String _description;

    private final Object _value;

    public LiteralBinding(String description, Object value, Location location)
    {
        super(location);
        _description = description;
        _value = value;
    }

    public Object get()
    {
        return _value;
    }

    @Override
    public String toString()
    {
        return String.format("LiteralBinding[%s: %s]", _description, _value);
    }
}
