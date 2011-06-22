// Copyright 2007, 2008, 2009 The Apache Software Foundation
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

import org.apache.tapestry5.Binding;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.FieldTranslator;
import org.apache.tapestry5.internal.services.StringInterner;
import org.apache.tapestry5.ioc.Location;
import org.apache.tapestry5.services.BindingFactory;
import org.apache.tapestry5.services.FieldTranslatorSource;

/**
 * Interprets the binding expression as the name of a {@link org.apache.tapestry5.Translator} provided by the {@link
 * org.apache.tapestry5.services.TranslatorSource}.
 */
public class TranslateBindingFactory implements BindingFactory
{
    private final FieldTranslatorSource source;

    private final StringInterner interner;

    public TranslateBindingFactory(FieldTranslatorSource source, StringInterner interner)
    {
        this.source = source;
        this.interner = interner;
    }

    public Binding newBinding(String description, ComponentResources container,
                              final ComponentResources component, final String expression, Location location)
    {
        return new InvariantBinding(location, FieldTranslator.class, interner.intern(description + ": " + expression))
        {
            public Object get()
            {
                return source.createTranslator(component, expression);
            }
        };
    }
}
